package com.jetbrains.actionscript.profiler.base;

import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.util.ui.ColumnInfo;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Comparator;

/**
 * @author: Fedor.Korotkov
 */
public class BaseSortableTreeTable extends TreeTable {
  private static final int DEFAULT_ROW_HEIGHT = 20;

  public BaseSortableTreeTable(ColumnInfo[] columns) {
    super(new BoundedSortableListTreeTableModel(new DefaultMutableTreeNode(), columns));
    setAutoCreateRowSorter(false);
    TreeTableRowSorter rowSorter = new TreeTableRowSorter(this);

    for (int i = 0; i < getSortableTreeTableModel().getColumnCount(); ++i) {
      TableCellRenderer tableCellRenderer = getSortableTreeTableModel().getRenderer(i);
      if (tableCellRenderer != null) {
        getColumnModel().getColumn(i).setCellRenderer(tableCellRenderer);
      }

      Comparator comparator = getSortableTreeTableModel().getComparator(i);

      if (comparator != null) {
        rowSorter.setComparator(i, comparator);
      }
    }

    setRowSorter(rowSorter);

    setRowHeight(DEFAULT_ROW_HEIGHT);
  }

  @Override
  public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    JComponent jComponent = (JComponent)super.prepareRenderer(renderer, row, column);
    jComponent.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

    return jComponent;
  }

  @Override
  public String getToolTipText(MouseEvent event) {
    final Point p = event.getPoint();
    final int hitColumnIndex = columnAtPoint(p);
    final int hitRowIndex = rowAtPoint(p);

    if ((hitColumnIndex != -1) && (hitRowIndex != -1)) {
      return getValueAt(hitRowIndex, hitColumnIndex).toString();
    }
    return getToolTipText();
  }

  public void reload() {
    getSortableTreeTableModel().reload();
  }

  public SortableListTreeTableModel getSortableTreeTableModel() {
    return (SortableListTreeTableModel)getTableModel();
  }
}
