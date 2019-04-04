package com.google.jstestdriver.idea.rt.execution;

import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.idea.rt.execution.TestPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class TestPathFactory {

  private final String myJstdConfigFileAbsolutePath;

  public TestPathFactory(@NotNull File jstdConfigFile) {
    myJstdConfigFileAbsolutePath = jstdConfigFile.getAbsolutePath();
  }

  @NotNull
  public TestPath createTestPath(@NotNull BrowserInfo browser,
                                 @Nullable File jsTestFile,
                                 @NotNull String testCaseName,
                                 @NotNull String testName) {
    return new TestPath(myJstdConfigFileAbsolutePath, browser, jsTestFile, testCaseName, testName);
  }

  @NotNull
  public TestPath createTestPath(@NotNull TestResult testResult) {
    return new TestPath(myJstdConfigFileAbsolutePath,
                        testResult.getBrowserInfo(),
                        null,
                        testResult.getTestCaseName(),
                        testResult.getTestName());
  }

}
