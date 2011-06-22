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

import java.io.File;
import java.io.Serializable;

import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.TestResult;

/**
 * This class is serialized using Java serialization, and sent across the socket from the
 * test runner to the IDE. It contains any information from the test runner that needs to
 * affect the UI in the IDE.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class TestResultProtocolMessage implements Serializable {
  public String jstdConfigFilePath;
  public String phase;
  public String testName;
  public String browser;
  public String testCase = "<unnamed>";
  public String stack;
  public String log;
  public String message;
  public float duration;
  public String result;

  public static TestResultProtocolMessage fromTestResult(File jstdConfigFile, TestResult testResult) {
    TestResultProtocolMessage message = new TestResultProtocolMessage();
    message.jstdConfigFilePath = jstdConfigFile.getAbsolutePath();
    message.phase = "testRun";
    message.testCase = testResult.getTestCaseName();
    message.testName = testResult.getTestName();
    message.browser = testResult.getBrowserInfo().toString();
    message.stack = testResult.getStack();
    message.log = testResult.getLog();
    message.message = testResult.getParsedMessage();
    message.duration = testResult.getTime();
    message.result = testResult.getResult().toString();
    return message;
  }

  public static TestResultProtocolMessage fromDryRun(File jstdConfigFile, String testName, BrowserInfo browser) {
    TestResultProtocolMessage message = new TestResultProtocolMessage();
    message.jstdConfigFilePath = jstdConfigFile.getAbsolutePath();
    message.phase = "dryRun";
    int lastDot = testName.lastIndexOf(".");
    if (lastDot < 0) {
      message.testName = testName;
    } else {
      message.testCase = testName.substring(0, lastDot);
      message.testName = testName.substring(lastDot + 1);
    }
    message.browser = browser.toString();
    return message;
  }

  public boolean isDryRun() {
    return phase.equals("dryRun");
  }

}
