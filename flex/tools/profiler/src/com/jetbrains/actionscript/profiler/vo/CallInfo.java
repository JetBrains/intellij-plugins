package com.jetbrains.actionscript.profiler.vo;

public class CallInfo {
  private final String frameName;
  private final long cumulativeTime;
  private final long selfTime;

  public CallInfo(String frameName, long cumulativeTime, long selfTime) {
    this.frameName = frameName;
    this.cumulativeTime = cumulativeTime;
    this.selfTime = selfTime;
  }

  public String getFrameName() {
    return frameName;
  }

  public long getCumulativeTime() {
    return cumulativeTime;
  }

  public long getSelfTime() {
    return selfTime;
  }

  @Override
  public String toString() {
    return getFrameName();
  }
}
