package com.jetbrains.actionscript.profiler.model;

import com.jetbrains.actionscript.profiler.sampler.CreateObjectSample;
import com.jetbrains.actionscript.profiler.sampler.DeleteObjectSample;
import com.jetbrains.actionscript.profiler.sampler.Sample;

import java.util.*;

/**
 * @author: Fedor.Korotkov
 */
public class ProfileData {
  private final Set<Sample> profile = new LinkedHashSet<Sample>();
  private final Map<Integer, CreateObjectSample> objects = new HashMap<Integer, CreateObjectSample>();
  private int allocated;
  private final Map<Integer, Set<Integer>> references = new LinkedHashMap<Integer, Set<Integer>>(50);

  public Set<Sample> getProfile() {
    return profile;
  }

  public Map<Integer, CreateObjectSample> getObjects() {
    return objects;
  }

  public int getAllocated() {
    return allocated;
  }

  public Map<Integer, Set<Integer>> getReferences() {
    return references;
  }

  public Collection<CreateObjectSample> getCreateObjectSamples() {
    return getObjects().values();
  }

  public void incAllocated(int size) {
    allocated += size;
  }

  public void decAllocated(int size) {
    allocated -= size;
  }

  public void putNewObject(int id, CreateObjectSample sample) {
    objects.put(id, sample);
  }

  public CreateObjectSample removeObject(int id) {
    return objects.remove(id);
  }

  public void addPerformanceSample(Sample sample) {
    profile.add(sample);
  }

  public int getPerformanceInfoSize() {
    return getProfile().size();
  }

  public void clearMemory() {
    allocated = 0;
    objects.clear();
    references.clear();

    Iterator<Sample> i = profile.iterator();
    while (i.hasNext()) {
      Sample next = i.next();
      if (next instanceof CreateObjectSample || next instanceof DeleteObjectSample) {
        i.remove();
      }
    }
  }

  public void clearPerformance() {
    Iterator<Sample> i = profile.iterator();
    while (i.hasNext()) {
      Sample next = i.next();
      if (!(next instanceof CreateObjectSample) && !(next instanceof DeleteObjectSample)) {
        i.remove();
      }
    }
  }

  public Set<Map.Entry<Integer, Set<Integer>>> getReferenceIds() {
    return references.entrySet();
  }
}
