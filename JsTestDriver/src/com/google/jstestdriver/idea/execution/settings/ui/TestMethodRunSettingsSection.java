package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.ObjectUtils;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

class TestMethodRunSettingsSection extends TestCaseRunSettingsSection {
  private JTextField myTestNameTextField;
  private JBLabel myLabel;

  TestMethodRunSettingsSection() {
    super();
    if (myLabel == null) {
      myLabel = new JBLabel("Method:");
    }
    setAnchor(super.getAnchor());
  }

  @Override
  protected JComponent getAdditionalComponent() {
    JPanel panel = new JPanel(new GridBagLayout());
    {
      GridBagConstraints c = new GridBagConstraints(
          0, 0,
          2, 1,
          1.0, 0.0,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0),
          0, 0
      );
      panel.add(super.getAdditionalComponent(), c);
    }

    myLabel.setDisplayedMnemonic('M');
    {
      GridBagConstraints c = new GridBagConstraints(
          0, 1,
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
          1, 1,
          1, 1,
          1.0, 0.0,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(UIUtil.DEFAULT_VGAP, 0, 0, 0),
          0, 0
      );
      myTestNameTextField = new JTextField();
      panel.add(myTestNameTextField, c);
      myLabel.setLabelFor(myTestNameTextField);
    }
    return panel;
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    super.resetFrom(runSettings);
    myTestNameTextField.setText(runSettings.getTestMethodName());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    super.applyTo(runSettingsBuilder);
    runSettingsBuilder.setTestMethodName(ObjectUtils.notNull(myTestNameTextField.getText(), ""));
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    if (myLabel == null) {
      myLabel = new JBLabel("Method:");
    }
    myLabel.setAnchor(anchor);
  }
}
