package com.jetbrains.actionscript.profiler.calltree;

import com.intellij.util.ArrayUtil;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;

import java.util.ArrayList;
import java.util.List;

class CallerFinder {
  private CallerFinder() {
  }

  /*
  * Find nodes that call <code>frames</code> in reverse order.
  *
  * For example
  *
  * <code>foo</code> call <code>bar</code> call <code>baz</code>
  * <code>bad</code> call <code>bar</code> call <code>foo</code> call <code>baz</code>
  * <code>frames = [baz, bar]</code>
  *
  * Method return only <code>foo</code>.
  */
  static List<CallTreeNode> findCallsByFrames(CallTreeNode root, FrameInfo[] frames) {
    List<CallTreeNode> calls = new ArrayList<CallTreeNode>();
    if (frames.length == 0) {
      return calls;
    }
    frames = ArrayUtil.reverseArray(frames);
    for (CallTreeNode node : root.getChildren()) {
      fillCallsByFrames(node, calls, frames, new ArrayList<FrameInfo>());
    }
    return calls;
  }

  private static void fillCallsByFrames(CallTreeNode currentNode,
                                        List<CallTreeNode> result,
                                        FrameInfo[] frames,
                                        List<FrameInfo> callChainAddedFrames) {
    //we need only the nearest node to the root
    //we have <code>callChainAddedFrames<code>
    boolean needAdd = !callChainAddedFrames.contains(currentNode.getFrameInfo()) && currentNode.getChildDeep(frames) != null;
    if (needAdd) {
      result.add(currentNode);
      callChainAddedFrames.add(currentNode.getFrameInfo());
    }

    for (CallTreeNode childCall : currentNode.getChildren()) {
      fillCallsByFrames(childCall, result, frames, callChainAddedFrames);
    }
    if (needAdd) {
      //pop
      callChainAddedFrames.remove(callChainAddedFrames.size() - 1);
    }
  }
}
