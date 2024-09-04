// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.flex.model.bc.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.build.AddRemoveTableRowsDialog;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.TableUtil;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.update.UiNotifyConnector;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.intellij.flex.model.bc.CompilerOptionInfo.ListElementType;
import static com.intellij.lang.javascript.flex.projectStructure.ui.CompilerOptionsConfigurable.ExtensionAwareFileChooserDescriptor;

public class RepeatableValueDialog extends AddRemoveTableRowsDialog<StringBuilder> {
  private final CompilerOptionInfo myInfo;

  public RepeatableValueDialog(final Project project,
                               final String title,
                               final List<StringBuilder> value,
                               final CompilerOptionInfo info) {
    this(project, title, value, info, null);
  }

  public RepeatableValueDialog(final Project project,
                               final String title,
                               final List<StringBuilder> value,
                               final CompilerOptionInfo info,
                               final @Nullable String autoAddedConditionalCompilerDefinition) {
    super(project, title, value);
    assert info.TYPE == CompilerOptionInfo.OptionType.List;
    myInfo = info;
    setEditAddedRow(true);

    init();

    myTable.setDefaultEditor(VirtualFile.class, new AbstractTableCellEditor() {
      public TextFieldWithBrowseButton myTextWithBrowse = new TextFieldWithBrowseButton();
      public ExtensionAwareFileChooserDescriptor myFileChooserDescriptor = new ExtensionAwareFileChooserDescriptor();

      {
        myTextWithBrowse.addBrowseFolderListener(myProject, myFileChooserDescriptor);
      }

      @Override
      public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        myFileChooserDescriptor.setAllowedExtensions(myInfo.LIST_ELEMENTS[column].FILE_EXTENSIONS);
        myTextWithBrowse.setText(FileUtil.toSystemDependentName(String.valueOf(value)));
        return myTextWithBrowse;
      }

      @Override
      public Object getCellEditorValue() {
        return FileUtil.toSystemIndependentName(myTextWithBrowse.getText().trim());
      }
    });

    if (autoAddedConditionalCompilerDefinition != null) {
      assert "compiler.define".equals(info.ID) && info.LIST_ELEMENTS.length == 2 : info.ID;
      getCurrentList().add(new StringBuilder(autoAddedConditionalCompilerDefinition).append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR));

      UiNotifyConnector.doWhenFirstShown(myTable, () -> {
        final int rowCount = myTable.getRowCount();
        if (rowCount > 0) {
          myTable.addRowSelectionInterval(rowCount - 1, rowCount - 1);

          // todo this doesn't work because editing is immediately stopped in JBTable.columnMarginChanged()
          TableUtil.editCellAt(myTable, rowCount - 1, myInfo.LIST_ELEMENTS.length - 1);
        }
      });
    }
  }

  @Override
  public @Nullable JComponent getPreferredFocusedComponent() {
    return myTable;
  }

  @Override
  protected TableModelBase createTableModel() {
    return new TableModelBase() {
      @Override
      public int getColumnCount() {
        return myInfo.LIST_ELEMENTS.length;
      }

      @Override
      public @Nullable String getColumnName(final int column) {
        return myInfo.LIST_ELEMENTS.length == 1 ? null : myInfo.LIST_ELEMENTS[column].DISPLAY_NAME;
      }

      @Override
      public Class getColumnClass(final int column) {
        final ListElementType type = myInfo.LIST_ELEMENTS[column].LIST_ELEMENT_TYPE;
        return (type == ListElementType.File || type == ListElementType.FileOrFolder)
               ? VirtualFile.class
               : type == ListElementType.Boolean
                 ? Boolean.class
                 : String.class;
      }

      @Override
      protected Object getValue(final StringBuilder s, final int column) {
        final String value = StringUtil.split(s.toString(), CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR, true, false).get(column);
        final ListElementType type = myInfo.LIST_ELEMENTS[column].LIST_ELEMENT_TYPE;
        return (type == ListElementType.File || type == ListElementType.FileOrFolder)
               ? FileUtil.toSystemDependentName(value)
               : type == ListElementType.Boolean
                 ? Boolean.valueOf(value)
                 : value;
      }

      @Override
      protected void setValue(final StringBuilder s, final int column, final Object aValue) {
        final List<String> parts =
          StringUtil.split(s.toString(), CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR, true, false);
        s.delete(0, s.length());

        for (int i = 0; i < column; i++) {
          s.append(parts.get(i)).append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR);
        }

        final ListElementType type = myInfo.LIST_ELEMENTS[column].LIST_ELEMENT_TYPE;
        final String fixedValue = (type == ListElementType.File || type == ListElementType.FileOrFolder)
                                  ? FileUtil.toSystemIndependentName(String.valueOf(aValue))
                                  : String.valueOf(aValue);
        s.append(fixedValue);

        for (int i = column + 1; i < myInfo.LIST_ELEMENTS.length; i++) {
          s.append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR).append(parts.get(i));
        }
      }
    };
  }

  @Override
  protected boolean addObject() {
    final CompilerOptionInfo.ListElement firstElement = myInfo.LIST_ELEMENTS[0];
    if (myInfo.LIST_ELEMENTS.length == 1 &&
        (firstElement.LIST_ELEMENT_TYPE == ListElementType.File ||
         firstElement.LIST_ELEMENT_TYPE == ListElementType.FileOrFolder)) {
      final FileChooserDescriptor descriptor = firstElement.LIST_ELEMENT_TYPE == ListElementType.File
                                               ? FlexUtils.createFileChooserDescriptor(firstElement.FILE_EXTENSIONS)
                                               : FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
      final VirtualFile file = FileChooser.chooseFile(descriptor, myProject, null);
      if (file != null) {
        getCurrentList().add(new StringBuilder(file.getPath()));
        return true;
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
      return true;
    }

    return false;
  }

  @Override
  protected @Nullable ValidationInfo doValidate() {
    if ("compiler.define".equals(myInfo.ID)) {
      for (StringBuilder builder : getCurrentList()) {
        final List<String> strings = StringUtil.split(builder.toString(), CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR, true, false);
        assert strings.size() == 2 : builder;

        final String name = strings.get(0);
        final String value = strings.get(1);

        if (name.isEmpty()) {
          return new ValidationInfo("Missing constant name");
        }
        final int colonIndex = name.indexOf("::");
        if (colonIndex <= 0) {
          return new ValidationInfo("Incorrect name: " + name);
        }

        if (value.isEmpty()) {
          return new ValidationInfo("Constant " + name + " has empty value");
        }
      }
    }

    return null;
  }
}
