package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.build.AddRemoveTableRowsDialog;
import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.AbstractTableCellEditor;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.intellij.lang.javascript.flex.projectStructure.ui.CompilerOptionsConfigurable.ExtensionAwareFileChooserDescriptor;

public class RepeatableValueDialog extends AddRemoveTableRowsDialog<StringBuilder> {
  private CompilerOptionInfo myInfo;

  public RepeatableValueDialog(final Project project,
                               final String title,
                               final List<StringBuilder> value,
                               final CompilerOptionInfo info) {
    super(project, title, value);
    assert info.TYPE == CompilerOptionInfo.OptionType.List;
    myInfo = info;
    setEditAddedRow(true);

    init();

    myTable.setDefaultEditor(VirtualFile.class, new AbstractTableCellEditor() {
      public TextFieldWithBrowseButton myTextWithBrowse = new TextFieldWithBrowseButton();
      public ExtensionAwareFileChooserDescriptor myFileChooserDescriptor = new ExtensionAwareFileChooserDescriptor();

      {
        myTextWithBrowse.addBrowseFolderListener(null, null, myProject, myFileChooserDescriptor);
      }

      public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        myFileChooserDescriptor.setAllowedExtension(myInfo.LIST_ELEMENTS[column].FILE_EXTENSION);
        myTextWithBrowse.setText(FileUtil.toSystemDependentName(String.valueOf(value)));
        return myTextWithBrowse;
      }

      public Object getCellEditorValue() {
        return FileUtil.toSystemIndependentName(myTextWithBrowse.getText().trim());
      }
    });
  }

  protected TableModelBase createTableModel() {
    return new TableModelBase() {
      public int getColumnCount() {
        return myInfo.LIST_ELEMENTS.length;
      }

      public String getColumnName(final int column) {
        return myInfo.LIST_ELEMENTS[column].DISPLAY_NAME;
      }

      public Class getColumnClass(final int column) {
        return myInfo.LIST_ELEMENTS[column].LIST_ELEMENT_TYPE == CompilerOptionInfo.ListElementType.File ? VirtualFile.class : String.class;
      }

      protected Object getValue(final StringBuilder s, final int column) {
        final String value = StringUtil.split(s.toString(), CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR, true, false).get(column);
        return myInfo.LIST_ELEMENTS[column].LIST_ELEMENT_TYPE == CompilerOptionInfo.ListElementType.File
               ? FileUtil.toSystemDependentName(value)
               : value;
      }

      protected void setValue(final StringBuilder s, final int column, final Object aValue) {
        final List<String> parts =
          StringUtil.split(s.toString(), String.valueOf(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR), true, false);
        s.delete(0, s.length());

        for (int i = 0; i < column; i++) {
          s.append(parts.get(i)).append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR);
        }

        final String fixedValue = myInfo.LIST_ELEMENTS[column].LIST_ELEMENT_TYPE == CompilerOptionInfo.ListElementType.File
                                  ? FileUtil.toSystemIndependentName(String.valueOf(aValue))
                                  : String.valueOf(aValue);
        s.append(fixedValue);

        for (int i = column + 1; i < myInfo.LIST_ELEMENTS.length; i++) {
          s.append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR).append(parts.get(i));
        }
      }
    };
  }

  protected void addObject() {
    if (myInfo.LIST_ELEMENTS.length == 1 && myInfo.LIST_ELEMENTS[0].LIST_ELEMENT_TYPE == CompilerOptionInfo.ListElementType.File) {
      final String extension = myInfo.LIST_ELEMENTS[0].FILE_EXTENSION;
      final VirtualFile file = FileChooser.chooseFile(myProject, FlexUtils.createFileChooserDescriptor(extension));
      if (file != null) {
        getCurrentList().add(new StringBuilder(file.getPath()));
      }
    }
    else {
      final StringBuilder b = new StringBuilder();
      boolean first = true;
      for (CompilerOptionInfo.ListElement listElement : myInfo.LIST_ELEMENTS) {
        if (first) {
          first = false;
        }
        else {
          b.append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR);
        }
        b.append(listElement.DEFAULT_VALUE);
      }
      getCurrentList().add(b);
    }
  }
}
