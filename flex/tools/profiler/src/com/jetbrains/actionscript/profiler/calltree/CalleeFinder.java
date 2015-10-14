package com.jetbrains.actionscript.profiler.calltree;

import com.intellij.util.ArrayUtil;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;

import java.util.ArrayList;
import java.util.List;

class CalleeFinder {
  private CalleeFinder() {
  }

  /*
  * Find nodes with <code>frameName == frames[0]</code>. Node's call stack contains all <code>frames</code> in order.
  */
  static List<CallTreeNode> findCallsByFrameName(CallTreeNode root, FrameInfo[] frames) {
    ArrayList<CallTreeNode> result = new ArrayList<CallTreeNode>();
    fillCallsByFrameName(root, ArrayUtil.reverseArray(frames), result);
    return result;
  }

  private static void fillCallsByFrameName(CallTreeNode node,
                                           FrameInfo[] frames,
                                           List<CallTreeNode> result) {
    final CallTreeNode deepChild = node.getChildDeep(frames);
    if (deepChild != null) {
      result.addAll(deepChild.getChildren());
    }
    for (CallTreeNode child : node.getChildren()) {
      fillCallsByFrameName(child, frames, result);
    }
  }
}
