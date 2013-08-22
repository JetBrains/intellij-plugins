package com.intellij.javascript.karma.coverage;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoverageStartupStatus {

  private final boolean myCoverageReporterSpecifiedInConfig;
  private final boolean myCoverageReportFound;
  private final boolean myCoveragePluginInstalled;

  public KarmaCoverageStartupStatus(boolean coverageReporterSpecifiedInConfig,
                                    boolean coverageReportFound,
                                    boolean coveragePluginInstalled) {
    myCoverageReporterSpecifiedInConfig = coverageReporterSpecifiedInConfig;
    myCoverageReportFound = coverageReportFound;
    myCoveragePluginInstalled = coveragePluginInstalled;
  }

  public boolean isCoverageReporterSpecifiedInConfig() {
    return myCoverageReporterSpecifiedInConfig;
  }

  public boolean isCoverageReportFound() {
    return myCoverageReportFound;
  }

  public boolean isCoveragePluginInstalled() {
    return myCoveragePluginInstalled;
  }
}
