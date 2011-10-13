package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

class TestCaseRunSettingsSection extends AbstractRunSettingsSection {

  private JsFileRunSettingsSection myJsFileRunSettingsSection;
  private JTextField myTestCaseNameTextField;

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    myJsFileRunSettingsSection = new JsFileRunSettingsSection() {
      @Override
      protected JComponent getAdditionalComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel("Case:");
        label.setDisplayedMnemonic('e');
        {
          GridBagConstraints c = new GridBagConstraints(
              0, 0,
              1, 1,
              0.0, 0.0,
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
          myTestCaseNameTextField = new JTextField();
          panel.add(myTestCaseNameTextField, c);
          label.setLabelFor(myTestCaseNameTextField);
        }
        {
          GridBagConstraints c = new GridBagConstraints(
              0, 2,
              1, 1,
              1.0, 1.0,
              GridBagConstraints.FIRST_LINE_START,
              GridBagConstraints.HORIZONTAL,
              new Insets(0, 0, 0, 0),
              0, 0
          );
          JComponent additionalComponent = getTestCaseAdditionalComponent();
          if (additionalComponent != null) {
            panel.add(additionalComponent, c);
          }
        }
        return panel;
      }
    };
    return myJsFileRunSettingsSection.createComponent(creationContext);
  }

  protected JComponent getTestCaseAdditionalComponent() {
    return null;
  }

  @Override
  public void resetFrom(JstdRunSettings runSettings) {
    myJsFileRunSettingsSection.resetFrom(runSettings);
    myTestCaseNameTextField.setText(runSettings.getTestCaseName());
  }

  @Override
  public void applyTo(JstdRunSettings.Builder runSettingsBuilder) {
    myJsFileRunSettingsSection.applyTo(runSettingsBuilder);
    runSettingsBuilder.setTestCaseName(ObjectUtils.notNull(myTestCaseNameTextField.getText(), ""));
  }
}
