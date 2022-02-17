package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.flex.model.bc.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.lang.javascript.ui.JSClassChooserDialog;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.AbstractTableCellEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RLMsDialog extends RepeatableValueDialog {
  private final Module myModule;

  public RLMsDialog(final Module module, final Collection<FlexBuildConfiguration.RLMInfo> rlms) {
    super(module.getProject(), FlexBundle.message("rlms.dialog.title"), toStringBuilderList(rlms), CompilerOptionInfo.RLMS_INFO_FOR_UI);
    myModule = module;

    setEditAddedRow(false);
    myTable.getColumnModel().getColumn(0).setCellRenderer(createMainClassRenderer(module));
    myTable.getColumnModel().getColumn(0).setCellEditor(createMainClassEditor(module));
  }

  private static List<StringBuilder> toStringBuilderList(final Collection<FlexBuildConfiguration.RLMInfo> rlms) {
    final List<StringBuilder> result = new ArrayList<>();
    for (FlexBuildConfiguration.RLMInfo rlm : rlms) {
      result.add(new StringBuilder()
                   .append(rlm.MAIN_CLASS)
                   .append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR)
                   .append(rlm.OUTPUT_FILE)
                   .append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR)
                   .append(rlm.OPTIMIZE));
    }
    return result;
  }

  private static DefaultTableCellRenderer createMainClassRenderer(final Module module) {
    return new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
        final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        final String fqn = String.valueOf(value);
        setText(fqn);
        setForeground(isSelected
                      ? table.getSelectionForeground()
                      : FlexUtils.getPathToMainClassFile(fqn, module).isEmpty()
                        ? JBColor.RED
                        : table.getForeground());
        return component;
      }
    };
  }

  private static AbstractTableCellEditor createMainClassEditor(final Module module) {
    return new AbstractTableCellEditor() {
      private JSReferenceEditor mainClassComponent;

      @Override
      public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        mainClassComponent =
          JSReferenceEditor.forClassName("", module.getProject(), null, module.getModuleScope(false), null, null,
                                         FlexBundle.message("choose.rlm.main.class.title"));
        mainClassComponent.setText(String.valueOf(value));
        return mainClassComponent;
      }

      @Override
      public Object getCellEditorValue() {
        return mainClassComponent.getText();
      }
    };
  }

  @Override
  protected boolean addObject() {
    if (DumbService.isDumb(myProject)) {
      return super.addObject();
    }

    final JSClassChooserDialog chooser = new JSClassChooserDialog(myProject, FlexBundle.message("choose.rlm.main.class.title"),
                                                                  myModule.getModuleScope(false), null, null);
    if (chooser.showDialog()) {
      final JSClass clazz = chooser.getSelectedClass();
      if (clazz != null) {
        final StringBuilder b = new StringBuilder();
        b.append(clazz.getQualifiedName());
        b.append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR);
        b.append(BCUtils.suggestRLMOutputPath(clazz.getQualifiedName()));
        b.append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR);
        b.append("true");
        getCurrentList().add(b);
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }

  public Collection<FlexBuildConfiguration.RLMInfo> getRLMs() {
    final List<StringBuilder> currentList = getCurrentList();
    final Collection<FlexBuildConfiguration.RLMInfo> result = new ArrayList<>(currentList.size());

    for (StringBuilder listEntry : currentList) {
      final List<String> parts = StringUtil.split(listEntry.toString(), CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR, true, false);
      assert parts.size() == 3 : listEntry;
      result.add(new FlexBuildConfiguration.RLMInfo(parts.get(0), parts.get(1), Boolean.parseBoolean(parts.get(2))));
    }

    return result;
  }

  @Override
  protected String getHelpId() {
    return "Runtime-Loaded_Modules_dialog";
  }
}
