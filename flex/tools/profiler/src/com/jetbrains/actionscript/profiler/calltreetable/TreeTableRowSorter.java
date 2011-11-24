package com.jetbrains.actionscript.profiler.calltreetable;

import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TreeTableRowSorter extends TableRowSorter {
  private final TreeTable table;
  private List<? extends SortKey> sortKeys = new ArrayList<SortKey>();

  public TreeTableRowSorter(CallTreeTable table) {
    super(table.getModel());
    this.table = table;
  }

  @Override
  public void sort() {
    //do nothing
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
    if (sortKeys.size() > 0 && sortKeys.get(0) instanceof SortKey) {
      setSortKey((SortKey)sortKeys.get(0));
      fireSortOrderChanged();
    }
  }

  private void setSortKey(SortKey key) {
    if (key.getSortOrder() == SortOrder.UNSORTED) {
      return;
    }

    final int columnModelIndex = key.getColumn();
    TableColumnModel colModel = table.getColumnModel();
    int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

    if (modelIndex < 0 || getComparator(modelIndex) == null) {
      return;
    }

    final Comparator<DefaultMutableTreeNode> comparator =
      new ComparatorWrapper<DefaultMutableTreeNode>(getComparator(modelIndex), key.getSortOrder() == SortOrder.ASCENDING);

    TreeUtil.sort((DefaultTreeModel)table.getTableModel(), comparator);
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
