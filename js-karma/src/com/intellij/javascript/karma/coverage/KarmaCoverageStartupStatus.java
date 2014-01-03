package com.intellij.javascript.karma.coverage;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoverageStartupStatus {

  private final boolean myCoveragePreprocessorSpecifiedInConfig;
  private final boolean myCoverageReportFound;
  private final boolean myCoveragePluginInstalled;

  public KarmaCoverageStartupStatus(boolean coveragePreprocessorSpecifiedInConfig,
                                    boolean coverageReportFound,
                                    boolean coveragePluginInstalled) {
    myCoveragePreprocessorSpecifiedInConfig = coveragePreprocessorSpecifiedInConfig;
    myCoverageReportFound = coverageReportFound;
    myCoveragePluginInstalled = coveragePluginInstalled;
  }

  public boolean isCoveragePreprocessorSpecifiedInConfig() {
    return myCoveragePreprocessorSpecifiedInConfig;
  }

  public boolean isCoverageReportFound() {
    return myCoverageReportFound;
  }

  public boolean isSuccessful() {
    return myCoveragePreprocessorSpecifiedInConfig && myCoverageReportFound;
  }

  public boolean isKarmaCoveragePackageNeededToBeInstalled() {
    return !myCoverageReportFound && !myCoveragePluginInstalled;
  }

}
