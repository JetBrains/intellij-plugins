// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.coverage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageLoadErrorReporter;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageSuite;
import com.intellij.coverage.FailedCoverageLoadingResult;
import com.intellij.coverage.CoverageLoadingResult;
import com.intellij.coverage.SuccessCoverageLoadingResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.rt.coverage.data.ClassData;
import com.intellij.rt.coverage.data.LineData;
import com.intellij.rt.coverage.data.ProjectData;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartFileInfo;
import com.jetbrains.lang.dart.analyzer.DartFileInfoKt;
import com.jetbrains.lang.dart.analyzer.DartLocalFileInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.SortedMap;

public final class DartCoverageRunner extends CoverageRunner {
  private static final String ID = "DartCoverageRunner";
  private static final Logger LOG = Logger.getInstance(DartCoverageRunner.class.getName());

  @Override
  public @NotNull CoverageLoadingResult loadCoverageData(
    final @NotNull File sessionDataFile,
    @Nullable CoverageSuite baseCoverageSuite,
    @NotNull CoverageLoadErrorReporter reporter
  ) {
    if (!(baseCoverageSuite instanceof DartCoverageSuite)) {
      String message = "Expected Dart coverage suite, got " + (baseCoverageSuite == null ? "null" : baseCoverageSuite.getClass().getName());
      LOG.warn(message);
      return new FailedCoverageLoadingResult(message);
    }

    if (ApplicationManager.getApplication().isDispatchThread()) {
      final Ref<CoverageLoadingResult> projectDataRef = new Ref<>();

      ProgressManager.getInstance().runProcessWithProgressSynchronously(
        () -> projectDataRef.set(doLoadCoverageData(sessionDataFile, (DartCoverageSuite)baseCoverageSuite, reporter)),
        DartBundle.message("progress.title.loading.coverage.data"), true, baseCoverageSuite.getProject());

      return projectDataRef.get();
    }
    else {
      return doLoadCoverageData(sessionDataFile, (DartCoverageSuite)baseCoverageSuite, reporter);
    }
  }

  private static CoverageLoadingResult doLoadCoverageData(
    final @NotNull File sessionDataFile,
    final @NotNull DartCoverageSuite coverageSuite,
    final @NotNull CoverageLoadErrorReporter reporter
  ) {
    final ProcessHandler coverageProcess = coverageSuite.getCoverageProcess();
    // coverageProcess == null means that we are switching to data gathered earlier
    if (coverageProcess != null) {
      for (int i = 0; i < 100; ++i) {
        ProgressManager.checkCanceled();

        if (coverageProcess.waitFor(100)) {
          break;
        }
      }

      if (!coverageProcess.isProcessTerminated()) {
        coverageProcess.destroyProcess();
        String message = "Dart coverage process is running too long, terminating";
        LOG.warn(message);
        return new FailedCoverageLoadingResult(message);
      }
    }

    final Project project = coverageSuite.getProject();
    final String contextFilePath = coverageSuite.getContextFilePath();
    if (project == null || contextFilePath == null) {
      String message = "Could not get project or context file path";
      LOG.warn(message);
      return new FailedCoverageLoadingResult(message);
    }

    final String contextId = DartAnalysisServerService.getInstance(project).execution_createContext(contextFilePath, reporter);
    if (contextId == null) {
      String message = "Could not create context for " + contextFilePath;
      LOG.warn(message);
      return new FailedCoverageLoadingResult(message);
    }

    final ProjectData projectData = new ProjectData();

    try {
      DartCoverageData data =
        new Gson().fromJson(new BufferedReader(new FileReader(sessionDataFile, StandardCharsets.UTF_8)), DartCoverageData.class);
      if (data == null) {
        String message = "Coverage file does not contain valid data.";
        LOG.warn(message);
        return new FailedCoverageLoadingResult(message);
      }

      for (Map.Entry<String, SortedMap<Integer, Integer>> entry : data.getMergedDartFileCoverageData().entrySet()) {
        ProgressManager.checkCanceled();

        String filePath = getFileForUri(project, contextId, entry.getKey());
        if (filePath == null) {
          // File is not found.
          String message = "Could not find source: " + entry.getKey();
          LOG.warn(message);
          reporter.reportWarning(message, null);
          continue;
        }
        SortedMap<Integer, Integer> lineHits = entry.getValue();
        ClassData classData = projectData.getOrCreateClassData(filePath);
        if (lineHits.isEmpty()) {
          classData.setLines(new LineData[1]);
          continue;
        }
        LineData[] lines = new LineData[lineHits.lastKey() + 1];
        for (Map.Entry<Integer, Integer> hit : lineHits.entrySet()) {
          LineData lineData = new LineData(hit.getKey(), null);
          lineData.setHits(hit.getValue());
          lines[hit.getKey()] = lineData;
        }
        classData.setLines(lines);
      }
    }
    catch (JsonSyntaxException | IOException e) {
      LOG.warn(e);
      reporter.reportWarning(e);
    }
    finally {
      DartAnalysisServerService.getInstance(project).execution_deleteContext(contextId);
    }
    return new SuccessCoverageLoadingResult(projectData);
  }

  private static @Nullable String getFileForUri(final @NotNull Project project, final @NotNull String contextId, final @NotNull String uri) {
    if (uri.startsWith("dart:_") || uri.startsWith("dart:") && uri.contains("-patch/")) {
      // dart:_builtin or dart:core-patch/core_patch.dart
      return null;
    }

    String filePathOrUri = DartAnalysisServerService.getInstance(project).execution_mapUri(contextId, uri);
    DartFileInfo fileInfo = filePathOrUri != null ? DartFileInfoKt.getDartFileInfo(project, filePathOrUri) : null;
    if (fileInfo instanceof DartLocalFileInfo localFileInfo) {
      return localFileInfo.getFilePath();
    }
    return null;
  }

  @Override
  public @NotNull String getPresentableName() {
    return "Dart";
  }

  @Override
  public @NotNull String getId() {
    return ID;
  }

  @Override
  public @NotNull String getDataFileExtension() {
    return "json";
  }

  @Override
  public boolean acceptsCoverageEngine(@NotNull CoverageEngine engine) {
    return engine instanceof DartCoverageEngine;
  }
}
