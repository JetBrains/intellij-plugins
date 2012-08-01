package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.SwingUtils;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class TestMethodRunSettingsSection extends AbstractRunSettingsSection {

  private final TestCaseRunSettingsSection myTestCaseRunSettingsSection;
  private final JTextField myTestNameTextField;
  private final JBLabel myLabel;

  TestMethodRunSettingsSection() {
    myTestCaseRunSettingsSection = new TestCaseRunSettingsSection();
    myTestNameTextField = new JTextField();
    myLabel = new JBLabel("Method:");
    setAnchor(SwingUtils.getWiderComponent(myLabel, myTestCaseRunSettingsSection));
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    myTestCaseRunSettingsSection.resetFrom(runSettings);
    myTestNameTextField.setText(runSettings.getTestMethodName());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    myTestCaseRunSettingsSection.applyTo(runSettingsBuilder);
    runSettingsBuilder.setTestMethodName(ObjectUtils.notNull(myTestNameTextField.getText(), ""));
  }

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    JPanel panel = new JPanel(new GridBagLayout());
    {
      GridBagConstraints c = new GridBagConstraints(
        0, 0,
        2, 1,
        0.0, 0.0,
        GridBagConstraints.NORTHWEST,
        GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0),
        0, 0
      );
      JComponent testCaseComponent = myTestCaseRunSettingsSection.getComponent(creationContext);
      panel.add(testCaseComponent, c);
    }

    {
      myLabel.setDisplayedMnemonic('M');
      myLabel.setLabelFor(myTestNameTextField);
      myLabel.setHorizontalAlignment(SwingConstants.RIGHT);
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
      panel.add(myTestNameTextField, c);
      myLabel.setLabelFor(myTestNameTextField);
    }
    SwingUtils.addGreedyBottomRow(panel);
    return panel;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    myLabel.setAnchor(anchor);
    myTestCaseRunSettingsSection.setAnchor(anchor);
  }

}
