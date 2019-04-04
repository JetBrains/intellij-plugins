package com.google.jstestdriver.idea.rt.execution;

import com.google.jstestdriver.BrowserInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class TestPath {

  private final String myJstdConfigFileAbsolutePath;
  private final String myBrowserDisplayName;
  private final String myJsTestFileAbsolutePath;
  private final String myTestCaseName;
  private final String myTestName;

  public TestPath(@NotNull String jstdConfigFileAbsolutePath,
                  @NotNull BrowserInfo browserInfo,
                  @Nullable File jsTestFile,
                  @NotNull String testCaseName,
                  @NotNull String testName) {
    myJstdConfigFileAbsolutePath = jstdConfigFileAbsolutePath;
    myBrowserDisplayName = browserInfo.toString();
    myJsTestFileAbsolutePath = jsTestFile != null ? jsTestFile.getAbsolutePath() : null;
    myTestCaseName = testCaseName;
    myTestName = testName;
  }

  @NotNull
  public String getJstdConfigFileAbsolutePath() {
    return myJstdConfigFileAbsolutePath;
  }

  @NotNull
  public String getBrowserDisplayName() {
    return myBrowserDisplayName;
  }

  @Nullable
  public String getJsTestFileAbsolutePath() {
    return myJsTestFileAbsolutePath;
  }

  @NotNull
  public String getTestCaseName() {
    return myTestCaseName;
  }

  @NotNull
  public String getTestName() {
    return myTestName;
  }
}
