/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.execution.tree;

import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.config.JstdConfigStructure;
import com.google.jstestdriver.idea.execution.TestListenerContext;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMTestRunnerResultsForm;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import static com.google.jstestdriver.TestResult.Result;

/**
 * Updates the Test Result UI panel in the IDE with Test Results. The results are streaming from the JSTD runner, so
 * we update the UI as each event arrives, to give better user feedback.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class RemoteTestListener {

  private final TestListenerContext myContext;
  private final Map<String, BrowserNode> browserMap = Maps.newHashMap();
  private final VirtualFile myDirectory;
  private final StringBuilder myRootNodeLog = new StringBuilder();
  private final MultiMap<String, TestNode> myRunningTestByFullNameMap = new MultiMap<String, TestNode>();
  private Node myLastTestCaseParentNode;
  private File myLastConfigFile;

  public RemoteTestListener(@NotNull TestListenerContext ctx, @Nullable VirtualFile directory) {
    myContext = ctx;
    myDirectory = directory;
  }

  // This method must only be called on the AWT event thread, as it updates the UI.
  public void onTestStarted(@NotNull TestResultProtocolMessage message) {
    TestNode testNode = createTestNode(message);
    String fullTestName = message.getFullTestName();
    if (fullTestName != null) {
      myRunningTestByFullNameMap.putValue(message.getFullTestName(), testNode);
    }
  }

  private SMTestRunnerResultsForm getSMTestRunnerResultsForm() {
    return myContext.resultsForm();
  }

  private SMTestProxy.SMRootTestProxy getTestsRootNode() {
    return myContext.resultsForm().getTestsRootNode();
  }

  @NotNull
  private TestNode createTestNode(@NotNull TestResultProtocolMessage message) {
    BrowserNode browserNode = browserMap.get(message.browser);
    if (browserNode == null) {
      browserNode = new BrowserNode(message.browser);
      browserMap.put(message.browser, browserNode);
      onSuiteStarted(getTestsRootNode(), browserNode.getTestProxy());
    }

    JstdConfigFileNode jstdConfigFileNode = browserNode.getJstdConfigFileNodeByPath(message.jstdConfigFilePath);
    boolean fakeJstdConfigFileNode = myDirectory == null;
    boolean jstdConfigFileNodeAlreadyExists = jstdConfigFileNode != null;
    if (!jstdConfigFileNodeAlreadyExists) {
      jstdConfigFileNode = new JstdConfigFileNode(browserNode, myDirectory, message.jstdConfigFilePath, fakeJstdConfigFileNode);
      if (!fakeJstdConfigFileNode) {
        onSuiteStarted(browserNode.getTestProxy(), jstdConfigFileNode.getTestProxy());
      }
    }

    Node testCaseParentNode = fakeJstdConfigFileNode ? browserNode : jstdConfigFileNode;
    if (!jstdConfigFileNodeAlreadyExists) {
      JstdConfigStructure configStructure = JstdConfigStructure.newConfigStructure(jstdConfigFileNode.getConfigFile());
      StacktracePrinter stacktracePrinter = new StacktracePrinter(myContext.consoleView(), configStructure, message.browser);
      testCaseParentNode.wirePrinter(stacktracePrinter);
    }

    myLastTestCaseParentNode = testCaseParentNode;

    TestCaseNode testCaseNode = jstdConfigFileNode.getTestCaseNode(message.testCase);
    myLastConfigFile = jstdConfigFileNode.getConfigFile();
    if (testCaseNode == null) {
      testCaseNode = new TestCaseNode(jstdConfigFileNode, message.testCase);
      onSuiteStarted(testCaseParentNode.getTestProxy(), testCaseNode.getTestProxy());
    }

    TestNode testNode = testCaseNode.getTestByName(message.testName);
    if (testNode == null) {
      testNode = new TestNode(testCaseNode, message.testName);
      onTestStarted(testCaseNode.getTestProxy(), testNode.getTestProxy());
    }
    return testNode;
  }

  @Nullable
  private TestNode findTestNode(TestResultProtocolMessage message) {
    BrowserNode browserNode = browserMap.get(message.browser);
    if (browserNode == null) {
      return null;
    }
    JstdConfigFileNode jstdConfigFileNode = browserNode.getJstdConfigFileNodeByPath(message.jstdConfigFilePath);
    if (jstdConfigFileNode == null) {
      return null;
    }
    TestCaseNode testCaseNode = jstdConfigFileNode.getTestCaseNode(message.testCase);
    if (testCaseNode == null) {
      return null;
    }
    return testCaseNode.getTestByName(message.testName);
  }

  // This method must only be called on the AWT event thread, as it updates the UI.
  public void onTestFinished(final TestResultProtocolMessage message) {
    TestNode testNode = findTestNode(message);
    if (testNode == null) {
      String fullName = message.getFullTestName();
      final Iterator<TestNode> startedTestNodesIt = myRunningTestByFullNameMap.get(fullName).iterator();
      if (startedTestNodesIt.hasNext()) {
        TestNode startedTestNode = startedTestNodesIt.next();
        startedTestNodesIt.remove();

        // fixes problem when testCase or test name contains '.'
        startedTestNode.getTestProxy().setFixedName(message.testName);
        TestCaseNode startedTestCaseNode = startedTestNode.getTestCaseNode();
        startedTestCaseNode.getTestProxy().setFixedName(message.testCase);
        testNode = startedTestNode;
      } else {
        // jasmine adapter hack
        testNode = createTestNode(message);
      }
    }
    testNode.done();

    TestCaseNode testCaseNode = testNode.getTestCaseNode();
    JstdConfigFileNode jstdConfigFileNode = testCaseNode.getJstdConfigFileNode();
    BrowserNode browserNode = jstdConfigFileNode.getBrowserNode();

    SMTestProxy testProxy = testNode.getTestProxy();
    testProxy.addStdOutput(message.log, Key.create("result"));
    testProxy.setDuration(Math.round(message.duration));
    Result result = Result.valueOf(message.result);
    if (result == Result.passed) {
      onTestFinished(testProxy);
    } else {
      final String stackStr;
      if (message.stack.startsWith(message.message)) {
        String s = message.stack.substring(message.message.length());
        stackStr = s.replaceFirst("^[\n\r]*", "");
      } else {
        stackStr = message.stack;
      }

      testProxy.setTestFailed(message.message, stackStr, result == Result.error);
      getSMTestRunnerResultsForm().onTestFailed(testProxy);
      testCaseNode.setTestFailed(result);
      jstdConfigFileNode.setTestFailed(result);
      browserNode.setTestFailed(result);
    }
    if (testCaseNode.isComplete()) {
      onSuiteFinished(testCaseNode.getTestProxy());
    }
    if (jstdConfigFileNode.isComplete()) {
      onSuiteFinished(jstdConfigFileNode.getTestProxy());
    }
    if (browserNode.isComplete()) {
      onSuiteFinished(browserNode.getTestProxy());
    }
  }

  private static void onSuiteStarted(SMTestProxy parent, SMTestProxy child) {
    parent.addChild(child);
  }

  public void onTestStarted(SMTestProxy parent, SMTestProxy child) {
    parent.addChild(child);
    getSMTestRunnerResultsForm().onTestStarted(child);
  }

  public static void onSuiteFinished(SMTestProxy node) {
    node.setFinished();
  }

  public void onTestFinished(SMTestProxy node) {
    node.setFinished();
    getSMTestRunnerResultsForm().onTestFinished(node);
  }

  public void onTestRunnerFailed(JstdTestRunnerFailure testRunnerFailure) {
    if (testRunnerFailure.getFailureType() == JstdTestRunnerFailure.FailureType.SINGLE_JSTD_CONFIG) {
      if (myLastConfigFile != null && myLastConfigFile.equals(new File(testRunnerFailure.getJstdConfigPath()))) {
        myLastTestCaseParentNode.getTestProxy().setTestFailed(testRunnerFailure.getMessage(), null, true);
      } else {
        SMTestProxy configNode = JstdConfigFileNode.createTestProxy(myDirectory, testRunnerFailure.getJstdConfigPath());
        getTestsRootNode().addChild(configNode);
        configNode.setTestFailed(testRunnerFailure.getMessage(), null, true);
      }
    } else {
      myRootNodeLog.append(testRunnerFailure.getMessage()).append("\n");
      getTestsRootNode().setTestFailed(myRootNodeLog.toString(), null, true);
    }
  }

}
