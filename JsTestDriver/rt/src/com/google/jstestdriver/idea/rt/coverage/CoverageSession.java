package com.google.jstestdriver.idea.rt.coverage;

import com.google.jstestdriver.idea.rt.coverage.CoverageReport;
import com.google.jstestdriver.idea.rt.coverage.CoverageSerializationUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Sergey Simonchik
 */
public class CoverageSession {

  private final File myIdeCoverageFile;
  private final CoverageReport myCoverageReport;

  public CoverageSession(@NotNull File ideCoverageFile) {
    myIdeCoverageFile = ideCoverageFile;
    myCoverageReport = new CoverageReport();
  }

  public void finish() {
    try {
      CoverageSerializationUtils.writeLCOV(myCoverageReport, myIdeCoverageFile);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void mergeReport(@NotNull CoverageReport report) {
    myCoverageReport.mergeReport(report);
  }
}
