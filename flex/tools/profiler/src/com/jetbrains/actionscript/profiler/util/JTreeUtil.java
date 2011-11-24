package com.jetbrains.actionscript.profiler.util;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class JTreeUtil {
  private JTreeUtil() {
  }

  public static void removeChildren(DefaultMutableTreeNode root, DefaultTreeModel model) {
    if (root.getChildCount() == 0) {
      return;
    }
    int[] childIndices = new int[root.getChildCount()];
    Object[] removedChildren = new Object[root.getChildCount()];
    for (int i = 0; i < root.getChildCount(); ++i) {
      childIndices[i] = i;
      removedChildren[i] = root.getChildAt(i);
    }
    root.removeAllChildren();
    model.nodesWereRemoved(root, childIndices, removedChildren);
  }
}
