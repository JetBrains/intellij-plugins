package com.jetbrains.actionscript.profiler.calltree;

import gnu.trove.THashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class CallTreeNode {
  private final String frameName;
  private long duration;
  private final THashMap<String, CallTreeNode> children = new THashMap<String, CallTreeNode>();

  private static String stripCallDelims(String s) {
    final int endIndex = s.indexOf('(');
    return endIndex != -1 ? s.substring(0, endIndex) : s;
  }

  public CallTreeNode() {
    frameName = null;
  }

  public CallTreeNode(String frame, long duration) {
    frameName = frame;
    this.duration = duration;
  }

  public String getFrameName() {
    return frameName;
  }

  public long getCumulativeTiming() {
    return duration;
  }

  void addChildRecursive(CallTreeNode newChildNode) {
    CallTreeNode child = findChildByName(newChildNode.getFrameName());
    if (child != null) {
      child.duration += newChildNode.getCumulativeTiming();
      for (CallTreeNode node : newChildNode.getChildren()) {
        child.addChildRecursive(node);
      }
    }
    else {
      addChild(newChildNode);
    }
  }

  private void addChild(CallTreeNode newChildNode) {
    children.put(newChildNode.getFrameName(), newChildNode);
  }

  @Nullable
  public CallTreeNode findChildByName(String frame) {
    return children.get(frame);
  }

  int calcSelfTiming() {
    int count = 0;
    for (CallTreeNode child : children.values()) {
      count += child.getCumulativeTiming();
    }
    return count;
  }

  List<CallTreeNode> getChildren() {
    return new ArrayList<CallTreeNode>(children.values());
  }

  @Override
  public boolean equals(Object obj) {
    /*
     * compare references cause we don't have needed information
     * nodes equal if and only if they have same parent
     * we may have many nodes with the same name and children e.g. in recursive functions
    */
    return obj == this;
  }

  @Override
  public String toString() {
    return frameName + " (" + children.size() + ")";
  }

  public void addChildren(List<String> reversedFrames, long duration) {
    CallTreeNode node = this;
    for (String frame : reversedFrames) {
      frame = stripCallDelims(frame);
      CallTreeNode child = node.findChildByName(frame);
      if (child == null) {
        child = new CallTreeNode(frame, 0);
        node.addChild(child);
      }
      child.duration += duration;
      node = child;
    }
  }
}
