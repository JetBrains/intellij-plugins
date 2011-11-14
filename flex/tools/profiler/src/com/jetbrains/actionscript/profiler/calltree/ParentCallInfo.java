package com.jetbrains.actionscript.profiler.calltree;

class ParentCallInfo {
  private final int index;
  private final int snapshotSizeOfCalls;
  private final CallTreeNode node;

  public ParentCallInfo(int index, CallTreeNode node, int snapshotSizeOfCalls) {
    this.index = index;
    this.node = node;
    this.snapshotSizeOfCalls = snapshotSizeOfCalls;
  }

  public int getIndex() {
    return index;
  }

  public int getSnapshotSizeOfCalls() {
    return snapshotSizeOfCalls;
  }

  public CallTreeNode getNode() {
    return node;
  }
}
