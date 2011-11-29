package com.jetbrains.actionscript.profiler.calltreetable;

import com.intellij.util.ui.tree.TreeUtil;
import com.jetbrains.actionscript.profiler.util.JTreeUtil;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TreeTableRowSorter extends TableRowSorter {
  private final CallTreeTable table;
  private List<? extends SortKey> sortKeys = new ArrayList<SortKey>();

  public TreeTableRowSorter(CallTreeTable table) {
    super(table.getModel());
    this.table = table;
  }

  @Override
  public List<? extends SortKey> getSortKeys() {
    return sortKeys;
  }

  @Override
  public void setSortKeys(List sortKeys) {
    if (sortKeys == null) {
      return;
    }
    this.sortKeys = sortKeys;
    sort();
  }

  @Override
  public void sort() {
    if (sortKeys.size() > 0) {
      sortOnKey(sortKeys.get(0));
    }
  }

  private void sortOnKey(SortKey key) {
    if (key.getSortOrder() == SortOrder.UNSORTED) {
      return;
    }

    final int columnModelIndex = key.getColumn();
    TableColumnModel colModel = table.getColumnModel();
    int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

    if (modelIndex < 0 || getComparator(modelIndex) == null) {
      return;
    }

    final Comparator<TreeNode> comparator =
      new ComparatorWrapper<TreeNode>(getComparator(modelIndex), key.getSortOrder() == SortOrder.ASCENDING);

    final List<TreePath> paths = TreeUtil.collectExpandedPaths(table.getTree());
    if (JTreeUtil.isSorted(paths, comparator)) {
      return;
    }

    for (TreePath path : paths) {
      Object node = path.getLastPathComponent();
      if (node instanceof DefaultMutableTreeNode) {
        JTreeUtil.sortChildren((DefaultMutableTreeNode)node, comparator);
      }
    }

    table.reload();
    TreeUtil.restoreExpandedPaths(table.getTree(), paths);
  }

  private static class ComparatorWrapper<T> implements Comparator<T> {
    private final Comparator<T> comparator;
    private final boolean reverse;

    public ComparatorWrapper(Comparator<T> comparator, boolean reverse) {
      this.comparator = comparator;
      this.reverse = reverse;
    }

    @Override
    public int compare(T o1, T o2) {
      return reverse ? -comparator.compare(o1, o2) : comparator.compare(o1, o2);
    }
  }
}
