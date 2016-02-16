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
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageSuite;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
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
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;

public class DartCoverageRunner extends CoverageRunner {
  private static final Logger LOG = Logger.getInstance(DartCoverageRunner.class.getName());

  @Nullable
  @Override
  public ProjectData loadCoverageData(@NotNull File sessionDataFile, @Nullable CoverageSuite baseCoverageSuite) {
    if (baseCoverageSuite == null || !(baseCoverageSuite instanceof DartCoverageSuite)) {
      return null;
    }

    DartCoverageSuite coverageSuite = (DartCoverageSuite)baseCoverageSuite;
    VirtualFile contextFile = coverageSuite.getContextFile();
    if (contextFile == null) {
      return null;
    }
    String contextId = DartAnalysisServerService.getInstance().execution_createContext(contextFile.getPath());
    if (contextId == null) {
      return null;
    }

    try {
      for (int i = 0; i < 10; i++) {
        LOG.warn(new Date().getTime() + ":" + sessionDataFile.length() + "," + sessionDataFile.exists());
        if (sessionDataFile.length() > 0) {
          break;
        }
        try {
          Thread.sleep(100);
        }
        catch (InterruptedException e) {
          LOG.warn("Sleep interrupted.");
        }
      }

      DartCoverageData data = new Gson().fromJson(new BufferedReader(new FileReader(sessionDataFile)), DartCoverageData.class);
      ProjectData projectData = new ProjectData();
      for (Map.Entry<String, SortedMap<Integer, Integer>> entry : data.getMergedDartFileCoverageData().entrySet()) {
        String filePath = getFileForUri(contextId, entry.getKey());
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
      return projectData;
    }
    catch (FileNotFoundException e) {
      LOG.warn(e);
      return null;
    }
    finally {
      DartAnalysisServerService.getInstance().execution_deleteContext(contextId);
    }
  }

  @Nullable
  private static String getFileForUri(@NotNull final String contextId, @NotNull final String uri) {
    if (uri.startsWith("dart:_") || uri.startsWith("dart:") && uri.contains("-patch/")) {
      // dart:_builtin or dart:core-patch/core_patch.dart
      return null;
    }

    return DartAnalysisServerService.getInstance().execution_mapUri(contextId, null, uri);
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return "Dart Coverage";
  }

  @NotNull
  @Override
  public String getId() {
    return DartCoverageEngine.ID;
  }

  @NotNull
  @Override
  public String getDataFileExtension() {
    return "coverage";
  }

  @Override
  public boolean acceptsCoverageEngine(@NotNull CoverageEngine engine) {
    return engine instanceof DartCoverageEngine;
  }
}
