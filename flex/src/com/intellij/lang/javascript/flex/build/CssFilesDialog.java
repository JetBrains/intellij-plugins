package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

public class CssFilesDialog extends AddRemoveTableRowsDialog<String> {
  private final Project myProject;

  public CssFilesDialog(final Project project, final List<String> cssFilesList) {
    super(project, FlexBundle.message("css.files.title"), new ArrayList<String>(cssFilesList));
    myProject = project;
    init();
  }

  protected AddObjectDialog<String> createAddObjectDialog() {
    assert false;
    return null;
  }

  protected TableModelBase createTableModel() {
    return new TableModelBase() {

      public int getColumnCount() {
        return 1;
      }

      public String getColumnName(int column) {
        return "CSS Files";
      }

      public Class getColumnClass(int column) {
        return String.class;
      }

      protected Object getValue(final String cssFilePath, final int column) {
        return FileUtil.toSystemDependentName(cssFilePath);
      }

      public void setValueAt(final Object aValue, final int row, final int column) {
        getCurrentList().set(row, FileUtil.toSystemIndependentName((String)aValue));
      }

      protected void setValue(final String cssFilePath, final int column, final Object aValue) {
        assert false;
      }
    };
  }

  protected void addObject() {
    final VirtualFile[] files = FileChooser.chooseFiles(myProject, new FileChooserDescriptor(true, false, false, false, false, true) {
      public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
        return super.isFileVisible(file, showHiddenFiles) && (file.isDirectory() || "css".equalsIgnoreCase(file.getExtension()));
      }
    });

    for (final VirtualFile file : files) {
      getCurrentList().add(file.getPath());
    }
  }
}
