package com.google.jstestdriver.idea.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class TestFileScope {

  private final String myTestCaseName;
  private final String myTestMethodName;

  public TestFileScope(@Nullable String testCaseName, @Nullable String testMethodName) {
    myTestCaseName = testCaseName;
    myTestMethodName = testCaseName == null ? null : testMethodName;
  }

  @Nullable
  public String getTestCaseName() {
    return myTestCaseName;
  }

  @Nullable
  public String getTestMethodName() {
    return myTestMethodName;
  }

  @NotNull
  public String toJstdScope() {
    if (myTestCaseName == null) {
      return "all";
    }
    if (myTestMethodName == null) {
      return myTestCaseName;
    }
    return myTestCaseName + "." + myTestMethodName;
  }
}
