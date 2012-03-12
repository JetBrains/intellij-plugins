package com.google.jstestdriver.idea.execution.settings.ui;

import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.util.SwingUtils;
import com.google.jstestdriver.idea.util.TextChangeListener;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

class JsFileRunSettingsSection extends AbstractRunSettingsSection {

  private final TextFieldWithBrowseButton myJsFileTextFieldWithBrowseButton;
  private final OptionalConfigRunSettingsSection myOptionalConfigRunSettingsSection;
  private final JBLabel myLabel;

  JsFileRunSettingsSection() {
    myJsFileTextFieldWithBrowseButton = new TextFieldWithBrowseButton();
    myOptionalConfigRunSettingsSection = new OptionalConfigRunSettingsSection(this);

    myLabel = new JBLabel("JS test file:");
    setAnchor(myLabel);
  }

  @Override
  public void resetFrom(@NotNull JstdRunSettings runSettings) {
    myOptionalConfigRunSettingsSection.resetFrom(runSettings);
    myJsFileTextFieldWithBrowseButton.setText(runSettings.getJsFilePath());
  }

  @Override
  public void applyTo(@NotNull JstdRunSettings.Builder runSettingsBuilder) {
    myOptionalConfigRunSettingsSection.applyTo(runSettingsBuilder);
    runSettingsBuilder.setJSFilePath(ObjectUtils.notNull(myJsFileTextFieldWithBrowseButton.getText(), ""));
  }

  public void addJsFileTextFieldListener(final TextChangeListener textChangeListener) {
    SwingUtils.addTextChangeListener(myJsFileTextFieldWithBrowseButton.getChildComponent(), textChangeListener);
  }

  @NotNull
  @Override
  public JComponent createComponent(@NotNull CreationContext creationContext) {
    JPanel panel = new JPanel(new GridBagLayout());

    myLabel.setDisplayedMnemonic('J');
    {
      GridBagConstraints c = new GridBagConstraints(
          0, 0,
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
          1, 0,
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

    int nextRow = 1;
    {
      GridBagConstraints c = new GridBagConstraints(
          0, nextRow,
          2, 1,
          1.0, 0.0,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0),
          0, 0
      );
      JComponent additionalComponent1 = getAdditionalComponent();
      if (additionalComponent1 != null) {
        panel.add(additionalComponent1, c);
        nextRow++;
      }
    }

    {
      GridBagConstraints c = new GridBagConstraints(
          0, nextRow,
          2, 1,
          1.0, 0.0,
          GridBagConstraints.WEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0),
          0, 0
      );
      panel.add(myOptionalConfigRunSettingsSection.getComponent(creationContext), c);
      nextRow++;
    }

    {
      GridBagConstraints c = new GridBagConstraints(
          0, nextRow,
          2, 1,
          1.0, 1.0,
          GridBagConstraints.WEST,
          GridBagConstraints.BOTH,
          new Insets(0, 0, 0, 0),
          0, 0
      );
      panel.add(myOptionalConfigRunSettingsSection.getComponent(creationContext), c);
    }

    return panel;
  }

  protected JComponent getAdditionalComponent() {
    return null;
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    myLabel.setAnchor(anchor);
  }
}
