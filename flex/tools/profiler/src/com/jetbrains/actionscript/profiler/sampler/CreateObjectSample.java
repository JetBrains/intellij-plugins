package com.jetbrains.actionscript.profiler.sampler;

/**
 * User: Maxim
 * Date: 04.09.2010
 * Time: 19:54:24
 */
public class CreateObjectSample extends Sample {
  public final int id;
  public final String className;
  public final int size;

  public CreateObjectSample(long duration, String[] frames, int id, String className, int size) {
    super(duration, frames);
    this.id = id;
    this.className = className;
    this.size = size;
  }
}
