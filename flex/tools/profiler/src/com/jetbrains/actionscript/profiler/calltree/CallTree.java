package com.jetbrains.actionscript.profiler.calltree;

import com.intellij.openapi.util.Pair;
import com.intellij.util.ArrayUtil;
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

  CallTreeNode getRoot() {
    return root;
  }

  public void addFrames(FrameInfo[] frames, long duration) {
    root.addChildren(Arrays.asList(ArrayUtil.reverseArray(frames)), duration);
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
    THashSet<FrameInfo> names = new THashSet<>();
    for (CallTreeNode node : nodes) {
      names.add(node.getFrameInfo());
    }
    return names;
  }
}
