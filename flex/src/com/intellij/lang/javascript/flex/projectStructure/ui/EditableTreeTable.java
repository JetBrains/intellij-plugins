package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.treeStructure.treetable.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.EditableModel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

abstract class EditableTreeTable<T> extends TreeTable {

  private static final Object HEIGHT_TEST_MARKER = new Object();

  EditableTreeTable(String firstColumnName, ColumnInfo... columns) {
    super(new ListTreeTableModelOnColumns(new DefaultMutableTreeNode(),
                                          ArrayUtil.mergeArrays(new ColumnInfo[]{new FirstColumnInfo(firstColumnName)}, wrap(columns))));

    final ColoredTreeCellRenderer r = new ColoredTreeCellRenderer() {

      @Override
      public void customizeCellRenderer(@NotNull JTree tree,
                                        Object value,
                                        boolean selected,
                                        boolean expanded,
                                        boolean leaf,
                                        int row,
                                        boolean hasFocus) {
        Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
        if (userObject == HEIGHT_TEST_MARKER) {
          return;
        }
        render(this, (T)userObject);
        setPaintFocusBorder(false);
      }
    };

    setTreeCellRenderer(new TreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(JTree tree,
                                                    Object value,
                                                    boolean selected,
                                                    boolean expanded,
                                                    boolean leaf,
                                                    int row,
                                                    boolean hasFocus) {
        return r.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, true);
      }
    });

    Dimension s = r.getTreeCellRendererComponent(getTree(), new DefaultMutableTreeNode(HEIGHT_TEST_MARKER), false, false, true, 0, false)
      .getPreferredSize();
    getTree().setRowHeight(s.height);
  }

  // copied from TableView
  public void updateColumnSizes() {
    final JTableHeader header = getTableHeader();
    final TableCellRenderer defaultRenderer = header == null? null : header.getDefaultRenderer();

    ColumnInfo[] columns = ((ListTreeTableModelOnColumns)getTableModel()).getColumnInfos();
    for (int i = 0; i < columns.length; i++) {
      final ColumnInfo columnInfo = columns[i];
      final TableColumn column = getColumnModel().getColumn(i);

      final Component headerComponent = defaultRenderer == null? null :
        defaultRenderer.getTableCellRendererComponent(this, column.getHeaderValue(), false, false, 0, 0);
      final Dimension headerSize = headerComponent == null ? JBUI.emptySize() : headerComponent.getPreferredSize();
      final String maxStringValue;
      final String preferredValue;
      if (columnInfo.getWidth(this) > 0) {
        int width = columnInfo.getWidth(this);
        column.setMaxWidth(width);
        column.setPreferredWidth(width);
        column.setMinWidth(width);
      }
      else if ((maxStringValue = columnInfo.getMaxStringValue()) != null) {
        int width = getFontMetrics(getFont()).stringWidth(maxStringValue) + columnInfo.getAdditionalWidth();
        width = Math.max(width, headerSize.width);
        column.setPreferredWidth(width);
        column.setMaxWidth(width);
      }
      else if ((preferredValue = columnInfo.getPreferredStringValue()) != null) {
        int width = getFontMetrics(getFont()).stringWidth(preferredValue) + columnInfo.getAdditionalWidth();
        width = Math.max(width, headerSize.width);
        column.setPreferredWidth(width);
      }
    }
  }

  @Override
  public void setModel(TreeTableModel treeTableModel) {
    super.setModel(treeTableModel);
    updateColumnSizes();
  }

  // copied from TableView
  @Override
  public TableCellRenderer getCellRenderer(int row, int column) {
    ListTreeTableModelOnColumns model = (ListTreeTableModelOnColumns)getTableModel();
    ColumnInfo columnInfo = model.getColumnInfos()[convertColumnIndexToModel(column)];
    T item = (T)getValueAt(convertRowIndexToModel(row), 0);
    final TableCellRenderer renderer = columnInfo.getCustomizedRenderer(item, columnInfo.getRenderer(item));
    return renderer == null ? super.getCellRenderer(row, column) : renderer;
  }

  // copied from TableView
  @Override
  public TableCellEditor getCellEditor(int row, int column) {
    ListTreeTableModelOnColumns model = (ListTreeTableModelOnColumns)getTableModel();
    final ColumnInfo columnInfo = model.getColumnInfos()[convertColumnIndexToModel(column)];
    T item = (T)getValueAt(convertRowIndexToModel(row), 0);
    final TableCellEditor editor = columnInfo.getEditor(item);
    return editor == null ? super.getCellEditor(row, column) : editor;
  }

  public void refreshItemAt(final Integer row) {
    Object node = getTree().getPathForRow(row).getLastPathComponent();
    ((DefaultTreeModel)getTree().getModel()).nodeChanged((TreeNode)node);
  }

  private static ColumnInfo[] wrap(ColumnInfo[] columns) {
    return ContainerUtil.map2Array(columns, ColumnInfo.class, columnInfo -> new ColumnInfoWrapper(columnInfo));
  }

  public void refresh() {
    List<TreePath> expandedPaths = TreeUtil.collectExpandedPaths(getTree());
    ((DefaultTreeModel)getTree().getModel()).reload();
    TreeUtil.restoreExpandedPaths(getTree(), expandedPaths);
  }

  protected abstract void render(SimpleColoredComponent coloredTreeCellRenderer, T userObject);

  //@Override
  //protected TreeTableModelAdapter adapt(TreeTableModel treeTableModel) {
  //  return new EditableModelAdapter(treeTableModel, getTree(), this);
  //}

  public List<T> getItems() {
    int rows = getRowCount();
    List<T> result = new ArrayList<>(rows);
    for (int row = 0; row < rows; row++) {
      result.add(getItemAt(row));
    }
    return result;
  }

  public T getItemAt(int row) {
    return (T)getValueAt(row, 0);
  }

  public DefaultMutableTreeNode getRoot() {
    return (DefaultMutableTreeNode)getTree().getModel().getRoot();
  }

  @Override
  public TreeTableCellRenderer createTableRenderer(TreeTableModel treeTableModel) {
    TreeTableCellRenderer r = super.createTableRenderer(treeTableModel);
    r.setDefaultBorder(null);
    return r;
  }

  private static class EditableModelAdapter extends TreeTableModelAdapter implements EditableModel {

    EditableModelAdapter(TreeTableModel treeTableModel, JTree tree, JTable table) {
      super(treeTableModel, tree, table);
    }

    @Override
    public void addRow() {
    }

    @Override
    public void removeRow(int index) {
    }

    @Override
    public void exchangeRows(int oldIndex, int newIndex) {
    }

    @Override
    public boolean canExchangeRows(int oldIndex, int newIndex) {
      return false;
    }
  }

  private static class FirstColumnInfo<T> extends ColumnInfo<DefaultMutableTreeNode, T> {
    FirstColumnInfo(String name) {
      super(name);
    }

    @Override
    public T valueOf(DefaultMutableTreeNode treeNode) {
      return (T)treeNode.getUserObject();
    }

    @Override
    public Class getColumnClass() {
      return TreeTableModel.class;
    }
  }

  private static class ColumnInfoWrapper<T, Aspect> extends ColumnInfo<Object, Aspect> {
    private final ColumnInfo<T, Aspect> myDelegate;

    ColumnInfoWrapper(ColumnInfo<T, Aspect> columnInfo) {
      super(columnInfo.getName());
      myDelegate = columnInfo;
    }

    @Override
    public Class getColumnClass() {
      return ColumnInfoWrapper.class;
    }

    @Override
    public Aspect valueOf(Object item) {
      return myDelegate.valueOf((T)((DefaultMutableTreeNode)item).getUserObject());
    }

    @Override
    public TableCellRenderer getRenderer(Object item) {
      return myDelegate.getRenderer((T)item);
    }

    @Override
    public TableCellRenderer getCustomizedRenderer(Object item, TableCellRenderer renderer) {
      return myDelegate.getCustomizedRenderer((T)item, renderer);
    }

    @Override
    public TableCellEditor getEditor(Object item) {
      return myDelegate.getEditor((T)item);
    }

    @Override
    public boolean isCellEditable(Object item) {
      return myDelegate.isCellEditable((T)((DefaultMutableTreeNode)item).getUserObject());
    }

    @Override
    public void setValue(Object item, Aspect value) {
      myDelegate.setValue((T)((DefaultMutableTreeNode)item).getUserObject(), value);
    }

    @Override
    public int getWidth(JTable table) {
      return myDelegate.getWidth(table);
    }

    @Override
    public String getMaxStringValue() {
      return myDelegate.getMaxStringValue();
    }
  }
}
