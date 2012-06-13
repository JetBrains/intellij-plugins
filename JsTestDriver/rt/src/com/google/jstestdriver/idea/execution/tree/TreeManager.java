package com.google.jstestdriver.idea.execution.tree;

import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.idea.execution.tc.TC;
import com.google.jstestdriver.idea.execution.tc.TCAttribute;
import com.google.jstestdriver.idea.execution.tc.TCMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Sergey Simonchik
 */
public class TreeManager {

  private final File myRunAllConfigsInDirectory;
  private final PrintStream myOutStream;
  private final PrintStream myErrStream;
  private final RootNode myRootNode;
  private ConfigNode myCurrentJstdConfigNode;
  private int myNextNodeId = 1;

  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public TreeManager(@Nullable File runAllConfigsInDirectory) {
    myRunAllConfigsInDirectory = runAllConfigsInDirectory;
    myOutStream = System.out;
    myErrStream = System.err;
    myRootNode = new RootNode(this);
  }

  public void onJstdConfigRunningStarted(@NotNull File jstdConfigFile) {
    String jstdConfigNodeDisplayName = buildJstdConfigDisplayName(jstdConfigFile);
    myCurrentJstdConfigNode = new ConfigNode(jstdConfigNodeDisplayName, jstdConfigFile, myRootNode);
    myRootNode.addChild(myCurrentJstdConfigNode);
  }

  private String buildJstdConfigDisplayName(@NotNull File jstdConfigFile) {
    String displayName = null;
    if (myRunAllConfigsInDirectory != null) {
      String directoryPath = myRunAllConfigsInDirectory.getAbsolutePath();
      String jstdConfigFilePath = jstdConfigFile.getAbsolutePath();
      if (jstdConfigFilePath.startsWith(directoryPath)) {
        displayName = jstdConfigFilePath.substring(directoryPath.length());
        if (displayName.startsWith("/") || displayName.startsWith("\\")) {
          displayName = displayName.substring(1);
        }
      }
    }
    if (displayName == null) {
      displayName = jstdConfigFile.getName();
    }
    return displayName;
  }

  public void onTestRegistered(@NotNull TestResultProtocolMessage message) {
    getOrCreateTestNode(message);
  }

  public void onTestCompleted(@NotNull TestResultProtocolMessage message) {
    TestNode testNode = getOrCreateTestNode(message);
    if (message.log != null && !message.log.isEmpty()) {
      TCMessage stdOutMessage = TC.newTestStdOutMessage(testNode);
      stdOutMessage.addAttribute(TCAttribute.STDOUT, message.log + "\n");
      printTCMessage(stdOutMessage);
    }

    TestResult.Result result = TestResult.Result.valueOf(message.result);
    if (result == TestResult.Result.passed) {
      TCMessage testFinishedMessage = TC.newTestFinishedMessage(testNode);
      testFinishedMessage.addIntAttribute(TCAttribute.TEST_DURATION, (int)message.duration);
      printTCMessage(testFinishedMessage);
    } else {
      final String stackStr;
      if (message.stack.startsWith(message.message)) {
        String s = message.stack.substring(message.message.length());
        stackStr = s.replaceFirst("^[\n\r]*", "");
      } else {
        stackStr = message.stack;
      }
      TCMessage testFailedMessage = TC.newTestFailedMessage(testNode);
      testFailedMessage.addAttribute(TCAttribute.EXCEPTION_MESSAGE, message.message);
      testFailedMessage.addAttribute(TCAttribute.EXCEPTION_STACKTRACE, stackStr);
      if (result == TestResult.Result.error) {
        testFailedMessage.addAttribute(TCAttribute.IS_TEST_ERROR, "yes");
      }
      testFailedMessage.addIntAttribute(TCAttribute.TEST_DURATION, (int)message.duration);
      printTCMessage(testFailedMessage);
    }
  }

  @NotNull
  private TestNode getOrCreateTestNode(@NotNull TestResultProtocolMessage message) {
    ConfigNode configNode = getCurrentConfigNode();
    BrowserNode browserNode = configNode.findChildByName(message.browser);
    if (browserNode == null) {
      browserNode = new BrowserNode(message.browser, configNode);
      configNode.addChild(browserNode);
    }

    TestCaseNode testCaseNode = browserNode.findChildByName(message.testCaseName);
    if (testCaseNode == null) {
      testCaseNode = new TestCaseNode(message.testCaseName, message.jsTestFilePath, browserNode);
      browserNode.addChild(testCaseNode);
    }

    TestNode testNode = testCaseNode.findChildByName(message.testName);
    if (testNode == null) {
      testNode = new TestNode(message.testName, testCaseNode);
      testCaseNode.addChild(testNode);
    }

    return testNode;
  }

  @NotNull
  private ConfigNode getCurrentConfigNode() {
    ConfigNode configNode = myCurrentJstdConfigNode;
    if (configNode == null) {
      throw new RuntimeException("Current " + ConfigNode.class.getSimpleName() + " is null!");
    }
    return configNode;
  }

  public void printThrowable(@NotNull String message, @NotNull Throwable t) {
    String fullMessage = formatMessage(message, t);
    myErrStream.println(fullMessage);
  }

  public void printThrowable(@NotNull Throwable t) {
    String message = formatStacktrace(t);
    myErrStream.println(message);
  }

  @NotNull
  public PrintStream getSystemOutStream() {
    return myOutStream;
  }

  public void onJstdConfigRunningFinished(@Nullable Exception testsRunException) {
    ConfigNode configNode = getCurrentConfigNode();
    for (BrowserNode browserNode : configNode.getChildren()) {
      for (TestCaseNode testCaseNode : browserNode.getChildren()) {
        TCMessage testCaseFinishedMessage = TC.newTestSuiteFinishedMessage(testCaseNode);
        printTCMessage(testCaseFinishedMessage);
      }
      TCMessage browserFinishedMessage = TC.newTestSuiteFinishedMessage(browserNode);
      printTCMessage(browserFinishedMessage);
    }
    if (testsRunException != null) {
      ConfigErrorNode configErrorNode = new ConfigErrorNode(configNode);
      TCMessage startedMessage = TC.newConfigErrorStartedMessage(configErrorNode);
      printTCMessage(startedMessage);
      TCMessage finishedMessage = TC.newConfigErrorFinishedMessage(configErrorNode);
      String fullMessage = formatMessage(testsRunException.getMessage(), testsRunException.getCause());
      finishedMessage.addAttribute(TCAttribute.EXCEPTION_MESSAGE, fullMessage);
      printTCMessage(finishedMessage);
    }
    TCMessage configFinishedMessage = TC.newTestSuiteFinishedMessage(configNode);
    printTCMessage(configFinishedMessage);
  }

  public void onTestingFinished() {
  }

  public int getNextNodeId() {
    return myNextNodeId++;
  }

  public void printTCMessage(@NotNull TCMessage message) {
    myOutStream.println(message.getText());
  }

  public void reportRootError(@NotNull String message) {
    RootErrorNode rootErrorNode = new RootErrorNode(myRootNode);
    TCMessage startedMessage = TC.newRootErrorStartedMessage(rootErrorNode);
    printTCMessage(startedMessage);
    TCMessage finishedMessage = TC.newRootErrorFinishedMessage(rootErrorNode);
    finishedMessage.addAttribute(TCAttribute.EXCEPTION_MESSAGE, message);
    printTCMessage(finishedMessage);
  }

  @NotNull
  private static String formatMessage(@NotNull String message, @Nullable Throwable t) {
    if (t == null) {
      return message;
    }
    String stacktrace = formatStacktrace(t);
    return message + "\n" + stacktrace;
  }

  @NotNull
  private static String formatStacktrace(@NotNull Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    try {
      t.printStackTrace(pw);
    } finally {
      pw.close();
    }
    return sw.toString();
  }

}
