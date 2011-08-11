package com.intellij.lang.javascript.flex.actions;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.TableUtil;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.CellEditorComponentWithBrowseButton;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.intellij.lang.javascript.flex.actions.airinstaller.AirInstallerParametersBase.FilePathAndPathInPackage;

public class FilesToPackageForm {
  private JPanel myMainPanel; // needed for form reuse
  private JBTable myFilesToPackageTable;
  private JButton myAddButton;
  private JButton myRemoveButton;

  private final Project myProject;
  private List<FilePathAndPathInPackage> myFilesToPackage = new LinkedList<FilePathAndPathInPackage>();

  private enum Column {
    Path("Path to file or folder", String.class) {
      Object getValue(final FilePathAndPathInPackage row) {
        return row.FILE_PATH;
      }

      void setValue(final List<FilePathAndPathInPackage> myFilesToPackage, final int row, final Object value) {
        myFilesToPackage.get(row).FILE_PATH = (String)value;
      }
    },

    RelativePath("Its relative path in package", String.class) {
      Object getValue(final FilePathAndPathInPackage row) {
        return row.PATH_IN_PACKAGE;
      }

      void setValue(final List<FilePathAndPathInPackage> myFilePathsToPackage, final int row, final Object value) {
        myFilePathsToPackage.get(row).PATH_IN_PACKAGE = (String)value;
      }
    };

    private final String myColumnName;

    private final Class myColumnClass;

    private Column(final String columnName, final Class columnClass) {
      myColumnName = columnName;
      myColumnClass = columnClass;
    }

    public String getColumnName() {
      return myColumnName;
    }

    private Class getColumnClass() {
      return myColumnClass;
    }

    abstract Object getValue(FilePathAndPathInPackage row);

    abstract void setValue(List<FilePathAndPathInPackage> myFilesToPackage, int row, Object value);
  }

  public FilesToPackageForm(final Project project) {
    myProject = project;
    initTable();
    initTableButtons();
    updateRemoveButtonState();
  }

  private void initTable() {
    myFilesToPackageTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // otherwise model is not in sync with view
    myFilesToPackageTable.setRowHeight(20);

    myFilesToPackageTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        updateRemoveButtonState();
      }
    });

    myFilesToPackageTable.setModel(new DefaultTableModel() {

      public int getColumnCount() {
        return Column.values().length;
      }

      public int getRowCount() {
        return myFilesToPackage.size();
      }

      public String getColumnName(int column) {
        return Column.values()[column].getColumnName();
      }

      public Class<?> getColumnClass(int column) {
        return Column.values()[column].getColumnClass();
      }

      public Object getValueAt(int row, int column) {
        return Column.values()[column].getValue(myFilesToPackage.get(row));
      }

      public void setValueAt(Object aValue, int row, int column) {
        Column.values()[column].setValue(myFilesToPackage, row, aValue);
      }
    });

    myFilesToPackageTable.getColumnModel().getColumn(0).setCellEditor(new AbstractTableCellEditor() {
      private CellEditorComponentWithBrowseButton<JTextField> myComponent;

      public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row, int column) {
        final ActionListener listener = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            FileChooserDescriptor d = new FileChooserDescriptor(true, true, false, true, false, false);

            VirtualFile initialFile = LocalFileSystem.getInstance().findFileByPath((String)getCellEditorValue());
            VirtualFile file = FileChooser.chooseFile(myProject, d, initialFile);
            if (file != null) {
              myComponent.getChildComponent().setText(file.getPresentableUrl());
            }
          }
        };

        myComponent = new CellEditorComponentWithBrowseButton<JTextField>(new TextFieldWithBrowseButton(listener), this);
        myComponent.getChildComponent().setText((String)value);
        return myComponent;
      }

      public Object getCellEditorValue() {
        return myComponent.getChildComponent().getText();
      }
    });
  }

  private void initTableButtons() {
    myAddButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        VirtualFile[] files = FileChooser.chooseFiles(myProject, new FileChooserDescriptor(true, true, false, true, false, true));
        for (final VirtualFile file : files) {
          myFilesToPackage.add(new FilePathAndPathInPackage(file.getPresentableUrl(), file.getName()));
          fireDataChanged();
        }
      }
    });

    myRemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (myFilesToPackageTable.isEditing()) {
          myFilesToPackageTable.getCellEditor().stopCellEditing();
        }
        final int[] selectedRows = myFilesToPackageTable.getSelectedRows();
        Arrays.sort(selectedRows);
        for (int i = selectedRows.length - 1; i >= 0; i--) {
          myFilesToPackage.remove(selectedRows[i]);
        }
        fireDataChanged();
      }
    });
  }

  private void updateRemoveButtonState() {
    myRemoveButton.setEnabled(myFilesToPackageTable.getSelectedRowCount() > 0);
  }

  public void setPanelTitle(final String title) {
    myMainPanel.setBorder(IdeBorderFactory.createTitledBorder(title, false, true, true));
  }

  public void stopEditing() {
    TableUtil.stopEditing(myFilesToPackageTable);
  }

  public void fireDataChanged() {
    ((AbstractTableModel)myFilesToPackageTable.getModel()).fireTableDataChanged();
  }

  public List<FilePathAndPathInPackage> getFilesToPackage() {
    return myFilesToPackage;
  }

  public void setFilesToPackage(final List<FilePathAndPathInPackage> filePathAndPathInPackages) {
    myFilesToPackage = filePathAndPathInPackages;
    fireDataChanged();
  }
}
