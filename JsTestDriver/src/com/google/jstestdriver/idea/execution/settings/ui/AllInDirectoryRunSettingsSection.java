package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.ObjectUtils;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

class AllInDirectoryRunSettingsSection extends AbstractRunSettingsSection {

  private TextFieldWithBrowseButton myDirectoryTextFieldWithBrowseButton;
  private JBLabel myLabel;

  AllInDirectoryRunSettingsSection() {
    myLabel = new JBLabel("Directory: ");
    setAnchor(myLabel);
  }

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
    {
      GridBagConstraints c = new GridBagConstraints(
          0, 0,
          1, 1,
          0.0, 0.0,
          GridBagConstraints.LINE_START,
          GridBagConstraints.NONE,
          new Insets(UIUtil.DEFAULT_VGAP, 0, 0, 0),
          0, 0
      );
      myLabel.setDisplayedMnemonic('D');
      panel.add(myLabel, c);
    }
    {
      GridBagConstraints c = new GridBagConstraints(
          1, 0,
          1, 1,
          1.0, 1.0,
          GridBagConstraints.FIRST_LINE_START,
          GridBagConstraints.HORIZONTAL,
          new Insets(UIUtil.DEFAULT_VGAP, 0, 0, 0),
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
    myLabel.setLabelFor(myDirectoryTextFieldWithBrowseButton);
    return panel;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    myLabel.setAnchor(anchor);
  }
}
