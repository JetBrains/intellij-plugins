package com.google.jstestdriver.idea;

import com.google.jstestdriver.*;
import com.google.jstestdriver.hooks.TestListener;
import com.google.jstestdriver.idea.execution.TestPath;
import com.google.jstestdriver.idea.execution.TestPathFactory;
import com.google.jstestdriver.idea.execution.tree.TreeManager;
import com.google.jstestdriver.idea.util.TestFileScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class IdeaTestListener implements TestListener {

  private static final String PREFIX = "/test/";

  private final TreeManager myTreeManager;
  private final File myBasePath;
  private final Object MONITOR = new Object();
  private final boolean myDryRun;
  private final TestFileScope myTestFileScope;
  private final TestPathFactory myTestPathFactory;

  public IdeaTestListener(@NotNull TreeManager treeManager,
                          @NotNull File jstdConfigFile,
                          @NotNull File singleBasePath,
                          boolean dryRun,
                          @NotNull TestFileScope testFileScope) {
    myTreeManager = treeManager;
    myBasePath = singleBasePath;
    myDryRun = dryRun;
    myTestFileScope = testFileScope;
    myTestPathFactory = new TestPathFactory(jstdConfigFile);
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
      String testCaseName = testCase.getName();
      if (!myTestFileScope.containsTestCase(testCaseName)) {
        return;
      }
      for (String testName : testCase.getTests()) {
        if (!myTestFileScope.containsTestCaseAndMethod(testCaseName, testName)) {
          continue;
        }
        File jsTestFile = resolveTestFile(testCase.getFileName());
        TestPath testPath = myTestPathFactory.createTestPath(
          browser,
          jsTestFile,
          testCase.getName(),
          testName
        );
        myTreeManager.onTestRegistered(testPath);
      }
    }
  }

  @Override
  public void onTestComplete(TestResult testResult) {
    synchronized (MONITOR) {
      TestPath testPath = myTestPathFactory.createTestPath(testResult);
      myTreeManager.onTestCompleted(testPath, testResult);
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
