package com.google.jstestdriver.idea.execution.tree;

import com.google.jstestdriver.TestResult;
import com.intellij.execution.testframework.Printer;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;

import java.util.Collection;

abstract class Node {

  private SMTestProxyWithPrinterAndLocation myTestProxy;
  private TestResult.Result myWorstResult;

  Node() {
  }

  public Node(SMTestProxyWithPrinterAndLocation testProxy) {
    setTestProxy(testProxy);
  }

  public abstract Collection<? extends Node> getChildren();

  public void setTestProxy(SMTestProxyWithPrinterAndLocation testProxy) {
    myTestProxy = testProxy;
  }

  public SMTestProxy getTestProxy() {
    return myTestProxy;
  }

  public String getName() {
    return myTestProxy.getName();
  }

  public void wirePrinter(Printer printer) {
    myTestProxy.wirePrinter(printer);
  }

  public boolean isComplete() {
    for (Node childNode : getChildren()) {
      if (!childNode.isComplete()) {
        return false;
      }
    }
    return true;
  }

  public void setTestFailed(TestResult.Result result) {
    if (myWorstResult == null
        || (myWorstResult == TestResult.Result.failed && result == TestResult.Result.error)) {
      myWorstResult = result;
      if (result == TestResult.Result.error) {
        getTestProxy().setTestFailed("", "", true);
      } else if (result == TestResult.Result.failed) {
        getTestProxy().setTestFailed("", "", false);
      }
    }
  }

}
