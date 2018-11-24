package com.jetbrains.actionscript.profiler.calltree;

import com.jetbrains.actionscript.profiler.base.FilePathProducer;
import com.jetbrains.actionscript.profiler.base.FrameInfoProducer;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import gnu.trove.THashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class CallTreeNode implements FrameInfoProducer, FilePathProducer {
  private final FrameInfo frameInfo;
  private long duration;
  private final THashMap<FrameInfo, CallTreeNode> children = new THashMap<>();

  public CallTreeNode() {
    frameInfo = null;
  }

  public CallTreeNode(FrameInfo frameInfo, long duration) {
    this.frameInfo = frameInfo;
    this.duration = duration;
  }

  public FrameInfo getFrameInfo() {
    return frameInfo;
  }

  public long getCumulativeTiming() {
    return duration;
  }

  public void addChildrenRecursive(List<CallTreeNode> children) {
    for (CallTreeNode child : children) {
      addChildRecursive(child);
    }
  }

  void addChildRecursive(CallTreeNode newChildNode) {
    CallTreeNode child = findChildByName(newChildNode.getFrameInfo());
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
    children.put(newChildNode.getFrameInfo(), newChildNode);
  }

  @Nullable
  public CallTreeNode findChildByName(FrameInfo frame) {
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
    return new ArrayList<>(children.values());
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
    return frameInfo.toString() + " (" + children.size() + ")";
  }

  public void addChildren(List<FrameInfo> reversedFrames, long duration) {
    CallTreeNode node = this;
    for (FrameInfo frame : reversedFrames) {
      CallTreeNode child = node.findChildByName(frame);
      if (child == null) {
        child = new CallTreeNode(frame, 0);
        node.addChild(child);
      }
      child.duration += duration;
      node = child;
    }
  }

  @Override
  public String getFilePath() {
    return frameInfo.getFilePath();
  }

  @Nullable
  public CallTreeNode getChildDeep(FrameInfo[] frames) {
    CallTreeNode currentNode = this;
    for (FrameInfo frame : frames) {
      currentNode = currentNode.findChildByName(frame);
      if (currentNode == null) {
        return null;
      }
    }
    return currentNode;
  }
}
