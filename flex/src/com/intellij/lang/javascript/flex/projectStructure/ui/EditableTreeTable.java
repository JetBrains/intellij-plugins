package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.treeStructure.treetable.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.EditableModel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ksafonov
 */
abstract class EditableTreeTable<T> extends TreeTable {

  private static final Object HEIGHT_TEST_MARKER = new Object();

  public EditableTreeTable(String firstColumnName, ColumnInfo... columns) {
    super(new ListTreeTableModelOnColumns(new DefaultMutableTreeNode(), ArrayUtil.join(new ColumnInfo[]{new FirstColumnInfo(firstColumnName)}, columns)));

    final ColoredTreeCellRenderer r = new ColoredTreeCellRenderer() {

      @Override
      public void customizeCellRenderer(JTree tree,
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
        return r.getTreeCellRendererComponent(tree, value, false, expanded, leaf, row, hasFocus);
      }
    });

    Dimension s = r.getTreeCellRendererComponent(getTree(), new DefaultMutableTreeNode(HEIGHT_TEST_MARKER), false, false, true, 0, false)
      .getPreferredSize();
    getTree().setRowHeight(s.height);
  }

  public void refresh() {
    ((DefaultTreeModel)getTree().getModel()).reload();
  }

  protected abstract void render(SimpleColoredComponent coloredTreeCellRenderer, T userObject);

  //@Override
  //protected TreeTableModelAdapter adapt(TreeTableModel treeTableModel) {
  //  return new EditableModelAdapter(treeTableModel, getTree(), this);
  //}

  public List<T> getItems() {
    int rows = getRowCount();
    List<T> result = new ArrayList<T>(rows);
    for (int row = 0; row < rows; row++) {
      result.add((T)getValueAt(row, 0));
    }
    return result;
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

    public EditableModelAdapter(TreeTableModel treeTableModel, JTree tree, JTable table) {
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
  }

  private static class FirstColumnInfo<T> extends ColumnInfo<DefaultMutableTreeNode, T> {
    public FirstColumnInfo(String name) {
      super(name);
    }

    @Override
    public T valueOf(DefaultMutableTreeNode item) {
      return (T)item.getUserObject();
    }

    public Class getColumnClass() {
      return TreeTableModel.class;
    }
  }
}
