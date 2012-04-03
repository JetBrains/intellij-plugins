package com.google.jstestdriver.idea.coverage;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author Sergey Simonchik
 */
public class CoverageSerializationUtils {

  private static final String SOURCE_FILE_PREFIX = "SF:";
  private static final String LINE_HIT_PREFIX = "DA:";
  private static final String END_OF_RECORD = "end_of_record";

  public static CoverageReport readLCOV(@NotNull File file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    try {
      String currentFileName = null;
      String line;
      List<CoverageReport.LineHits> lineDataList = null;
      CoverageReport report = new CoverageReport();
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
          CoverageReport.LineHits lineHits = new CoverageReport.LineHits(lineNum, hitCount);
          lineDataList.add(lineHits);
        }
        else if (END_OF_RECORD.equals(line)) {
          if (lineDataList == null) {
            throw new RuntimeException("lineDataList is null!");
          }
          Preconditions.checkNotNull(currentFileName);
          report.mergeFileReport(currentFileName, lineDataList);
          currentFileName = null;
          lineDataList = null;
        }
      }
      Preconditions.checkState(lineDataList == null && currentFileName == null);
      return report;
    } finally {
      reader.close();
    }
  }

  public static void writeLCOV(@NotNull CoverageReport report, @NotNull File outputFile) throws IOException {
    PrintWriter out = new PrintWriter(outputFile);
    try {
      for (Map.Entry<String, List<CoverageReport.LineHits>> entry : report.getInfo().entrySet()) {
        out.print(SOURCE_FILE_PREFIX);
        out.println(entry.getKey());
        for (CoverageReport.LineHits lineHits : entry.getValue()) {
          out.print(LINE_HIT_PREFIX);
          out.print(lineHits.getLineNumber());
          out.print(',');
          out.println(lineHits.getHits());
        }
        out.println(END_OF_RECORD);
      }
    } finally {
      out.close();
    }
  }
}
