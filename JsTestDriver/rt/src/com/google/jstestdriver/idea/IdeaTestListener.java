package com.google.jstestdriver.idea;

import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.FileResult;
import com.google.jstestdriver.TestCase;
import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.hooks.TestListener;
import com.google.jstestdriver.idea.execution.tree.TestResultProtocolMessage;
import com.google.jstestdriver.idea.util.ConsoleObjectOutput;
import com.google.jstestdriver.idea.util.JstdConfigParsingUtils;
import com.google.jstestdriver.model.BasePaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;

/**
 * @author Sergey Simonchik
 */
public class IdeaTestListener implements TestListener {

  private static final String PREFIX = "/test/";

  private final ObjectOutput myTestResultProtocolMessageOutput;
  private final File myJstdConfigFile;
  private final File myBasePath;

  public IdeaTestListener(@NotNull ObjectOutput testResultProtocolMessageOutput,
                          @NotNull File jstdConfigFile,
                          @NotNull BasePaths basePaths) {
    myTestResultProtocolMessageOutput = testResultProtocolMessageOutput;
    myJstdConfigFile = jstdConfigFile;
    myBasePath = JstdConfigParsingUtils.getSingleBasePath(basePaths, jstdConfigFile);
  }

  @Override
  public void onTestComplete(TestResult testResult) {
    TestResultProtocolMessage message = TestResultProtocolMessage.fromTestResult(myJstdConfigFile, testResult);
    writeTestResultProtocolMessage(message);
  }

  @Override
  public void onFileLoad(BrowserInfo browserInfo, FileResult fileResult) {
  }

  @Override
  public void onTestRegistered(BrowserInfo browser, TestCase testCase) {
    for (String testName : testCase.getTests()) {
      File jsTestFile = resolveTestFile(testCase.getFileName());
      TestResultProtocolMessage message = TestResultProtocolMessage.fromDryRun(
          myJstdConfigFile, browser, jsTestFile, testCase.getName(), testName
      );
      writeTestResultProtocolMessage(message);
    }
  }

  @Override
  public void finish() {
  }

  private void writeTestResultProtocolMessage(@NotNull TestResultProtocolMessage message) {
    try {
      synchronized (myTestResultProtocolMessageOutput) {
        myTestResultProtocolMessageOutput.writeObject(message);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
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
