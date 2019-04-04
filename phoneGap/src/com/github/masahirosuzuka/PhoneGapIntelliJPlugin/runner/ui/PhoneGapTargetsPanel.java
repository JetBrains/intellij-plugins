package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUtil;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.ReloadableComboBoxPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Set;

public class PhoneGapTargetsPanel extends ReloadableComboBoxPanel<String> {

  @Override
  protected void doUpdateValues(@NotNull Set<String> values) {
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
  public String getSelectedValue() {
    return getTargetsField().getText();
  }

  public TextFieldWithHistory getTargetsField() {
    return (TextFieldWithHistory)myComboBox;
  }

  @NotNull
  @Override
  protected JComboBox<String> createValuesComboBox() {
    return new TextFieldWithHistory();
  }
}
