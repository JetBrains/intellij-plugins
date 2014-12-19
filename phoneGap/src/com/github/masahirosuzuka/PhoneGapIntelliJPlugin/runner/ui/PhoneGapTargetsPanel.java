package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUtil;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.util.containers.ContainerUtil;

import javax.swing.*;
import java.util.Set;

public class PhoneGapTargetsPanel extends PanelWithAsyncLoad<String> {

  @Override
  protected void doUpdateValues(Set<String> values) {
    String selectedValue = getSelectedValue();
    getTargetsField().setHistory(ContainerUtil.newArrayList(values));
    PhoneGapUtil.setTextFieldWithHistory(getTargetsField(), selectedValue);
  }

  public boolean isEnabled() {
    return getTargetsField().isEnabled();
  }

  public void setEnabled(boolean enabled) {
    myComboBox.setEnabled(enabled);
    myActionPanel.setVisible(enabled);
  }

  @Override
  protected boolean shouldUpdate(Set<String> values) {
    return true;
  }

  @Override
  protected String getSelectedValue() {
    return getTargetsField().getText();
  }

  public TextFieldWithHistory getTargetsField() {
    return (TextFieldWithHistory)myComboBox;
  }

  @Override
  protected JComboBox createValuesComboBox() {
    return new TextFieldWithHistory();
  }
}
