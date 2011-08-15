package com.google.jstestdriver.idea.execution.tree;

import com.google.jstestdriver.idea.javascript.navigation.Test;
import com.google.jstestdriver.idea.javascript.navigation.TestCase;
import com.intellij.execution.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

class TestNode extends Node {

  private final TestCaseNode myTestCaseNode;
  private boolean myDone = false;

  public TestNode(@NotNull TestCaseNode testCaseNode, @NotNull String testName) {
    myTestCaseNode = testCaseNode;
    TestCase testCaseStructure = testCaseNode.getTestCaseStructure();
    Test test = testCaseStructure != null ? testCaseStructure.getTestByName(testName) : null;
    Location testLocation = test != null ? test.getLocation() : null;
    setTestProxy(new SMTestProxyWithPrinterAndLocation(testName, false, LocationProvider.createConstantProvider(testLocation)));
    myTestCaseNode.registerTestNode(this);
  }

  public TestCaseNode getTestCaseNode() {
    return myTestCaseNode;
  }

  public void done() {
    myDone = true;
  }

  @Override
  public boolean isComplete() {
    return myDone;
  }

  @Override
  public Collection<? extends Node> getChildren() {
    return Collections.emptyList();
  }
}
