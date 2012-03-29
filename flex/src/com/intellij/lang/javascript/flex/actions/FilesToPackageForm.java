package com.intellij.lang.javascript.flex.actions;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.ui.AirPackagingConfigurableBase;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.TableUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.CellEditorComponentWithBrowseButton;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions.FilePathAndPathInPackage;

public class FilesToPackageForm {
  private JPanel myMainPanel;
  private JBTable myFilesToPackageTable;

  private final Project myProject;
  private List<FilePathAndPathInPackage> myFilesToPackage = new ArrayList<FilePathAndPathInPackage>();

  private enum Column {
    Path("Path to file or folder", String.class) {
      Object getValue(final FilePathAndPathInPackage row) {
        return FileUtil.toSystemDependentName(row.FILE_PATH);
      }

      void setValue(final List<FilePathAndPathInPackage> myFilesToPackage, final int row, final Object value) {
        myFilesToPackage.get(row).FILE_PATH = FileUtil.toSystemIndependentName(((String)value).trim());
      }
    },

    RelativePath("Its relative path in package", String.class) {
      Object getValue(final FilePathAndPathInPackage row) {
        return FileUtil.toSystemDependentName(row.PATH_IN_PACKAGE);
      }

      void setValue(final List<FilePathAndPathInPackage> myFilePathsToPackage, final int row, final Object value) {
        myFilePathsToPackage.get(row).PATH_IN_PACKAGE = FileUtil.toSystemIndependentName(((String)value).trim());
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
  }

  private void initTable() {
    myFilesToPackageTable = new JBTable();
    myFilesToPackageTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // otherwise model is not in sync with view
    myFilesToPackageTable.setPreferredScrollableViewportSize(new Dimension(400, 150));
    myFilesToPackageTable.setRowHeight(new JTextField("Fake").getPreferredSize().height + myFilesToPackageTable.getRowMargin());

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
    ToolbarDecorator d = ToolbarDecorator.createDecorator(myFilesToPackageTable);
    d.setAddAction(new AnActionButtonRunnable() {
      public void run(AnActionButton button) {
        VirtualFile[] files = FileChooser.chooseFiles(myProject, new FileChooserDescriptor(true, true, false, true, false, true));
        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();

        for (final VirtualFile file : files) {
          final VirtualFile sourceRoot = fileIndex.getSourceRootForFile(file);
          final String relativePath = sourceRoot == null ? null : VfsUtilCore.getRelativePath(file, sourceRoot, '/');
          myFilesToPackage.add(new FilePathAndPathInPackage(file.getPath(), StringUtil.notNullize(relativePath, file.getName())));
        }

        if (files.length > 0) {
          fireDataChanged();

          IdeFocusManager.getInstance(myProject).requestFocus(myFilesToPackageTable, true);
          final int rowCount = myFilesToPackageTable.getRowCount();
          myFilesToPackageTable.setRowSelectionInterval(rowCount - files.length, rowCount - 1);
        }
      }
    });
    d.setRemoveAction(new AnActionButtonRunnable() {
      public void run(AnActionButton anActionButton) {
        TableUtil.stopEditing(myFilesToPackageTable);
        final int[] selectedRows = myFilesToPackageTable.getSelectedRows();
        Arrays.sort(selectedRows);
        for (int i = selectedRows.length - 1; i >= 0; i--) {
          myFilesToPackage.remove(selectedRows[i]);
        }
        fireDataChanged();
      }
    });

    myMainPanel.add(d.createPanel(), BorderLayout.CENTER);
  }

  public void fireDataChanged() {
    ((AbstractTableModel)myFilesToPackageTable.getModel()).fireTableDataChanged();
  }

  public List<FilePathAndPathInPackage> getFilesToPackage() {
    TableUtil.stopEditing(myFilesToPackageTable);
    return myFilesToPackage;
  }

  public void resetFrom(final List<FilePathAndPathInPackage> filesToPackage) {
    myFilesToPackage.clear();
    myFilesToPackage.addAll(filesToPackage);
    fireDataChanged();
  }

  public boolean isModified(final List<FilePathAndPathInPackage> filesToPackage) {
    return !FlexUtils.equalLists(filesToPackage, myFilesToPackage);
  }

  public ActionCallback navigateTo(final AirPackagingConfigurableBase.Location location) {
    if (location == AirPackagingConfigurableBase.Location.FilesToPackage) {
      return IdeFocusManager.findInstance().requestFocus(myFilesToPackageTable, true);
    }
    return new ActionCallback.Done();
  }
}
