package com.google.jstestdriver.idea.execution.tree;

import com.intellij.execution.testframework.Printer;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;

class Node {

  private SMTestProxyWithPrinterAndLocation myTestProxy;

  Node() {
  }

  public Node(SMTestProxyWithPrinterAndLocation testProxy) {
    setTestProxy(testProxy);
  }

  public void setTestProxy(SMTestProxyWithPrinterAndLocation testProxy) {
    myTestProxy = testProxy;
  }

  public SMTestProxy getTestProxy() {
    return myTestProxy;
  }

  public void wirePrinter(Printer printer) {
    myTestProxy.wirePrinter(printer);
  }

}
