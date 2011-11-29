package com.jetbrains.actionscript.profiler.util;

import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.tree.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JTreeUtil {
  private JTreeUtil() {
  }

  public static boolean isSorted(List<TreePath> paths, Comparator<TreeNode> comparator) {
    for (TreePath path : paths) {
      Object node = path.getLastPathComponent();
      if (node instanceof TreeNode && !isSorted((TreeNode)node, comparator)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isSorted(TreeNode node, Comparator<TreeNode> comparator) {
    List<TreeNode> children = TreeUtil.childrenToArray(node);
    return isNodesSorted(children, comparator);
  }

  private static boolean isNodesSorted(List<TreeNode> nodes, Comparator<TreeNode> comparator) {
    TreeNode prev = null;
    for (TreeNode node : nodes) {
      if (prev != null && comparator.compare(prev, node) == 1) {
        return false;
      }
      prev = node;
    }
    return true;
  }

  public static void sortChildren(final DefaultMutableTreeNode node, final Comparator comparator) {
    final List<TreeNode> children = TreeUtil.childrenToArray(node);
    Collections.sort(children, comparator);
    node.removeAllChildren();
    TreeUtil.addChildrenTo(node, children);
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
