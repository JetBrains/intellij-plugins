package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.ObjectUtils;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

class TestCaseRunSettingsSection extends JsFileRunSettingsSection {
  private JTextField myTestCaseNameTextField;
  private JBLabel myLabel;

  TestCaseRunSettingsSection() {
    super();
    if (myLabel == null) {
      myLabel = new JBLabel("Case:");
    }
    setAnchor(super.getAnchor());
  }

  @Override
  protected JComponent getAdditionalComponent() {
    JPanel panel = new JPanel(new GridBagLayout());
    myLabel.setDisplayedMnemonic('e');
    {
      GridBagConstraints c = new GridBagConstraints(
        0, 0,
        1, 1,
        0.0, 0.0,
        GridBagConstraints.WEST,
        GridBagConstraints.NONE,
        new Insets(UIUtil.DEFAULT_VGAP, 0, 0, UIUtil.DEFAULT_HGAP),
        0, 0
      );
      panel.add(myLabel, c);
    }
    {
      GridBagConstraints c = new GridBagConstraints(
        1, 0,
        1, 1,
        1.0, 1.0,
        GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL,
        new Insets(UIUtil.DEFAULT_VGAP, 0, 0, 0),
        0, 0
      );
      myTestCaseNameTextField = new JTextField();
      panel.add(myTestCaseNameTextField, c);
      myLabel.setLabelFor(myTestCaseNameTextField);
    }
    return panel;

  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    if (myLabel == null) {
      myLabel = new JBLabel("Case:");
    }
    myLabel.setAnchor(anchor);
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    super.resetFrom(runSettings);
    myTestCaseNameTextField.setText(runSettings.getTestCaseName());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    super.applyTo(runSettingsBuilder);
    runSettingsBuilder.setTestCaseName(ObjectUtils.notNull(myTestCaseNameTextField.getText(), ""));
  }
}
