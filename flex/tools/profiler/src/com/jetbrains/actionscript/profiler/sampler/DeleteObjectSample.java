package com.jetbrains.actionscript.profiler.sampler;

/**
 * User: Maxim
 * Date: 04.09.2010
 * Time: 19:54:24
 */
public class DeleteObjectSample extends Sample {
  public final int id;
  public final int size;

  public DeleteObjectSample(long duration, FrameInfo[] frames, int id, int size) {
    super(duration, frames);
    this.id = id;
    this.size = size;
  }
}
