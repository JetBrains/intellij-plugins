package com.jetbrains.actionscript.profiler.calltree;

import com.intellij.openapi.util.Pair;
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

  public void addFrames(String[] frames, long duration, boolean skipSystemStuff) {
    ArrayList<String> filteredFrames = new ArrayList<String>();
    for (int index = frames.length - 1; index >= 0; --index) {
      if (!(skipSystemStuff && frames[index].startsWith("["))) {
        filteredFrames.add(frames[index]);
      }
    }
    root.addChildren(filteredFrames, duration);
  }

  /*
   * @return pair <cumulative time map, self time map>
   */
  public Pair<Map<String, Long>, Map<String, Long>> getTimeMaps() {
    return TimeMapBuilder.buildTimeMaps(root, root.getChildren());
  }

  /*
   * @return pair <cumulative time map, self time map>
   */
  public Pair<Map<String, Long>, Map<String, Long>> getCallersTimeMaps(String[] frames) {
    List<CallTreeNode> calls = CallerFinder.findCallsByFrames(root, frames);
    Pair<Map<String, Long>, Map<String, Long>> timeMaps = TimeMapBuilder.buildTimeMaps(root, calls);
    Set<String> callerNames = getNamesOfNodes(calls);
    Map<String, Long> filteredCountMap = filterMap(timeMaps.getFirst(), callerNames);
    Map<String, Long> filteredSelfTimeMap = filterMap(timeMaps.getSecond(), callerNames);
    return Pair.create(filteredCountMap, filteredSelfTimeMap);
  }

  /*
   * @return pair <cumulative time map, self time map>
   */
  public Pair<Map<String, Long>, Map<String, Long>> getCalleesTimeMaps(String[] frames) {
    List<CallTreeNode> calls = CalleeFinder.findCallsByFrameName(root, frames);
    return TimeMapBuilder.buildTimeMaps(root, calls);
  }

  private static Map<String, Long> filterMap(Map<String, Long> first, Set<String> callerNames) {
    first.keySet().retainAll(callerNames);
    return first;
  }

  private static Set<String> getNamesOfNodes(Collection<CallTreeNode> nodes) {
    THashSet<String> names = new THashSet<String>();
    for (CallTreeNode node : nodes) {
      names.add(node.getFrameName());
    }
    return names;
  }
}
