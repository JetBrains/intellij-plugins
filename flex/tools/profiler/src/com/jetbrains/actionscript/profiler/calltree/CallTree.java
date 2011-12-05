package com.jetbrains.actionscript.profiler.calltree;

import com.intellij.openapi.util.Pair;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import gnu.trove.THashSet;

import java.util.*;

public class CallTree {
  private final CallTreeNode root;

  public CallTree() {
    root = new CallTreeNode();
  }

  CallTree(CallTreeNode root) {
    this.root = root;
  }

  public void addFrames(FrameInfo[] frames, long duration, boolean skipSystemStuff) {
    ArrayList<FrameInfo> filteredFrames = new ArrayList<FrameInfo>();
    for (int index = frames.length - 1; index >= 0; --index) {
      if (!(skipSystemStuff && frames[index].isSystem())) {
        filteredFrames.add(frames[index]);
      }
    }
    root.addChildren(filteredFrames, duration);
  }

  /*
   * @return pair <cumulative time map, self time map>
   */
  public Pair<Map<FrameInfo, Long>, Map<FrameInfo, Long>> getTimeMaps() {
    return TimeMapBuilder.buildTimeMaps(root, root.getChildren());
  }

  /*
   * @return pair <cumulative time map, self time map>
   */
  public Pair<Map<FrameInfo, Long>, Map<FrameInfo, Long>> getCallersTimeMaps(FrameInfo[] frames) {
    List<CallTreeNode> calls = CallerFinder.findCallsByFrames(root, frames);
    Pair<Map<FrameInfo, Long>, Map<FrameInfo, Long>> timeMaps = TimeMapBuilder.buildTimeMaps(root, calls);
    Set<FrameInfo> callerNames = getNamesOfNodes(calls);
    Map<FrameInfo, Long> filteredCountMap = filterMap(timeMaps.getFirst(), callerNames);
    Map<FrameInfo, Long> filteredSelfTimeMap = filterMap(timeMaps.getSecond(), callerNames);
    return Pair.create(filteredCountMap, filteredSelfTimeMap);
  }

  /*
   * @return pair <cumulative time map, self time map>
   */
  public Pair<Map<FrameInfo, Long>, Map<FrameInfo, Long>> getCalleesTimeMaps(FrameInfo[] frames) {
    List<CallTreeNode> calls = CalleeFinder.findCallsByFrameName(root, frames);
    return TimeMapBuilder.buildTimeMaps(root, calls);
  }

  private static Map<FrameInfo, Long> filterMap(Map<FrameInfo, Long> first, Set<FrameInfo> callerNames) {
    first.keySet().retainAll(callerNames);
    return first;
  }

  private static Set<FrameInfo> getNamesOfNodes(Collection<CallTreeNode> nodes) {
    THashSet<FrameInfo> names = new THashSet<FrameInfo>();
    for (CallTreeNode node : nodes) {
      names.add(node.getFrameInfo());
    }
    return names;
  }
}
