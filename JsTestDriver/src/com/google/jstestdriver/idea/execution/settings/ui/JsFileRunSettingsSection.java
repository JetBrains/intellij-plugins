package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.SwingUtils;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileTypeDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class JsFileRunSettingsSection extends AbstractRunSettingsSection {

  private final ConfigFileRunSettingsSection myConfigFileRunSettingsSection;
  private final TextFieldWithBrowseButton myJsTestFileTextFieldWithBrowseButton;
  private final JBLabel myLabel;

  JsFileRunSettingsSection() {
    myConfigFileRunSettingsSection = new ConfigFileRunSettingsSection();
    myJsTestFileTextFieldWithBrowseButton = new TextFieldWithBrowseButton();
    myLabel = new JBLabel("JavaScript test file:");
    setAnchor(SwingUtils.getWiderComponent(myLabel, myConfigFileRunSettingsSection));
  }

  @NotNull
  public JTextField getJsTestFileTextField() {
    return myJsTestFileTextFieldWithBrowseButton.getTextField();
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    myConfigFileRunSettingsSection.resetFrom(runSettings);
    myJsTestFileTextFieldWithBrowseButton.setText(runSettings.getJsFilePath());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    myConfigFileRunSettingsSection.applyTo(runSettingsBuilder);
    runSettingsBuilder.setJSFilePath(ObjectUtils.notNull(myJsTestFileTextFieldWithBrowseButton.getText(), ""));
  }

  @NotNull
  @Override
  public JComponent createComponent(@NotNull CreationContext creationContext) {
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
      JComponent configComponent = myConfigFileRunSettingsSection.getComponent(creationContext);
      panel.add(configComponent, c);
    }

    {
      myLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      myLabel.setDisplayedMnemonic('J');
      myLabel.setLabelFor(myJsTestFileTextFieldWithBrowseButton.getTextField());
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
      FileChooserDescriptor jsFileChooserDescriptor = new FileTypeDescriptor("Select JavaScript test file", ".js");
      myJsTestFileTextFieldWithBrowseButton.addBrowseFolderListener(
        null,
        null,
        creationContext.getProject(),
        jsFileChooserDescriptor
      );
      panel.add(myJsTestFileTextFieldWithBrowseButton, c);
      myLabel.setLabelFor(myJsTestFileTextFieldWithBrowseButton);
    }

    SwingUtils.addGreedyBottomRow(panel);

    return panel;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    myLabel.setAnchor(anchor);
    myConfigFileRunSettingsSection.setAnchor(anchor);
  }

}
