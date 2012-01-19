package com.jetbrains.actionscript.profiler.model;

import com.jetbrains.actionscript.profiler.sampler.CreateObjectSample;
import com.jetbrains.actionscript.profiler.sampler.DeleteObjectSample;
import com.jetbrains.actionscript.profiler.sampler.ObjectSampleHandler;
import com.jetbrains.actionscript.profiler.sampler.Sample;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

public class ProfilerDataConsumer {
  private final ProfileData profileData = new ProfileData();
  @Nullable private final ObjectSampleHandler objectSampleHandler;

  public ProfilerDataConsumer(@Nullable ObjectSampleHandler objectSampleHandler) {
    this.objectSampleHandler = objectSampleHandler;
  }

  public ProfileData getProfileData() {
    return profileData;
  }

  public void process(Sample sample) {
    if (sample instanceof CreateObjectSample) {
      final CreateObjectSample createObjectSample = (CreateObjectSample)sample;
      profileData.putNewObject(createObjectSample.id, createObjectSample);

      if (objectSampleHandler != null) {
        objectSampleHandler.processCreateSample(createObjectSample);
      }
    }
    else if (sample instanceof DeleteObjectSample) {
      DeleteObjectSample deleteObjectSample = (DeleteObjectSample)sample;

      profileData.removeObject(deleteObjectSample.id);

      if (objectSampleHandler != null) {
        objectSampleHandler.processDeleteSample(deleteObjectSample);
      }
    }
    else {
      profileData.addPerformanceSample(sample);
    }
  }

  public void referenced(int pid, int id) {
    Set<Integer> integers = profileData.getReferences().get(pid);
    if (integers == null) {
      integers = new LinkedHashSet<Integer>(3);
      profileData.getReferences().put(pid, integers);
    }
    integers.add(id);
  }

  public void resetCpuUsageData() {
    profileData.clearPerformance();
  }
}
