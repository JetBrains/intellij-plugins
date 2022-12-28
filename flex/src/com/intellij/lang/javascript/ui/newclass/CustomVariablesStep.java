// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.ui.newclass;

import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.TableUtil;
import com.intellij.ui.table.TableView;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomVariablesStep extends AbstractWizardStepEx {

  private static final class Item {
    private final String name;
    private String value;

    private Item(final String name, final String value) {
      this.name = name;
      this.value = value;
    }
  }

  static final Object ID = new Object();
  private final WizardModel myModel;
  private TableView<Item> myTable;
  private JPanel myPanel;
  private JLabel myTitleLabel;

  private static final ColumnInfo<Item, String> NAME = new ColumnInfo<>(JavaScriptBundle.message("varible.name.column.title")) {
    @Nullable
    @Override
    public String valueOf(final Item item) {
      return item.name;
    }
  };

  private static final ColumnInfo<Item, String> VALUE = new ColumnInfo<>(JavaScriptBundle.message("varible.value.column.title")) {
    @Nullable
    @Override
    public String valueOf(final Item item) {
      return item.value;
    }

    @Override
    public boolean isCellEditable(final Item item) {
      return true;
    }

    @Override
    public void setValue(final Item item, final String value) {
      item.value = value;
    }
  };

  public CustomVariablesStep(WizardModel model) {
    super(null);
    myModel = model;
    myTable.setPreferredScrollableViewportSize(JBUI.size(100, -1));
    myTable.setVisibleRowCount(3);
  }

  @NotNull
  @Override
  public Object getStepId() {
    return ID;
  }

  @Nullable
  @Override
  public Object getNextStepId() {
    return null;
  }

  @Nullable
  @Override
  public Object getPreviousStepId() {
    return MainStep.ID;
  }

  @Override
  public boolean isComplete() {
    return true;
  }

  @Override
  public void _init() {
    String text = JavaScriptBundle.message("custom.variables.step.title.label.text", myModel.getTemplateName());
    myTitleLabel.setText(UIUtil.removeMnemonic(text));
    myTitleLabel.setDisplayedMnemonic(text.charAt(UIUtil.getDisplayMnemonicIndex(text) + 1));

    ListTableModel<Item> model = new ListTableModel<>(NAME, VALUE);
    List<Item> items = ContainerUtil.sorted(
      ContainerUtil.map(myModel.getCustomTemplateAttributes(myModel.getTemplateName()).entrySet(), e -> new Item(e.getKey(), e.getValue())),
    (o1, o2) -> StringUtil.naturalCompare(o1.name, o2.name));
    model.addRows(items);
    myTable.setModelAndUpdateColumns(model);
  }

  @Override
  public void commit(final CommitType commitType) throws CommitStepException {
    TableUtil.stopEditing(myTable);
    List<Item> items = ((ListTableModel)myTable.getModel()).getItems();
    Map<String, String> map = new HashMap<>();
    for (Item item : items) {
      map.put(item.name, item.value);
    }
    myModel.setCustomTemplateAttributes(myModel.getTemplateName(), map);
  }

  @Override
  public JComponent getComponent() {
    return myPanel;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return null;
  }

  public void shown() {
    TableUtil.editCellAt(myTable, 0, 1);
  }
}
