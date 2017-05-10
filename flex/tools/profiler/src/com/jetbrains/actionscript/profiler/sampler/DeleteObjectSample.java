package com.jetbrains.actionscript.profiler.sampler;

public class DeleteObjectSample extends Sample {
  public final int id;
  public final String className;
  public final int size;

  public DeleteObjectSample(long duration, FrameInfo[] frames, int id, String type, int size) {
    super(duration, frames);
    this.id = id;
    this.size = size;
    className = type;
  }
}
