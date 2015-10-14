package com.jetbrains.actionscript.profiler.calltree;

/**
 * @author: Fedor.Korotkov
 */
public class CallTreeUtil {
  public static CallTree filterSystemStuff(CallTree tree) {
    return new CallTree(filterSystemStuffImpl(tree.getRoot()));
  }

  private static CallTreeNode filterSystemStuffImpl(CallTreeNode node) {
    CallTreeNode result = new CallTreeNode(node.getFrameInfo(), node.getCumulativeTiming());

    for (CallTreeNode child : node.getChildren()) {
      CallTreeNode newChild = filterSystemStuffImpl(child);
      if (newChild.getFrameInfo().isSystem()) {
        result.addChildrenRecursive(newChild.getChildren());
      }
      else {
        result.addChildRecursive(newChild);
      }
    }

    return result;
  }
}
