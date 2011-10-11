package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

class TestMethodRunSettingsSection extends AbstractRunSettingsSection {

  private TestCaseRunSettingsSection myTestCaseRunSettingsSection;
  private JTextField myTestNameTextField;

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    myTestCaseRunSettingsSection = new TestCaseRunSettingsSection() {
      @Override
      protected JComponent getTestCaseAdditionalComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel("Method:");
        label.setDisplayedMnemonic('M');
        {
          GridBagConstraints c = new GridBagConstraints(
              0, 0,
              1, 1,
              1.0, 0.0,
              GridBagConstraints.LINE_START,
              GridBagConstraints.NONE,
              new Insets(UISettingsUtil.TEXT_LABEL_TOP_SPACING, 0, UISettingsUtil.TEXT_LABEL_BOTTOM_SPACING, 0),
              0, 0
          );
          panel.add(label, c);
        }
        {
          GridBagConstraints c = new GridBagConstraints(
              0, 1,
              1, 1,
              1.0, 1.0,
              GridBagConstraints.FIRST_LINE_START,
              GridBagConstraints.HORIZONTAL,
              new Insets(0, 0, 0, 0),
              0, 0
          );
          myTestNameTextField = new JTextField();
          panel.add(myTestNameTextField, c);
          label.setLabelFor(myTestNameTextField);
        }
        return panel;
      }
    };
    return myTestCaseRunSettingsSection.getComponent(creationContext);
  }

  @Override
  public void resetFrom(JstdRunSettings runSettings) {
    myTestCaseRunSettingsSection.resetFrom(runSettings);
    myTestNameTextField.setText(runSettings.getTestMethodName());
  }

  @Override
  public void applyTo(JstdRunSettings.Builder runSettingsBuilder) {
    myTestCaseRunSettingsSection.applyTo(runSettingsBuilder);
    runSettingsBuilder.setTestMethodName(ObjectUtils.notNull(myTestNameTextField.getText(), ""));
  }
}
