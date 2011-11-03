package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.ObjectUtils;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

class ConfigFileRunSettingsSection extends AbstractRunSettingsSection {

  private TextFieldWithBrowseButton myConfigFileTextFieldWithBrowseButton;
  private final JBLabel myLabel = new JBLabel("Configuration file:");

  ConfigFileRunSettingsSection() {
    setAnchor(myLabel);
  }

  @Override
  public void resetFrom(JstdRunSettings runSettings) {
    myConfigFileTextFieldWithBrowseButton.setText(runSettings.getConfigFile());
  }

  @Override
  public void applyTo(JstdRunSettings.Builder runSettingsBuilder) {
    runSettingsBuilder.setConfigFile(ObjectUtils.notNull(myConfigFileTextFieldWithBrowseButton.getText(), ""));
  }

  @NotNull
  @Override
  public JComponent createComponent(@NotNull CreationContext creationContext) {
    JPanel panel = new JPanel(new GridBagLayout());
    {
      GridBagConstraints c = new GridBagConstraints(
          0, 0,
          1, 1,
          0.0, 0.0,
          GridBagConstraints.LINE_START,
          GridBagConstraints.NONE,
          new Insets(4, 0, UISettingsUtil.TEXT_LABEL_BOTTOM_SPACING, 0),
          0, 0
      );
      myLabel.setDisplayedMnemonic('C');
      panel.add(myLabel, c);
    }
    {
      GridBagConstraints c = new GridBagConstraints(
          1, 0,
          1, 1,
          1.0, 1.0,
          GridBagConstraints.FIRST_LINE_START,
          GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0),
          0, 0
      );
      myConfigFileTextFieldWithBrowseButton = new TextFieldWithBrowseButton();
      myConfigFileTextFieldWithBrowseButton.addBrowseFolderListener(
          "Select JsTestDriver configuration file",
          "",
          creationContext.getProject(),
          FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
      );
      panel.add(myConfigFileTextFieldWithBrowseButton, c);
    }
    myLabel.setLabelFor(myConfigFileTextFieldWithBrowseButton);
    return panel;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    myLabel.setAnchor(anchor);
  }
}
