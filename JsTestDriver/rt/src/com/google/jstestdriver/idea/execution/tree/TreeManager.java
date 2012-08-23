package com.google.jstestdriver.idea.execution.tree;

import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.idea.execution.TestPath;
import com.google.jstestdriver.idea.execution.tc.TC;
import com.google.jstestdriver.idea.execution.tc.TCAttribute;
import com.google.jstestdriver.idea.execution.tc.TCMessage;
import com.google.jstestdriver.idea.util.TestFileScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

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

  public void setCurrentBasePath(@NotNull String absoluteBasePath) {
    ConfigNode configNode = getCurrentConfigNode();
    configNode.setBasePath(absoluteBasePath);
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

  public void onTestRegistered(@NotNull TestPath message) {
    getOrCreateTestNode(message);
  }

  public void onTestCompleted(@NotNull TestPath testPath, @NotNull TestResult testResult) {
    TestNode testNode = getOrCreateTestNode(testPath);
    testNode.detachFromParent();
    String log = testResult.getLog();
    if (log != null && !log.isEmpty()) {
      TCMessage stdOutMessage = TC.newTestStdOutMessage(testNode);
      stdOutMessage.addAttribute(TCAttribute.STDOUT, log + "\n");
      printTCMessage(stdOutMessage);
    }

    int durationMillis = (int) testResult.getTime();
    TestResult.Result status = testResult.getResult();
    if (status == TestResult.Result.passed) {
      TCMessage testFinishedMessage = TC.newTestFinishedMessage(testNode);
      testFinishedMessage.addIntAttribute(TCAttribute.TEST_DURATION, durationMillis);
      printTCMessage(testFinishedMessage);
    } else {
      final String originalStack = testResult.getStack();
      final String parsedMessage = testResult.getParsedMessage();
      final String stackStr;
      if (originalStack.startsWith(parsedMessage)) {
        String s = originalStack.substring(parsedMessage.length());
        stackStr = s.replaceFirst("^[\n\r]*", "");
      } else {
        stackStr = originalStack;
      }
      TCMessage testFailedMessage = TC.newTestFailedMessage(testNode);
      testFailedMessage.addAttribute(TCAttribute.EXCEPTION_MESSAGE, parsedMessage);
      testFailedMessage.addAttribute(TCAttribute.EXCEPTION_STACKTRACE, stackStr);
      if (status == TestResult.Result.error) {
        testFailedMessage.addAttribute(TCAttribute.IS_TEST_ERROR, "yes");
      }
      testFailedMessage.addIntAttribute(TCAttribute.TEST_DURATION, durationMillis);
      printTCMessage(testFailedMessage);
    }
  }

  @NotNull
  private TestNode getOrCreateTestNode(@NotNull TestPath testPath) {
    ConfigNode configNode = getCurrentConfigNode();
    BrowserNode browserNode = configNode.findChildByName(testPath.getBrowserDisplayName());
    if (browserNode == null) {
      browserNode = new BrowserNode(testPath.getBrowserDisplayName(), configNode);
      configNode.addChild(browserNode);
    }

    TestCaseNode testCaseNode = browserNode.findChildByName(testPath.getTestCaseName());
    if (testCaseNode == null) {
      testCaseNode = new TestCaseNode(testPath.getTestCaseName(), testPath.getJsTestFileAbsolutePath(), browserNode);
      browserNode.addChild(testCaseNode);
    }

    TestNode testNode = testCaseNode.findChildByName(testPath.getTestName());
    if (testNode == null) {
      testNode = new TestNode(testPath.getTestName(), testCaseNode);
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

  public void onJstdConfigRunningFinished(@Nullable Exception testsRunException, @NotNull TestFileScope testFileScope) {
    ConfigNode configNode = getCurrentConfigNode();
    for (BrowserNode browserNode : configNode.getChildren()) {
      for (TestCaseNode testCaseNode : browserNode.getChildren()) {
        for (TestNode testNode : testCaseNode.getChildren()) {
          TCMessage testFailedMessage = TC.newTestFailedMessage(testNode);
          String reason = testsRunException != null ? "JsTestDriver crash" : "unknown reason";
          testFailedMessage.addAttribute(TCAttribute.EXCEPTION_MESSAGE, "Can't execute test due to " + reason + ".");
          testFailedMessage.addAttribute(TCAttribute.IS_TEST_ERROR, "yes");
          printTCMessage(testFailedMessage);
        }
        TCMessage testCaseFinishedMessage = TC.newTestSuiteFinishedMessage(testCaseNode);
        printTCMessage(testCaseFinishedMessage);
      }
      TCMessage browserFinishedMessage = TC.newTestSuiteFinishedMessage(browserNode);
      printTCMessage(browserFinishedMessage);
    }
    if (testsRunException != null) {
      ConfigErrorNode configErrorNode = new ConfigErrorNode(configNode);
      TCMessage startedMessage = configErrorNode.createStartedMessage();
      printTCMessage(startedMessage);
      TCMessage finishedMessage = TC.newConfigErrorFinishedMessage(configErrorNode);
      String fullMessage = formatMessage(testsRunException.getMessage(), testsRunException.getCause());
      finishedMessage.addAttribute(TCAttribute.EXCEPTION_MESSAGE, fullMessage);
      printTCMessage(finishedMessage);
    }
    else if (configNode.getChildren().isEmpty()) {
      final String message;
      Map.Entry<String, Set<String>> testCaseEntry = testFileScope.getSingleTestCaseEntry();
      if (testCaseEntry != null) {
        Set<String> testMethodNames = testCaseEntry.getValue();
        if (testMethodNames.isEmpty()) {
          message = "No '" + testCaseEntry.getKey() + "' test case found or it has no tests.";
        }
        else {
          message = "No '" + testFileScope.humanize() + "' test method found.";
        }
      }
      else {
        message = "No tests found. Please check 'test:' section of the configuration file.";
      }
      myErrStream.println(message);
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
    TCMessage startedMessage = rootErrorNode.createStartedMessage();
    printTCMessage(startedMessage);
    TCMessage finishedMessage = TC.newRootErrorFinishedMessage(rootErrorNode);
    finishedMessage.addAttribute(TCAttribute.EXCEPTION_MESSAGE, message);
    printTCMessage(finishedMessage);
  }

  public void onFileLoadError(@NotNull String browserName,
                              @Nullable String pathToJsFileWithError,
                              @Nullable String errorMessage) {
    ConfigNode configNode = getCurrentConfigNode();
    BrowserNode browserNode = configNode.findChildByName(browserName);
    if (browserNode == null) {
      browserNode = new BrowserNode(browserName, configNode);
      configNode.addChild(browserNode);
    }
    BrowserErrorNode browserErrorNode = BrowserErrorNode.newBrowserErrorNode(browserNode, pathToJsFileWithError, errorMessage);
    TCMessage startedMessage = browserErrorNode.createStartedMessage();
    printTCMessage(startedMessage);

    TCMessage finishedMessage = TC.newBrowserErrorFinishedMessage(browserErrorNode);
    if (errorMessage != null) {
      finishedMessage.addAttribute(TCAttribute.EXCEPTION_MESSAGE, errorMessage);
    }
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
