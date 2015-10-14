package com.jetbrains.actionscript.profiler.model;

import com.intellij.openapi.util.Key;
import com.jetbrains.actionscript.profiler.calltree.CallTree;
import com.jetbrains.actionscript.profiler.livetable.LiveModelController;
import com.jetbrains.actionscript.profiler.sampler.CreateObjectSample;
import com.jetbrains.actionscript.profiler.sampler.Sample;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author: Fedor.Korotkov
 */
public class ProfileData {
  public static final Key<CallTree> CALL_TREE_KEY = Key.create("ASCallTree");
  public static final Key<LiveModelController> CONTROLLER = Key.create("ASLiveController");
  public static final Key<ProfilingManager> PROFILING_MANAGER = Key.create("ASProfilingManager");

  private CallTree callTree = new CallTree();
  private final Map<Integer, CreateObjectSample> objects = new HashMap<Integer, CreateObjectSample>();
  private final Map<Integer, Set<Integer>> references = new LinkedHashMap<Integer, Set<Integer>>(50);

  public CallTree getCallTree() {
    return callTree;
  }

  public Map<Integer, Set<Integer>> getReferences() {
    return references;
  }

  public void putNewObject(int id, CreateObjectSample sample) {
    objects.put(id, sample);
  }

  public CreateObjectSample removeObject(int id) {
    return objects.remove(id);
  }

  public void addPerformanceSample(Sample sample) {
    callTree.addFrames(sample.frames, sample.duration);
  }

  public void clearMemory() {
    objects.clear();
    references.clear();
  }

  public void clearPerformance() {
    callTree = new CallTree();
  }
}
