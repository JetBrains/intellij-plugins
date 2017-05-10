package com.jetbrains.actionscript.profiler.sampler;

public class Sample {
  public final FrameInfo[] frames;
  public final long duration;

  public Sample(long duration, FrameInfo[] frames) {
    this.duration = duration;
    this.frames = frames;
  }
}
