package com.jetbrains.plugins.cidr.debugger.chart.ui;

import com.intellij.xdebugger.breakpoints.XLineBreakpoint;

import java.util.Comparator;

class XLineBreakpointComparator implements Comparator<XLineBreakpoint<?>> {
  public static final XLineBreakpointComparator COMPARATOR = new XLineBreakpointComparator();

  @Override
  public int compare(XLineBreakpoint o1, XLineBreakpoint o2) {

    int i = o1.getShortFilePath().compareTo(o2.getShortFilePath());
    if (i != 0) {
      return i;
    }
    return o1.getLine() - o2.getLine();
  }
}
