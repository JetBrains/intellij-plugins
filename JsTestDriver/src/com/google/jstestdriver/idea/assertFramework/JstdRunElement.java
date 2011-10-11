package com.google.jstestdriver.idea.assertFramework;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class JstdRunElement {

  private final String myTestCaseName;
  private final String myTestMethodName;

  private JstdRunElement(@NotNull String testCaseName, @Nullable String testMethodName) {
    myTestCaseName = testCaseName;
    myTestMethodName = testMethodName;
  }

  @NotNull
  public String getTestCaseName() {
    return myTestCaseName;
  }

  @Nullable
  public String getTestMethodName() {
    return myTestMethodName;
  }

  public static JstdRunElement newTestCaseRunElement(@NotNull String testCaseName) {
    return new JstdRunElement(testCaseName, null);
  }

  public static JstdRunElement newTestMethodRunElement(@NotNull String testCaseName, @NotNull String testMethodName) {
    return new JstdRunElement(testCaseName, testMethodName);
  }
}
