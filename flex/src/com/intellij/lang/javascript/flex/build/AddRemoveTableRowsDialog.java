package com.intellij.lang.javascript.flex.build;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

public abstract class AddRemoveTableRowsDialog<T> extends DialogWrapper {

  private final List<T> myList;

  private JPanel myMainPanel;
  private JTable myTable;
  private JButton myAddButton;
  private JButton myRemoveButton;

  public AddRemoveTableRowsDialog(final Project project, final String title, final List<T> list) {
    super(project);
    myList = list;
    setTitle(title);
  }

  protected void init() {
    initTable();
    initButtons();

    super.init();
    updateControls();
  }

  protected void initTable() {
    myTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // otherwize model is not in sync with view

    myTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        updateControls();
      }
    });

    myTable.setModel(getTableModel());
    myTable.setDefaultRenderer(Boolean.class, new NoBackgroundBooleanTableCellRenderer());

    final TableColumnModel columnModel = myTable.getColumnModel();
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      columnModel.getColumn(i).setPreferredWidth(getPreferredColumnWidth(i));
    }
  }

  protected abstract TableModelBase getTableModel();

  protected int getPreferredColumnWidth(final int columnIndex) {
    return 75;
  }

  private void initButtons() {
    myAddButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addObject();
        ((AbstractTableModel)myTable.getModel()).fireTableDataChanged();
      }
    });

    myRemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final int[] selectedRows = myTable.getSelectedRows();
        Arrays.sort(selectedRows);
        for (int i = selectedRows.length - 1; i >= 0; i--) {
          myList.remove(selectedRows[i]);
        }

        ((AbstractTableModel)myTable.getModel()).fireTableDataChanged();
      }
    });
  }

  protected void addObject() {
    final AddObjectDialog<T> dialog = createAddObjectDialog();
    dialog.show();
    if (dialog.isOK()) {
      myList.add(dialog.getAddedObject());
    }
  }

  protected abstract AddObjectDialog<T> createAddObjectDialog();

  private void updateControls() {
    myRemoveButton.setEnabled(myTable.getSelectedRowCount() > 0);
  }

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
