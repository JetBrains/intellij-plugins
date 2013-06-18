package com.intellij.javascript.karma.coverage;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public abstract class KarmaCoverageSession {

  private final String myCoverageFilePath;

  public KarmaCoverageSession(@NotNull String coverageFilePath) {
    myCoverageFilePath = coverageFilePath;
  }

  @NotNull
  public String getCoverageFilePath() {
    return myCoverageFilePath;
  }

  public abstract void onCoverageSessionFinished();

}
