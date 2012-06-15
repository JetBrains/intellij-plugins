package com.google.jstestdriver.idea;

import com.google.jstestdriver.*;
import com.google.jstestdriver.hooks.TestListener;
import com.google.jstestdriver.idea.execution.tree.TestResultProtocolMessage;
import com.google.jstestdriver.idea.execution.tree.TreeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class IdeaTestListener implements TestListener {

  private static final String PREFIX = "/test/";

  private final TreeManager myTreeManager;
  private final File myJstdConfigFile;
  private final File myBasePath;
  private final Object MONITOR = new Object();
  private final boolean myDryRun;

  public IdeaTestListener(
    @NotNull TreeManager treeManager,
    @NotNull File jstdConfigFile,
    @NotNull File singleBasePath,
    boolean dryRun
  ) {
    myTreeManager = treeManager;
    myJstdConfigFile = jstdConfigFile;
    myBasePath = singleBasePath;
    myDryRun = dryRun;
  }

  @Override
  public void onFileLoad(BrowserInfo browserInfo, FileResult fileResult) {
    if (!fileResult.isSuccess() && !myDryRun) {
      synchronized (MONITOR) {
        FileSource jsFileSource = fileResult.getFileSource();
        String jsFilePath = jsFileSource != null ? jsFileSource.getBasePath() : null;
        myTreeManager.onFileLoadError(browserInfo.toString(), jsFilePath, fileResult.getMessage());
      }
    }
  }

  @Override
  public void onTestRegistered(BrowserInfo browser, TestCase testCase) {
    synchronized (MONITOR) {
      for (String testName : testCase.getTests()) {
        File jsTestFile = resolveTestFile(testCase.getFileName());
        TestResultProtocolMessage message = TestResultProtocolMessage.fromDryRun(
          myJstdConfigFile, browser, jsTestFile, testCase.getName(), testName
        );
        myTreeManager.onTestRegistered(message);
      }
    }
  }

  @Override
  public void onTestComplete(TestResult testResult) {
    synchronized (MONITOR) {
      TestResultProtocolMessage message = TestResultProtocolMessage.fromTestResult(myJstdConfigFile, testResult);
      myTreeManager.onTestCompleted(message);
    }
  }

  @Override
  public void finish() {
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
