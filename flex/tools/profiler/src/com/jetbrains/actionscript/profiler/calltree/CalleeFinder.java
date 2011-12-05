package com.jetbrains.actionscript.profiler.calltree;

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
    for (CallTreeNode call : root.getChildren()) {
      fillCallsByFrameName(call, frames, frames.length - 1, result);
    }
    return result;
  }

  private static void fillCallsByFrameName(CallTreeNode node,
                                           FrameInfo[] frames,
                                           int index,
                                           List<CallTreeNode> result) {
    if (index == 0 && frames[0].equals(node.getFrameInfo())) {
      result.addAll(node.getChildren());
      return;
    }
    boolean currentNodeMatch = frames[index].equals(node.getFrameInfo());
    for (CallTreeNode child : node.getChildren()) {
      fillCallsByFrameName(child, frames, currentNodeMatch ? index - 1 : frames.length - 1, result);
      if (currentNodeMatch && child.getFrameInfo().equals(frames[frames.length - 1])) {
        //may starts here
        fillCallsByFrameName(child, frames, frames.length - 1, result);
      }
    }
  }
}
