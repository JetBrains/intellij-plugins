package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.build.AddRemoveTableRowsDialog;
import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

import java.util.List;

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
        return String.class; // todo special renderer/editor for File type
      }

      protected Object getValue(final StringBuilder s, final int column) {
        return StringUtil.split(s.toString(), String.valueOf(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR), true, false).get(column);
      }

      protected void setValue(final StringBuilder s, final int column, final Object aValue) {
        final List<String> parts =
          StringUtil.split(s.toString(), String.valueOf(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR), true, false);
        s.delete(0, s.length());

        for (int i = 0; i < column; i++) {
          s.append(parts.get(i)).append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR);
        }

        s.append(aValue);

        for (int i = column + 1; i < myInfo.LIST_ELEMENTS.length; i++) {
          s.append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR).append(parts.get(i));
        }
      }
    };
  }

  protected void addObject() {
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

  protected AddObjectDialog<StringBuilder> createAddObjectDialog() {
    assert false;
    return null;
  }
}
