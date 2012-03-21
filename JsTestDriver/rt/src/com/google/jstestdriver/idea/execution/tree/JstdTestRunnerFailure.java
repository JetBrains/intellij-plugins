package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;

public class JstdTestRunnerFailure implements Serializable {

  public enum FailureType {
    WHOLE_TEST_RUNNER, SINGLE_JSTD_CONFIG
  }

  private final FailureType myFailureType;
  private final String myMessage;
  private final String myJstdConfigPath;

  public JstdTestRunnerFailure(FailureType failureType, String message, File jstdConfigFile) {
    myFailureType = failureType;
    myMessage = message;
    myJstdConfigPath = jstdConfigFile == null ? null : jstdConfigFile.getAbsolutePath();
  }

  @NotNull
  public FailureType getFailureType() {
    return myFailureType;
  }

  public String getJstdConfigPath() {
    return myJstdConfigPath;
  }

  @NotNull
  public String getMessage() {
    return myMessage;
  }
}
