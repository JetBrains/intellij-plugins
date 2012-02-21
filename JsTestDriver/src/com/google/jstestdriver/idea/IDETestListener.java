package com.google.jstestdriver.idea;

import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.FileResult;
import com.google.jstestdriver.TestCase;
import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.hooks.TestListener;
import com.google.jstestdriver.idea.execution.tree.TestResultProtocolMessage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;

/**
 * @author Sergey Simonchik
 */
public class IDETestListener implements TestListener {

  private final ObjectOutput myTestResultProtocolMessageOutput;
  private final File myJstdConfigFile;

  public IDETestListener(@NotNull ObjectOutput testResultProtocolMessageOutput,
                         @NotNull File jstdConfigFile) {
    myTestResultProtocolMessageOutput = testResultProtocolMessageOutput;
    myJstdConfigFile = jstdConfigFile;
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
      TestResultProtocolMessage message = TestResultProtocolMessage.fromDryRun(
          myJstdConfigFile, browser, testCase.getName(), testName
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

}
