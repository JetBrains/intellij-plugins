package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.config.JstdConfigFileUtils;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.SwingUtils;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ConfigFileRunSettingsSection extends AbstractRunSettingsSection {

  private final TextFieldWithBrowseButton myConfigFileTextFieldWithBrowseButton;
  private final JBLabel myLabel = new JBLabel("Configuration file:");

  ConfigFileRunSettingsSection() {
    myConfigFileTextFieldWithBrowseButton = new TextFieldWithBrowseButton();
    setAnchor(myLabel);
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    myConfigFileTextFieldWithBrowseButton.setText(runSettings.getConfigFile());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    runSettingsBuilder.setConfigFile(ObjectUtils.notNull(myConfigFileTextFieldWithBrowseButton.getText(), ""));
  }

  @NotNull
  @Override
  protected JComponent createComponent(@NotNull CreationContext creationContext) {
    JPanel panel = new JPanel(new GridBagLayout());
    {
      GridBagConstraints c = new GridBagConstraints(
          0, 0,
          1, 1,
          0.0, 0.0,
          GridBagConstraints.EAST,
          GridBagConstraints.NONE,
          new Insets(UIUtil.DEFAULT_VGAP, 0, 0, UIUtil.DEFAULT_HGAP),
          0, 0
      );
      myLabel.setDisplayedMnemonic('C');
      myLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      myLabel.setLabelFor(myConfigFileTextFieldWithBrowseButton.getTextField());
      panel.add(myLabel, c);
    }
    {
      GridBagConstraints c = new GridBagConstraints(
          1, 0,
          1, 1,
          1.0, 0.0,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(UIUtil.DEFAULT_VGAP, 0, 0, 0),
          0, 0
      );
      myConfigFileTextFieldWithBrowseButton.addBrowseFolderListener(
          "Select JsTestDriver configuration file",
          null,
          creationContext.getProject(),
          new FileChooserDescriptor(true, false, false, false, false, false) {
            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
              boolean visible = super.isFileVisible(file, showHiddenFiles);
              if (visible && !file.isDirectory() && !showHiddenFiles) {
                visible = JstdConfigFileUtils.isJstdConfigFile(file);
              }
              return visible;
            }
          }
      );
      panel.add(myConfigFileTextFieldWithBrowseButton, c);
    }
    SwingUtils.addGreedyBottomRow(panel);
    return panel;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    myLabel.setAnchor(anchor);
  }

}
