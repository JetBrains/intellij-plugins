package com.jetbrains.actionscript.profiler.base;

import com.intellij.util.ui.ColumnInfo;
import com.jetbrains.actionscript.profiler.ProfilerBundle;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * @author: Fedor.Korotkov
 */
public class BoundedSortableListTreeTableModel extends SortableListTreeTableModel {
  private static final int MAX_CHILDREN_COUNT = 500;
  private static final TreeNode TOO_MANY_NODE = new DefaultMutableTreeNode(ProfilerBundle.message("too.many.nodes"));

  public BoundedSortableListTreeTableModel(DefaultMutableTreeNode root, ColumnInfo[] columns) {
    super(root, columns);
  }

  public int getChildCount(Object parent) {
    return Math.min(MAX_CHILDREN_COUNT + 1, super.getChildCount(parent));
  }

  public Object getChild(Object parent, int index) {
    if (index == MAX_CHILDREN_COUNT) {
      return TOO_MANY_NODE;
    }
    return ((TreeNode)parent).getChildAt(index);
  }
}
