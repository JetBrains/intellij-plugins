package com.jetbrains.actionscript.profiler;

import java.util.List;

/**
 * User: Maxim
 * Date: 04.09.2010
 * Time: 19:54:24
 */
public class Sample {
  public final String[] frames;
  public final long timestamp;

  public Sample(long timestamp, String[] frames) {
    this.timestamp = timestamp;
    this.frames = frames;
  }
}
