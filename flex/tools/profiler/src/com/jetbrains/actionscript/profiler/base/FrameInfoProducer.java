package com.jetbrains.actionscript.profiler.base;

import com.jetbrains.actionscript.profiler.sampler.FrameInfo;

/**
 * @author: Fedor.Korotkov
 */
public interface FrameInfoProducer {
  FrameInfo getFrameInfo();
}
