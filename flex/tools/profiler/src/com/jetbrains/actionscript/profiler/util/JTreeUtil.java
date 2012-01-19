package com.jetbrains.actionscript.profiler.util;

import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JTreeUtil {
  private JTreeUtil() {
  }

  public static boolean isSorted(List<TreePath> paths, Comparator<TreeNode> comparator, TreeTableModel model) {
    for (TreePath path : paths) {
      Object node = path.getLastPathComponent();
      if (node instanceof DefaultMutableTreeNode && !isSorted((DefaultMutableTreeNode)node, comparator, model)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isSorted(DefaultMutableTreeNode node, Comparator<TreeNode> comparator, TreeTableModel model) {
    List<TreeNode> children = childrenToArray(node, 0, model.getChildCount(node));
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

  public static void sortChildren(final DefaultMutableTreeNode node, final Comparator comparator, final int maxIndex) {
    final List<TreeNode> childrenSorted = childrenToArray(node, 0, maxIndex);
    Collections.sort(childrenSorted, comparator);
    childrenSorted.addAll(childrenToArray(node, maxIndex, node.getChildCount()));
    node.removeAllChildren();
    TreeUtil.addChildrenTo(node, childrenSorted);
  }

  private static List<TreeNode> childrenToArray(DefaultMutableTreeNode node, int l, int r) {
    final List<TreeNode> result = new ArrayList<TreeNode>();
    for (; l < r; ++l) {
      result.add(node.getChildAt(l));
    }
    return result;
  }

  public static void removeChildren(DefaultMutableTreeNode root, DefaultTreeModel model) {
    if (root.getChildCount() == 0) {
      return;
    }
    int[] childIndices = new int[model.getChildCount(root)];
    Object[] removedChildren = new Object[model.getChildCount(root)];
    for (int i = 0; i < removedChildren.length; ++i) {
      childIndices[i] = i;
      removedChildren[i] = root.getChildAt(i);
    }
    root.removeAllChildren();
    model.nodesWereRemoved(root, childIndices, removedChildren);
  }
}
