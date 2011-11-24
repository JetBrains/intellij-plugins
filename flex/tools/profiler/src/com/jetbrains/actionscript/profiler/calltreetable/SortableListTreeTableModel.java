package com.jetbrains.actionscript.profiler.calltreetable;

import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Comparator;

public class SortableListTreeTableModel extends ListTreeTableModel {
  private final ColumnInfo[] myColumns;

  public SortableListTreeTableModel(DefaultMutableTreeNode root, ColumnInfo[] columns) {
    super(root, columns);
    myColumns = columns;
  }

  @Nullable
  protected Comparator getComparator(int column) {
    return myColumns[column].getComparator();
  }

  @Nullable
  protected TableCellRenderer getRenderer(int column) {
    return myColumns[column].getRenderer(null);
  }
}
