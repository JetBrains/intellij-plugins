package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.SwingUtils;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
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
  private final TextFieldWithBrowseButton myJsFileTextFieldWithBrowseButton;
  private final JBLabel myLabel;

  JsFileRunSettingsSection() {
    myConfigFileRunSettingsSection = new ConfigFileRunSettingsSection();
    myJsFileTextFieldWithBrowseButton = new TextFieldWithBrowseButton();
    myLabel = new JBLabel("JavaScript test file:");
    setAnchor(SwingUtils.getWiderComponent(myLabel, myConfigFileRunSettingsSection));
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    myConfigFileRunSettingsSection.resetFrom(runSettings);
    myJsFileTextFieldWithBrowseButton.setText(runSettings.getJsFilePath());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    myConfigFileRunSettingsSection.applyTo(runSettingsBuilder);
    runSettingsBuilder.setJSFilePath(ObjectUtils.notNull(myJsFileTextFieldWithBrowseButton.getText(), ""));
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
      myLabel.setLabelFor(myJsFileTextFieldWithBrowseButton.getTextField());
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
      myJsFileTextFieldWithBrowseButton.addBrowseFolderListener(
          "Select JavaScript test file",
          "",
          creationContext.getProject(),
          FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
      );
      panel.add(myJsFileTextFieldWithBrowseButton, c);
      myLabel.setLabelFor(myJsFileTextFieldWithBrowseButton);
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
