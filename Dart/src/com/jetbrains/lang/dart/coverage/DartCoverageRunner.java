/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.coverage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageSuite;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.rt.coverage.data.ClassData;
import com.intellij.rt.coverage.data.LineData;
import com.intellij.rt.coverage.data.ProjectData;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.SortedMap;

public class DartCoverageRunner extends CoverageRunner {
  private static final String ID = "DartCoverageRunner";
  private static final Logger LOG = Logger.getInstance(DartCoverageRunner.class.getName());

  @Nullable
  @Override
  public ProjectData loadCoverageData(@NotNull final File sessionDataFile, @Nullable CoverageSuite baseCoverageSuite) {
    if (!(baseCoverageSuite instanceof DartCoverageSuite)) {
      return null;
    }

    if (ApplicationManager.getApplication().isDispatchThread()) {
      final Ref<ProjectData> projectDataRef = new Ref<>();

      ProgressManager.getInstance().runProcessWithProgressSynchronously(
        () -> projectDataRef.set(doLoadCoverageData(sessionDataFile, (DartCoverageSuite)baseCoverageSuite)),
        "Loading Coverage Data...", true, baseCoverageSuite.getProject());

      return projectDataRef.get();
    }
    else {
      return doLoadCoverageData(sessionDataFile, (DartCoverageSuite)baseCoverageSuite);
    }
  }

  @Nullable
  private static ProjectData doLoadCoverageData(@NotNull final File sessionDataFile, @NotNull final DartCoverageSuite coverageSuite) {
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
        return null;
      }
    }

    final Project project = coverageSuite.getProject();
    final String contextFilePath = coverageSuite.getContextFilePath();
    if (project == null || contextFilePath == null) {
      return null;
    }

    final String contextId = DartAnalysisServerService.getInstance(project).execution_createContext(contextFilePath);
    if (contextId == null) {
      return null;
    }

    final ProjectData projectData = new ProjectData();

    try {
      DartCoverageData data = new Gson().fromJson(new BufferedReader(new FileReader(sessionDataFile)), DartCoverageData.class);
      if (data == null) {
        LOG.warn("Coverage file does not contain valid data.");
        return null;
      }

      for (Map.Entry<String, SortedMap<Integer, Integer>> entry : data.getMergedDartFileCoverageData().entrySet()) {
        ProgressManager.checkCanceled();

        String filePath = getFileForUri(project, contextId, entry.getKey());
        if (filePath == null) {
          // File is not found.
          continue;
        }
        SortedMap<Integer, Integer> lineHits = entry.getValue();
        ClassData classData = projectData.getOrCreateClassData(filePath);
        if (lineHits.size() == 0) {
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
    catch (FileNotFoundException | JsonSyntaxException e) {
      LOG.warn(e);
    }
    finally {
      DartAnalysisServerService.getInstance(project).execution_deleteContext(contextId);
    }

    return projectData;
  }

  @Nullable
  private static String getFileForUri(@NotNull final Project project, @NotNull final String contextId, @NotNull final String uri) {
    if (uri.startsWith("dart:_") || uri.startsWith("dart:") && uri.contains("-patch/")) {
      // dart:_builtin or dart:core-patch/core_patch.dart
      return null;
    }

    return DartAnalysisServerService.getInstance(project).execution_mapUri(contextId, null, uri);
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return "Dart";
  }

  @NotNull
  @Override
  public String getId() {
    return ID;
  }

  @NotNull
  @Override
  public String getDataFileExtension() {
    return "json";
  }

  @Override
  public boolean acceptsCoverageEngine(@NotNull CoverageEngine engine) {
    return engine instanceof DartCoverageEngine;
  }
}
