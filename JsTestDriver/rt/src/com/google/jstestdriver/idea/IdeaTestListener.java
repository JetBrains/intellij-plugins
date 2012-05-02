package com.google.jstestdriver.idea;

import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.FileResult;
import com.google.jstestdriver.TestCase;
import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.hooks.TestListener;
import com.google.jstestdriver.idea.execution.tree.OutputManager;
import com.google.jstestdriver.idea.execution.tree.TestResultProtocolMessage;
import com.google.jstestdriver.idea.util.JstdConfigParsingUtils;
import com.google.jstestdriver.model.BasePaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class IdeaTestListener implements TestListener {

  private static final String PREFIX = "/test/";

  private final OutputManager myOutputManager;
  private final File myJstdConfigFile;
  private final File myBasePath;
  private final Object MONITOR = new Object();

  public IdeaTestListener(
    @NotNull OutputManager outputManager,
    @NotNull File jstdConfigFile,
    @NotNull BasePaths basePaths
  ) {
    myOutputManager = outputManager;
    myJstdConfigFile = jstdConfigFile;
    myBasePath = JstdConfigParsingUtils.getSingleBasePath(basePaths, jstdConfigFile);
  }

  @Override
  public void onFileLoad(BrowserInfo browserInfo, FileResult fileResult) {
  }

  @Override
  public void onTestRegistered(BrowserInfo browser, TestCase testCase) {
    synchronized (MONITOR) {
      for (String testName : testCase.getTests()) {
        File jsTestFile = resolveTestFile(testCase.getFileName());
        TestResultProtocolMessage message = TestResultProtocolMessage.fromDryRun(
          myJstdConfigFile, browser, jsTestFile, testCase.getName(), testName
        );
        myOutputManager.onTestRegistered(message);
      }
    }
  }

  @Override
  public void onTestComplete(TestResult testResult) {
    synchronized (MONITOR) {
      TestResultProtocolMessage message = TestResultProtocolMessage.fromTestResult(myJstdConfigFile, testResult);
      myOutputManager.onTestCompleted(message);
    }
  }

  @Override
  public void finish() {
    synchronized (MONITOR) {
      myOutputManager.finish();
    }
  }

  @Nullable
  private File resolveTestFile(@Nullable String jsTestFilePath) {
    if (jsTestFilePath == null) {
      return null;
    }
    if (jsTestFilePath.startsWith(PREFIX)) {
      String filePath = jsTestFilePath.substring(PREFIX.length());
      File resolved = doResolveTestFile(filePath);
      if (resolved != null) {
        return resolved;
      }
    }
    return doResolveTestFile(jsTestFilePath);
  }

  @Nullable
  private File doResolveTestFile(@NotNull String filePath) {
    File absoluteFile = new File(filePath);
    if (absoluteFile.isAbsolute() && absoluteFile.isFile()) {
      return absoluteFile;
    }
    File localFile = new File(myBasePath, filePath);
    if (localFile.isFile()) {
      return localFile;
    }
    return null;
  }

}
