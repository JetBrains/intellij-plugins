package com.jetbrains.actionscript.profiler.util;

import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JTreeUtil {
  private JTreeUtil() {
  }

  public static boolean isSorted(TreeModel model, Comparator<TreeNode> comparator) {
    return isSorted((TreeNode)model.getRoot(), comparator);
  }

  private static boolean isSorted(TreeNode root, Comparator<TreeNode> comparator) {
    List<TreeNode> children = TreeUtil.childrenToArray(root);
    if (!isSorted(children, comparator)) {
      return false;
    }
    boolean result = true;
    for (TreeNode child : children) {
      result = result && isSorted(child, comparator);
    }
    return result;
  }

  private static boolean isSorted(List<TreeNode> nodes, Comparator<TreeNode> comparator) {
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
