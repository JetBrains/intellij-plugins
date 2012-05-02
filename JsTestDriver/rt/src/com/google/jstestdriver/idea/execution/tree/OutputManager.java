package com.google.jstestdriver.idea.execution.tree;

import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.idea.execution.tc.TC;
import com.google.jstestdriver.idea.execution.tc.TCAttribute;
import com.google.jstestdriver.idea.execution.tc.TCMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Sergey Simonchik
 */
public class OutputManager {

  private final PrintStream myOutStream;
  private final PrintStream myErrStream;
  private final RootNode myRootNode;

  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public OutputManager() {
    myOutStream = System.out;
    myErrStream = System.err;
    myRootNode = new RootNode("<internal root node>", myOutStream, myErrStream);
  }

  public void onTestRegistered(@NotNull TestResultProtocolMessage message) {
    getOrCreateTestNode(message);
  }

  public void onTestCompleted(@NotNull TestResultProtocolMessage message) {
    TestNode testNode = getOrCreateTestNode(message);
    if (message.log != null && !message.log.isEmpty()) {
      TCMessage stdOutMessage = TC.testStdOut(testNode.getName());
      stdOutMessage.addAttribute(TCAttribute.STDOUT, message.log + "\n");
      stdOutMessage.print(myOutStream);
    }

    TestResult.Result result = TestResult.Result.valueOf(message.result);
    if (result == TestResult.Result.passed) {
      TCMessage testFinishedMessage = TC.testFinished(testNode.getName());
      testFinishedMessage.addAttribute(TCAttribute.TEST_DURATION, Integer.toString((int) message.duration));
      testFinishedMessage.print(myOutStream);
    } else {
      final String stackStr;
      if (message.stack.startsWith(message.message)) {
        String s = message.stack.substring(message.message.length());
        stackStr = s.replaceFirst("^[\n\r]*", "");
      } else {
        stackStr = message.stack;
      }
      TCMessage testFailedMessage = TC.testFailed(testNode.getName());
      testFailedMessage.addAttribute(TCAttribute.EXCEPTION_MESSAGE, message.message);
      testFailedMessage.addAttribute(TCAttribute.EXCEPTION_STACKTRACE, stackStr);
      if (result == TestResult.Result.error) {
        testFailedMessage.addAttribute(TCAttribute.IS_TEST_ERROR, "yes");
      }
      testFailedMessage.addAttribute(TCAttribute.TEST_DURATION, Integer.toString((int) message.duration));
      testFailedMessage.print(myOutStream);
    }
  }

  @NotNull
  private TestNode getOrCreateTestNode(@NotNull TestResultProtocolMessage message) {
    BrowserNode browserNode = myRootNode.findChildByName(message.browser);
    if (browserNode == null) {
      browserNode = new BrowserNode(message.browser, myRootNode);
      myRootNode.addChild(browserNode);
    }

    TestCaseNode testCaseNode = browserNode.findChildByName(message.testCaseName);
    if (testCaseNode == null) {
      testCaseNode = new TestCaseNode(message.testCaseName, browserNode);
      browserNode.addChild(testCaseNode);
    }

    TestNode testNode = testCaseNode.findChildByName(message.testName);
    if (testNode == null) {
      testNode = new TestNode(message.testName, testCaseNode);
      testCaseNode.addChild(testNode);
    }

    return testNode;
  }

  @Nullable
  private TestNode getTestNode(@NotNull TestResultProtocolMessage message) {
    BrowserNode browserNode = myRootNode.findChildByName(message.browser);
    if (browserNode == null) {
      return null;
    }

    TestCaseNode testCaseNode = browserNode.findChildByName(message.testCaseName);
    if (testCaseNode == null) {
      return null;
    }

    TestNode testNode = testCaseNode.findChildByName(message.testName);
    if (testNode == null) {
      return null;
    }

    return testNode;
  }

  public void printErrorMessage(@NotNull String message) {
    myErrStream.println(message);
  }

  public void printThrowable(@NotNull String message, @NotNull Throwable t) {
    String fullMessage = formatMessage(message, t);
    myErrStream.println(fullMessage);
  }

  public void printThrowable(@NotNull Throwable t) {
    String message = formatMessage(null, t);
    myErrStream.println(message);
  }

  @NotNull
  public PrintStream getSystemOutStream() {
    return myOutStream;
  }

  public void finish() {
    for (BrowserNode browserNode : myRootNode.getChildren()) {
      for (TestCaseNode testCaseNode : browserNode.getChildren()) {
        TCMessage testCaseFinishedMessage = TC.testSuiteFinished(testCaseNode.getName());
        testCaseFinishedMessage.print(myOutStream);
      }
      TCMessage browserFinishedMessage = TC.testSuiteFinished(browserNode.getName());
      browserFinishedMessage.print(myOutStream);
    }
  }

  private static String formatMessage(@Nullable String message, @NotNull Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    try {
      t.printStackTrace(pw);
    } finally {
      pw.close();
    }
    if (message == null) {
      return sw.toString();
    } else {
      return message + "\n\n" + sw.toString();
    }
  }

}
