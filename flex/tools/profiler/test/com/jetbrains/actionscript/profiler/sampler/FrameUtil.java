package com.jetbrains.actionscript.profiler.sampler;

/**
 * @author: Fedor.Korotkov
 */
public class FrameUtil {
  public static FrameInfo getFrameInfo(String name){
    return new FrameInfo(null, null, -1, null, name, null, null, null);
  }

  public static FrameInfo[] getInstances(String[] frames){
    FrameInfoBuilder frameInfoBuilder = new FrameInfoBuilder();
    return frameInfoBuilder.buildInstances(frames);
  }
}
