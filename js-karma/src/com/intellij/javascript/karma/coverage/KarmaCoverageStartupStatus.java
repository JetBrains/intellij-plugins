package com.intellij.javascript.karma.coverage;

/**
 * @author Sergey Simonchik
 */
public class KarmaCoverageStartupStatus {

  private final boolean myCoverageReporterSpecifiedInConfig;
  private final boolean myCoveragePreprocessorSpecifiedInConfig;
  private final boolean myCoverageReportFound;
  private final boolean myCoveragePluginInstalled;

  public KarmaCoverageStartupStatus(boolean coverageReporterSpecifiedInConfig,
                                    boolean coveragePreprocessorSpecifiedInConfig,
                                    boolean coverageReportFound,
                                    boolean coveragePluginInstalled) {
    myCoverageReporterSpecifiedInConfig = coverageReporterSpecifiedInConfig;
    myCoveragePreprocessorSpecifiedInConfig = coveragePreprocessorSpecifiedInConfig;
    myCoverageReportFound = coverageReportFound;
    myCoveragePluginInstalled = coveragePluginInstalled;
  }

  public boolean isCoverageReporterSpecifiedInConfig() {
    return myCoverageReporterSpecifiedInConfig;
  }

  public boolean isCoveragePreprocessorNeededToBeSpecified() {
    return myCoverageReporterSpecifiedInConfig && !myCoveragePreprocessorSpecifiedInConfig;
  }

  public boolean isCoverageReportFound() {
    return myCoverageReportFound;
  }

  public boolean isSuccessful() {
    return myCoverageReporterSpecifiedInConfig && myCoveragePreprocessorSpecifiedInConfig && myCoverageReportFound;
  }

  public boolean isKarmaCoveragePackageNeededToBeInstalled() {
    return myCoverageReporterSpecifiedInConfig && (!myCoverageReportFound && !myCoveragePluginInstalled);
  }

}
