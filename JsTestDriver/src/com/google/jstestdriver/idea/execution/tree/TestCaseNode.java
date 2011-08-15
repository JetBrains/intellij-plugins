package com.google.jstestdriver.idea.execution.tree;

import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.javascript.navigation.NavigationRegistry;
import com.google.jstestdriver.idea.javascript.navigation.TestCase;
import com.intellij.execution.Location;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * In the test results tree UI, this is an element representing a test case result.
 * @author alexeagle@google.com (Alex Eagle)
 */
class TestCaseNode extends Node {

  private final JstdConfigFileNode myJstdConfigFileNode;
  private final Map<String, TestNode> myTestNodeMap = Maps.newHashMap();
  private final TestCase myTestCaseStructure;
  // Determines how we represent the node. If any children fail, for example, the parent is also marked failed.
//  private Result myWorstResult = Result.passed;

  public TestCaseNode(JstdConfigFileNode jstdConfigFileNode, String testCaseName, @Nullable NavigationRegistry navigationRegistry) {
    myJstdConfigFileNode = jstdConfigFileNode;
    TestCase testCaseStructure = navigationRegistry != null ? navigationRegistry.getTestCaseByName(testCaseName) : null;
    Location location = testCaseStructure != null ? testCaseStructure.getLocation() : null;
    setTestProxy(new SMTestProxyWithPrinterAndLocation(testCaseName, true, LocationProvider.createConstantProvider(location)));
    myJstdConfigFileNode.registerTestCaseNode(this);
    myTestCaseStructure = testCaseStructure;
  }

  @Nullable
  public TestCase getTestCaseStructure() {
    return myTestCaseStructure;
  }

  public JstdConfigFileNode getJstdConfigFileNode() {
    return myJstdConfigFileNode;
  }

  public TestNode getTestByName(String testName) {
    return myTestNodeMap.get(testName);
  }

/*
  public boolean allTestsComplete() {
    for (TestNode testNode : myTestNodeMap.values()) {
      if (testNode.getTestProxy().isInProgress()) {
        return false;
      }
    }
    return true;
  }
*/

/*
  public void setTestFailed(Result result) {
    if (result == Result.error && myWorstResult != Result.error) {
      getTestProxy().setTestFailed("", "", true);
    } else if (result == Result.failed && myWorstResult == Result.passed) {
      getTestProxy().setTestFailed("", "", false);
    }
  }
*/

  public void registerTestNode(TestNode testNode) {
    myTestNodeMap.put(testNode.getTestProxy().getName(), testNode);
  }

//  public boolean isTestDone(String testName) {
//    return myDoneTestNames.contains(testName);
//  }
//
//  public void testDone(String testName) {
//    myDoneTestNames.add(testName);
//  }

  @Override
  public Collection<? extends Node> getChildren() {
    return myTestNodeMap.values();
  }
}
