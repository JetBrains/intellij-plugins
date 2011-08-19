package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.options.AirDescriptorOptions;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

public class AirDescriptorConfigurable extends NamedConfigurable<AirDescriptorOptions> {

  private JPanel myMainPanel;
  private JTextField myDescriptorFileNameTextField;
  private TextFieldWithBrowseButton myDescriptorFileLocationTextWithBrowse;
  private JComboBox myAirVersionComboBox;
  private JTextField myApplicationIdTextField;
  private JTextField myApplicationFileNameTextField;
  private JTextField myApplicationNameTextField;
  private JTextField myApplicationVersionTextField;
  private JTextField myApplicationTitleTextField;
  private JTextField myApplicationWidthTextField;
  private JTextField myApplicationHeightTextField;
  private JRadioButton myUseExistingDescriptorRadioButton;
  private JRadioButton myUseGeneratedDescriptorRadioButton;

  private final AirDescriptorOptions myAirDescriptorOptions;

  public AirDescriptorConfigurable(final AirDescriptorOptions airDescriptorOptions) {
    myAirDescriptorOptions = airDescriptorOptions;
  }

  @Nls
  public String getDisplayName() {
    return "AIR Descriptor";
  }

  public void setDisplayName(final String name) {
  }

  public String getBannerSlogan() {
    return "AIR Descriptor";
  }

  public Icon getIcon() {
    return null;
  }

  public AirDescriptorOptions getEditableObject() {
    return myAirDescriptorOptions;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  public boolean isModified() {
    return false;
  }

  public void apply() throws ConfigurationException {
    applyTo(myAirDescriptorOptions);
  }

  public void applyTo(final AirDescriptorOptions airDescriptorOptions) {
  }

  public void reset() {
  }

  public void disposeUIResources() {
  }
}
