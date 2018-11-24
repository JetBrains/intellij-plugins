package com.google.jstestdriver.idea.rt;

import com.google.jstestdriver.*;
import com.google.jstestdriver.hooks.TestListener;
import com.google.jstestdriver.idea.rt.execution.TestPath;
import com.google.jstestdriver.idea.rt.execution.TestPathFactory;
import com.google.jstestdriver.idea.rt.execution.tree.TreeManager;
import com.google.jstestdriver.idea.rt.util.TestFileScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

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
  private final Set<FileLoadError> myDryRunErrors = new HashSet<>();

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
    if (fileResult.isSuccess()) {
      return;
    }
    synchronized (MONITOR) {
      FileSource jsFileSource = fileResult.getFileSource();
      String jsFilePath = jsFileSource != null ? jsFileSource.getBasePath() : null;
      FileLoadError error = new FileLoadError(browserInfo, jsFilePath, fileResult.getMessage());
      final boolean reportError;
      if (myDryRun) {
        myDryRunErrors.add(error);
        reportError = true;
      }
      else {
        reportError = myDryRunErrors.contains(error);
      }
      if (reportError) {
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

  private static class FileLoadError {
    private final Long myBrowserId;
    private final String myJsFilePath;
    private final String myErrorMessage;

    private FileLoadError(@NotNull BrowserInfo browserInfo,
                          @Nullable String jsFilePath,
                          @Nullable String errorMessage) {
      myBrowserId = browserInfo.getId();
      myJsFilePath = jsFilePath;
      myErrorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      FileLoadError that = (FileLoadError)o;

      return equalsNullable(myBrowserId, that.myBrowserId) &&
             equalsNullable(myJsFilePath, that.myJsFilePath) &&
             equalsNullable(myErrorMessage, that.myErrorMessage);
    }

    public static <T> boolean equalsNullable(@Nullable T a, @Nullable T b) {
      return  a == null ? b == null : a.equals(b);
    }

    @Override
    public int hashCode() {
      int result = myBrowserId != null ? myBrowserId.hashCode() : 0;
      result = 31 * result + (myJsFilePath != null ? myJsFilePath.hashCode() : 0);
      result = 31 * result + (myErrorMessage != null ? myErrorMessage.hashCode() : 0);
      return result;
    }
  }
}
