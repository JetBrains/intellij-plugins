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

public class TestCaseRunSettingsSection extends AbstractRunSettingsSection {

  private final JsFileRunSettingsSection myJsFileRunSettingsSection;
  private final JTextField myTestCaseNameTextField;
  private final JBLabel myLabel;

  TestCaseRunSettingsSection() {
    myJsFileRunSettingsSection = new JsFileRunSettingsSection();
    myTestCaseNameTextField = new JTextField();
    myLabel = new JBLabel("Case:");
    setAnchor(SwingUtils.getWiderComponent(myLabel, myJsFileRunSettingsSection));
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    myJsFileRunSettingsSection.resetFrom(runSettings);
    myTestCaseNameTextField.setText(runSettings.getTestCaseName());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    myJsFileRunSettingsSection.applyTo(runSettingsBuilder);
    runSettingsBuilder.setTestCaseName(ObjectUtils.notNull(myTestCaseNameTextField.getText(), ""));
  }

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    JPanel panel = new JPanel(new GridBagLayout());
    {
      GridBagConstraints c = new GridBagConstraints(
        0, 0,
        2, 1,
        1.0, 0.0,
        GridBagConstraints.NORTHWEST,
        GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 0),
        0, 0
      );
      JComponent jsFileComponent = myJsFileRunSettingsSection.getComponent(creationContext);
      panel.add(jsFileComponent, c);
    }
    {
      myLabel.setDisplayedMnemonic('e');
      myLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      GridBagConstraints c = new GridBagConstraints(
        0, 1,
        1, 1,
        0.0, 0.0,
        GridBagConstraints.EAST,
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
      panel.add(myTestCaseNameTextField, c);
      myLabel.setLabelFor(myTestCaseNameTextField);
    }

    SwingUtils.addGreedyBottomRow(panel);

    return panel;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    myLabel.setAnchor(anchor);
    myJsFileRunSettingsSection.setAnchor(anchor);
  }

}
