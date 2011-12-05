package com.jetbrains.actionscript.profiler.calltree;

import com.intellij.openapi.util.Pair;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

import java.util.List;
import java.util.Map;

class TimeMapBuilder {
  private TimeMapBuilder() {
  }

  /*
  * @return pair <cumulative time map, self time map>
  */
  static Pair<Map<FrameInfo, Long>, Map<FrameInfo, Long>> buildTimeMaps(CallTreeNode root, List<CallTreeNode> calls) {
    Map<FrameInfo, Long> countMap = new THashMap<FrameInfo, Long>();
    Map<FrameInfo, Long> selfCountMap = new THashMap<FrameInfo, Long>();
    THashSet<CallTreeNode> trackedCalls = new THashSet<CallTreeNode>(calls);

    for (CallTreeNode node : root.getChildren()) {
      fillTimeMaps(node, countMap, selfCountMap, trackedCalls.contains(node), trackedCalls);
    }
    return Pair.create(countMap, selfCountMap);
  }

  private static void fillTimeMaps(CallTreeNode node,
                                   Map<FrameInfo, Long> countMap,
                                   Map<FrameInfo, Long> selfCountMap,
                                   boolean tracking,
                                   THashSet<CallTreeNode> trackedCalls) {
    if (tracking) {
      updateSelfTime(node, selfCountMap);
    }

    //save value before subcalls
    Long countBefore = countMap.get(node.getFrameInfo());
    if (countBefore == null) {
      countBefore = 0L;
    }
    for (CallTreeNode child : node.getChildren()) {
      fillTimeMaps(child, countMap, selfCountMap, tracking || trackedCalls.contains(child), trackedCalls);
    }
    if (tracking) {
      //rewrite values that were added in subcalls.
      //cause of that we get value only of the nearest node to the root
      countMap.put(node.getFrameInfo(), countBefore + node.getCumulativeTiming());
    }
  }

  private static void updateSelfTime(CallTreeNode node, Map<FrameInfo, Long> selfCountMap) {
    long currentSelfTime = node.getCumulativeTiming() - node.calcSelfTiming();
    Long selfTime = selfCountMap.get(node.getFrameInfo());
    if (selfTime == null) {
      selfTime = 0L;
    }
    selfCountMap.put(node.getFrameInfo(), selfTime + currentSelfTime);
  }
}
