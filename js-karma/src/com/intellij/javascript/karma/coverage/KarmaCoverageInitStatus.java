package com.intellij.javascript.karma.coverage;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoverageInitStatus {

  private final boolean myCoverageReportFound;
  private final boolean myCoveragePluginInstalled;

  public KarmaCoverageInitStatus(boolean coverageReportFound, boolean coveragePluginInstalled) {
    myCoverageReportFound = coverageReportFound;
    myCoveragePluginInstalled = coveragePluginInstalled;
  }

  public boolean isCoverageReportFound() {
    return myCoverageReportFound;
  }

  public boolean isCoveragePluginInstalled() {
    return myCoveragePluginInstalled;
  }
}
