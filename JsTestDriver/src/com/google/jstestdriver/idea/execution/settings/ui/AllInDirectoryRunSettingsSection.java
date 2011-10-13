package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.ObjectUtils;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

class AllInDirectoryRunSettingsSection extends AbstractRunSettingsSection {

  private TextFieldWithBrowseButton myDirectoryTextFieldWithBrowseButton;

  @Override
  public void resetFrom(JstdRunSettings runSettings) {
    myDirectoryTextFieldWithBrowseButton.setText(runSettings.getDirectory());
  }

  @Override
  public void applyTo(JstdRunSettings.Builder runSettingsBuilder) {
    runSettingsBuilder.setDirectory(ObjectUtils.notNull(myDirectoryTextFieldWithBrowseButton.getText(), ""));
  }

  @NotNull
  @Override
  public JComponent createComponent(@NotNull CreationContext creationContext) {
    JPanel panel = new JPanel(new GridBagLayout());
    JLabel label = new JLabel("Directory:");
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
      label.setDisplayedMnemonic('D');
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
      myDirectoryTextFieldWithBrowseButton = new TextFieldWithBrowseButton();
      myDirectoryTextFieldWithBrowseButton.addBrowseFolderListener(
          "Select directory",
          "All JsTestDriver configuration files in this folder will be executed",
          creationContext.getProject(),
          FileChooserDescriptorFactory.createSingleFolderDescriptor()
      );
      panel.add(myDirectoryTextFieldWithBrowseButton, c);
    }
    label.setLabelFor(myDirectoryTextFieldWithBrowseButton);
    return panel;
  }
}
