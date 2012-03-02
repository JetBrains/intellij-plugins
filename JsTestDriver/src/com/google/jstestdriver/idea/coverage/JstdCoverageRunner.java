package com.google.jstestdriver.idea.coverage;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageSuite;
import com.intellij.coverage.SimpleCoverageAnnotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.rt.coverage.data.ClassData;
import com.intellij.rt.coverage.data.LineData;
import com.intellij.rt.coverage.data.ProjectData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JstdCoverageRunner extends CoverageRunner {

  private static final Logger LOG = Logger.getInstance(JstdCoverageRunner.class);

  @Override
  public ProjectData loadCoverageData(@NotNull File sessionDataFile, @Nullable CoverageSuite baseCoverageSuite) {
    try {
      return readProjectData(sessionDataFile);
    }
    catch (Exception e) {
      LOG.warn("Can't read coverage data", e);
      return new ProjectData();
    }
  }

  @NotNull
  private static ProjectData readProjectData(@NotNull File dataFile) throws IOException {
    final String SOURCE_FILE_PREFIX = "SF:";
    final String LINE_HIT_PREFIX = "DA:";
    final String END_OF_RECORD = "end_of_record";
    BufferedReader reader = new BufferedReader(new FileReader(dataFile));
    try {
      ProjectData projectData = new ProjectData();
      String currentFileName = null;
      String line;
      List<LineData> lineDataList = null;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(SOURCE_FILE_PREFIX)) {
          currentFileName = line.substring(SOURCE_FILE_PREFIX.length());
          lineDataList = Lists.newArrayList();
        }
        else if (line.startsWith(LINE_HIT_PREFIX)) {
          if (lineDataList == null) {
            throw new RuntimeException("lineDataList is null!");
          }
          String[] values = line.substring(LINE_HIT_PREFIX.length()).split(",");
          Preconditions.checkState(values.length == 2);
          int lineNum = Integer.parseInt(values[0]);
          int hitCount = Integer.parseInt(values[1]);
          LineData lineData = new LineData(lineNum, null);
          lineData.setHits(hitCount);
          lineDataList.add(lineData);
        }
        else if (END_OF_RECORD.equals(line)) {
          if (lineDataList == null) {
            throw new RuntimeException("lineDataList is null!");
          }
          Preconditions.checkNotNull(currentFileName);
          addRecords(projectData, currentFileName, lineDataList);
          currentFileName = null;
          lineDataList = null;
        }
      }
      Preconditions.checkState(lineDataList == null && currentFileName == null);
      return projectData;
    } finally {
      reader.close();
    }
  }

  private static void addRecords(@NotNull ProjectData projectData,
                                 @NotNull String fileName,
                                 @NotNull List<LineData> lineDataList) {
    ClassData classData = projectData.getOrCreateClassData(SimpleCoverageAnnotator.getFilePath(fileName));
    int max = 0;
    for (LineData ld : lineDataList) {
      max = Math.max(max, ld.getLineNumber());
    }
    final LineData[] lines = new LineData[max + 1];
    for (LineData lineData : lineDataList) {
      lines[lineData.getLineNumber()] = lineData;
    }
    classData.setLines(lines);
  }

  @Override
  public String getPresentableName() {
    return "JstdPresentableName";
  }

  @Override
  public String getId() {
    return JstdCoverageEngine.ID;
  }

  @Override
  public String getDataFileExtension() {
    return "dat";
  }

  @Override
  public boolean acceptsCoverageEngine(@NotNull CoverageEngine engine) {
    return engine instanceof JstdCoverageEngine;
  }
}
