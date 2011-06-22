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
import com.google.jstestdriver.idea.config.ConfigStructure;
import com.google.jstestdriver.idea.execution.TestListenerContext;
import com.google.jstestdriver.idea.javascript.navigation.NavigationRegistry;
import com.google.jstestdriver.idea.javascript.navigation.Test;
import com.google.jstestdriver.idea.javascript.navigation.TestCase;
import com.intellij.execution.Location;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMTestRunnerResultsForm;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

import static com.google.jstestdriver.TestResult.Result;

/**
 * Updates the Test Result UI panel in the IDE with Test Results. The results are streaming from the JSTD runner, so
 * we update the UI as each event arrives, to give better user feedback.
 * @author alexeagle@google.com (Alex Eagle)
 */
public class RemoteTestListener {
  private final TestListenerContext myContext;
  private final Map<String, BrowserNode> browserMap = Maps.newHashMap();
  private final Map<VirtualFile, NavigationRegistry> myNavigationRegistryMap;
  private final VirtualFile myDirectory;
  private final StringBuilder myRootNodeLog = new StringBuilder();
  private SMTestProxy myConfigLastTestProxy;
  private File myLastConfigFile;

  public RemoteTestListener(@NotNull Map<VirtualFile, NavigationRegistry> navigationRegistryMap,
                            @NotNull TestListenerContext ctx,
                            @Nullable VirtualFile directory) {
    myContext = ctx;
    myNavigationRegistryMap = navigationRegistryMap;
    myDirectory = directory;
  }

  // This method must only be called on the AWT event thread, as it updates the UI.
  public void onTestStarted(TestResultProtocolMessage message) {
    BrowserNode browserNode = browserMap.get(message.browser);
    if (browserNode == null) {
      browserNode = new BrowserNode(message.browser);
      browserMap.put(message.browser, browserNode);
      onSuiteStarted(myContext.resultsForm().getTestsRootNode(), browserNode.getTestProxy());
    }

    JstdConfigFileNode jstdConfigFileNode = browserNode.getJstdConfigFileNodeByPath(message.jstdConfigFilePath);
    if (jstdConfigFileNode == null) {
      jstdConfigFileNode = new JstdConfigFileNode(myDirectory, message.jstdConfigFilePath);
      browserNode.registerJstdConfigFileNode(jstdConfigFileNode);
      final Node jstdConfigNode;
      if (myDirectory != null) {
        jstdConfigNode = jstdConfigFileNode;
        onSuiteStarted(browserNode.getTestProxy(), jstdConfigFileNode.getTestProxy());
      } else {
        jstdConfigNode = browserNode;
      }
      File jstdConfigFile = new File(jstdConfigFileNode.getAbsoluteFilePath());
      ConfigStructure configStructure = ConfigStructure.newConfigStructure(jstdConfigFile);
      StacktracePrinter stacktracePrinter = new StacktracePrinter(myContext.consoleView(), configStructure, message.browser);
      jstdConfigNode.wirePrinter(stacktracePrinter);
    }
    NavigationRegistry navigationRegistry = myNavigationRegistryMap.get(jstdConfigFileNode.getVirtualFile());

    TestCase testCaseLocation = navigationRegistry == null ? null : navigationRegistry.getTestCaseByName(message.testCase);
    TestCaseNode testCaseNode = jstdConfigFileNode.getTestCaseNode(message.testCase);
    myConfigLastTestProxy = (myDirectory != null ? jstdConfigFileNode : browserNode).getTestProxy();
    myLastConfigFile = new File(jstdConfigFileNode.getAbsoluteFilePath());
    if (testCaseNode == null) {
      testCaseNode = new TestCaseNode(message.testCase, testCaseLocation);
      jstdConfigFileNode.registerTestCaseNode(testCaseNode);
      onSuiteStarted(myDirectory != null ? jstdConfigFileNode.getTestProxy() : browserNode.getTestProxy(), testCaseNode.getTestProxy());
    }

    Test testLocation = testCaseLocation != null ? testCaseLocation.getTestByName(message.testName) : null;
    Location testLoc = testLocation == null ? null :testLocation.getLocation();
    SMTestProxy testNode = testCaseNode.getTestByName(message.testName);
    if (testNode == null) {
      testNode = new SMTestProxyWithPrinterAndLocation(message.testName, false, LocationProvider.createConstantProvider(testLoc));
      testCaseNode.registerTestProxy(testNode);
      onSuiteStarted(testCaseNode.getTestProxy(), testNode);
    }
  }

  // This method must only be called on the AWT event thread, as it updates the UI.
  public void onTestFinished(TestResultProtocolMessage message) {
    BrowserNode browserNode = browserMap.get(message.browser);
    JstdConfigFileNode jstdConfigFileNode = browserNode.getJstdConfigFileNodeByPath(message.jstdConfigFilePath);
    TestCaseNode testCaseNode = jstdConfigFileNode.getTestCaseNode(message.testCase);
    if (testCaseNode.isTestDone(message.testName)) {
      return;
    }
    testCaseNode.testDone(message.testName);
    SMTestProxy testNode = testCaseNode.getTestByName(message.testName);
    testNode.addStdOutput(message.log, Key.create("result"));
    testNode.setDuration(Math.round(message.duration));
    Result result = Result.valueOf(message.result);
    if (result == Result.passed) {
      testNode.setFinished();
      myContext.resultsForm().onTestFinished(testNode);
    } else {
      boolean isError = result == Result.error;
      final String stackStr;
      if (message.stack.startsWith(message.message)) {
        String s = message.stack.substring(message.message.length());
        stackStr = s.replaceFirst("^[\n\r]*", "");
      } else {
        stackStr = message.stack;
      }
      testNode.setTestFailed(message.message, stackStr, isError);
      myContext.resultsForm().onTestFailed(testNode);
      testCaseNode.setTestFailed(result);
      jstdConfigFileNode.setTestFailed(result);
      browserNode.setTestFailed(result);
    }
    if (testCaseNode.allTestsComplete()) {
      onSuiteFinished(testCaseNode.getTestProxy());
    }
    if (jstdConfigFileNode.allTestCasesComplete()) {
      onSuiteFinished(jstdConfigFileNode.getTestProxy());
    }

    if (browserNode.allJstdConfigFilesComplete()) {
      onSuiteFinished(browserNode.getTestProxy());
    }
  }

  // This method must only be called on the AWT event thread, as it updates the UI.
  public void onSuiteFinished(SMTestProxy node) {
    node.setFinished();
    SMTestRunnerResultsForm form = myContext.resultsForm();
    form.onTestFinished(node);
  }

  // This method must only be called on the AWT event thread, as it updates the UI.
  public void onSuiteStarted(SMTestProxy parent, SMTestProxy node) {
    parent.addChild(node);
    SMTestRunnerResultsForm form = myContext.resultsForm();
    form.onTestStarted(node);
  }

  public void onTestRunnerFailed(JstdTestRunnerFailure testRunnerFailure) {
    if (testRunnerFailure.getFailureType() == JstdTestRunnerFailure.FailureType.SINGLE_JSTD_CONFIG) {
      if (myLastConfigFile != null && myLastConfigFile.equals(new File(testRunnerFailure.getJstdConfigPath()))) {
        myConfigLastTestProxy.setTestFailed(testRunnerFailure.getMessage(), null, false);
      } else {
        SMTestProxy configNode = new JstdConfigFileNode(myDirectory, testRunnerFailure.getJstdConfigPath()).getTestProxy();
        myContext.resultsForm().getTestsRootNode().addChild(configNode);
        configNode.setTestFailed(testRunnerFailure.getMessage(), null, false);
      }
    } else {
      myRootNodeLog.append(testRunnerFailure.getMessage()).append("\n");
      SMTestProxy.SMRootTestProxy rootNode = myContext.resultsForm().getTestsRootNode();
      rootNode.setTestFailed(myRootNodeLog.toString(), null, false);
      System.out.println("Setting root error to " + myRootNodeLog);
    }
  }

}
