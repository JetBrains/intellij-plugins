package com.intellij.lang.javascript.flex.build;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public abstract class AddRemoveTableRowsDialog<T> extends DialogWrapper {

  private final List<T> myList;

  private JPanel myMainPanel;
  private JTable myTable;

  private boolean myEditAddedRow = false;

  public AddRemoveTableRowsDialog(final Project project, final String title, final List<T> list) {
    super(project);
    myList = list;
    setTitle(title);
  }

  protected void init() {
    myMainPanel = new JPanel(new BorderLayout());

    initTable();
    initButtons();

    super.init();
  }

  protected void initTable() {
    myTable = new JBTable();
    myTable.setRowHeight(new JLabel("fj").getPreferredSize().height + 4);
    myTable.setPreferredScrollableViewportSize(new Dimension(400, 150));

    myTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // otherwise model is not in sync with view

    myTable.setModel(createTableModel());
    myTable.setDefaultRenderer(Boolean.class, new NoBackgroundBooleanTableCellRenderer());

    final TableColumnModel columnModel = myTable.getColumnModel();
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      columnModel.getColumn(i).setPreferredWidth(getPreferredColumnWidth(i));
    }
  }

  protected abstract TableModelBase createTableModel();

  protected int getPreferredColumnWidth(final int columnIndex) {
    return 75;
  }

  private void initButtons() {
    ToolbarDecorator d = ToolbarDecorator.createDecorator(myTable);
    d.setAddAction(new AnActionButtonRunnable() {
      public void run(AnActionButton button) {
        addObject();
        ((AbstractTableModel)myTable.getModel()).fireTableDataChanged();
        if (myEditAddedRow) {
          myTable.editCellAt(myTable.getRowCount() - 1, 0);
        }
      }
    });
    d.setRemoveAction(new AnActionButtonRunnable() {
      public void run(AnActionButton anActionButton) {
        final int[] selectedRows = myTable.getSelectedRows();
        Arrays.sort(selectedRows);
        for (int i = selectedRows.length - 1; i >= 0; i--) {
          myList.remove(selectedRows[i]);
        }

        ((AbstractTableModel)myTable.getModel()).fireTableDataChanged();
      }
    });

    myMainPanel.add(d.createPanel(), BorderLayout.CENTER);
  }

  public void setEditAddedRow(final boolean editAddedRow) {
    myEditAddedRow = editAddedRow;
  }

  protected void addObject() {
    final AddObjectDialog<T> dialog = createAddObjectDialog();
    dialog.show();
    if (dialog.isOK()) {
      myList.add(dialog.getAddedObject());
    }
  }

  protected abstract AddObjectDialog<T> createAddObjectDialog();

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  protected void doOKAction() {
    final TableCellEditor cellEditor = myTable.getCellEditor();
    if (cellEditor != null) {
      // apply currently edited value if any
      cellEditor.stopCellEditing();
    }
    super.doOKAction();
  }

  public List<T> getCurrentList() {
    return myList;
  }

  public static abstract class AddObjectDialog<T> extends DialogWrapper {
    protected AddObjectDialog(final Project project) {
      super(project);
    }

    abstract T getAddedObject();
  }

  protected abstract class TableModelBase extends DefaultTableModel {

    public abstract int getColumnCount();

    public int getRowCount() {
      return myList.size();
    }

    public abstract String getColumnName(int column);

    public abstract Class getColumnClass(int column);

    public Object getValueAt(final int row, final int column) {
      return getValue(myList.get(row), column);
    }

    protected abstract Object getValue(final T t, final int column);

    public void setValueAt(final Object aValue, final int row, final int column) {
      setValue(myList.get(row), column, aValue);
    }

    protected abstract void setValue(final T t, final int column, final Object aValue);
  }
}
