package com.jetbrains.actionscript.profiler.calltree;

import gnu.trove.THashMap;

import java.util.*;

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
  static List<CallTreeNode> findCallsByFrames(CallTreeNode root, String[] frames) {
    ArrayList<CallTreeNode> calls = new ArrayList<CallTreeNode>();
    if (frames.length == 0) {
      return calls;
    }
    Map<String, LinkedList<CallTreeNode>> callsByName = new THashMap<String, LinkedList<CallTreeNode>>();
    for (CallTreeNode node : root.getChildren()) {
      fillCallsByFrames(node, callsByName, frames, new ParentCallInfo(frames.length - 1, null, 0));
    }
    //need unique, but in order
    Set<CallTreeNode> added = new LinkedHashSet<CallTreeNode>();
    for (LinkedList<CallTreeNode> callsForName : callsByName.values()) {
      added.addAll(callsForName);
    }
    return new ArrayList<CallTreeNode>(added);
  }

  private static void fillCallsByFrames(CallTreeNode currentNode,
                                        Map<String, LinkedList<CallTreeNode>> calls,
                                        String[] frames,
                                        ParentCallInfo parentInfo) {
    //we need only the nearest node to the root
    int currentSizeBefore = 0;
    if (calls.containsKey(currentNode.getFrameName())) {
      currentSizeBefore = calls.get(currentNode.getFrameName()).size();
    }

    for (CallTreeNode childCall : currentNode.getChildren()) {
      if (childCall.getFrameName().equals(frames[frames.length - 1])) {
        //call sequence may starts here
        fillCallsByFrames(childCall, calls, frames, new ParentCallInfo(frames.length - 1, currentNode, currentSizeBefore));
      }
      else {
        //otherwise parent in null
        fillCallsByFrames(childCall, calls, frames, new ParentCallInfo(frames.length - 1, null, 0));
      }

      if (parentInfo.getIndex() > 0 && currentNode.getFrameName().equals(frames[parentInfo.getIndex()])) {
        //match parent's expectations
        fillCallsByFrames(childCall, calls, frames,
                          new ParentCallInfo(parentInfo.getIndex() - 1, parentInfo.getNode(), parentInfo.getSnapshotSizeOfCalls()));
      }
    }

    //before out we try to add new calls.
    if (parentInfo.getIndex() == 0 && currentNode.getFrameName().equals(frames[parentInfo.getIndex()]) && parentInfo.getNode() != null) {
      String callerName = parentInfo.getNode().getFrameName();
      LinkedList<CallTreeNode> callerCalls = calls.get(callerName);
      if (callerCalls == null) {
        callerCalls = new LinkedList<CallTreeNode>();
        calls.put(callerName, callerCalls);
      }
      //remove calls that were added in subcalls.
      //cause of that we get only the nearest node to the root
      while (callerCalls.size() > parentInfo.getSnapshotSizeOfCalls()) {
        callerCalls.removeLast();
      }
      callerCalls.add(parentInfo.getNode());
    }
  }
}
