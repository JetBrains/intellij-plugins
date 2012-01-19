package com.jetbrains.actionscript.profiler.vo;

import com.jetbrains.actionscript.profiler.base.FrameInfoProducer;
import com.jetbrains.actionscript.profiler.base.QNameProducer;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;

public class CallInfo implements FrameInfoProducer, QNameProducer {
  private final FrameInfo frameInfo;
  private final long cumulativeTime;
  private final long selfTime;

  public CallInfo(FrameInfo frameInfo, long cumulativeTime, long selfTime) {
    this.frameInfo = frameInfo;
    this.cumulativeTime = cumulativeTime;
    this.selfTime = selfTime;
  }

  public FrameInfo getFrameInfo() {
    return frameInfo;
  }

  public long getCumulativeTime() {
    return cumulativeTime;
  }

  public long getSelfTime() {
    return selfTime;
  }

  @Override
  public String toString() {
    return getFrameInfo().toString();
  }

  @Override
  public String getQName() {
    return getFrameInfo().getQName();
  }
}
