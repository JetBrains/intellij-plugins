package com.google.jstestdriver.idea.execution.tree;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.jstestdriver.TestResult.Result;
import com.google.jstestdriver.idea.javascript.navigation.TestCase;
import com.intellij.execution.Location;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * In the test results tree UI, this is an element representing a test case result.
 * @author alexeagle@google.com (Alex Eagle)
 */
class TestCaseNode extends Node {

  private final Map<String, SMTestProxy> myTestProxyMap = Maps.newHashMap();
  private final Set<String> myDoneTestNames = Sets.newHashSet();
  // Determines how we represent the node. If any children fail, for example, the parent is also marked failed.
  private Result myWorstResult = Result.passed;

  public TestCaseNode(String testCaseName, @Nullable TestCase testCaseLocation) {
    Location location = testCaseLocation == null ? null : testCaseLocation.getLocation();
    setTestProxy(new SMTestProxyWithPrinterAndLocation(testCaseName, true, LocationProvider.createConstantProvider(location)));
  }

  public String getName() {
    return getTestProxy().getName();
  }

  public SMTestProxy getTestByName(String testName) {
    return myTestProxyMap.get(testName);
  }

  public boolean allTestsComplete() {
    for (SMTestProxy testProxy : myTestProxyMap.values()) {
      if (testProxy.isInProgress()) {
        return false;
      }
    }
    return true;
  }

  public void setTestFailed(Result result) {
    if (result == Result.error && myWorstResult != Result.error) {
      getTestProxy().setTestFailed("", "", true);
    } else if (result == Result.failed && myWorstResult == Result.passed) {
      getTestProxy().setTestFailed("", "", false);
    }
  }

  public void registerTestProxy(SMTestProxy testNode) {
    myTestProxyMap.put(testNode.getName(), testNode);
  }

  public boolean isTestDone(String testName) {
    return myDoneTestNames.contains(testName);
  }

  public void testDone(String testName) {
    myDoneTestNames.add(testName);
  }
}
