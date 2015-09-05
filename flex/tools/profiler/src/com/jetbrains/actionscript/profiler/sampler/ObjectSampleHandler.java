package com.jetbrains.actionscript.profiler.sampler;

/**
 * @author: Fedor.Korotkov
 */
public interface ObjectSampleHandler {
  void processCreateSample(CreateObjectSample createObjectSample);

  void processDeleteSample(DeleteObjectSample deleteObjectSample);
}
