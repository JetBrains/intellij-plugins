package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.FilesToPackageForm;
import com.intellij.lang.javascript.flex.actions.SigningOptionsForm;
import com.intellij.lang.javascript.flex.projectStructure.options.AirDesktopPackagingOptions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AirDesktopPackagingConfigurable extends NamedConfigurable<AirDesktopPackagingOptions> {

  private JPanel myMainPanel;

  private JRadioButton myGeneratedDescriptorRadioButton;
  private JRadioButton myCustomDescriptorRadioButton;
  private TextFieldWithBrowseButton myCustomDescriptorTextWithBrowse;

  private JTextField myInstallerFileNameTextField;

  private FilesToPackageForm myFilesToPackageForm;
  private JCheckBox myDoNotSignCheckBox;
  private SigningOptionsForm mySigningOptionsForm;

  private final Module myModule;
  private final AirDesktopPackagingOptions myAirDesktopPackagingOptions;

  public AirDesktopPackagingConfigurable(final Module module, final AirDesktopPackagingOptions airDesktopPackagingOptions) {
    myModule = module;
    myAirDesktopPackagingOptions = airDesktopPackagingOptions;

    final ActionListener listener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        myCustomDescriptorTextWithBrowse.setEnabled(myCustomDescriptorRadioButton.isSelected());
        if (myCustomDescriptorRadioButton.isSelected()) {
          IdeFocusManager.getInstance(module.getProject()).requestFocus(myCustomDescriptorTextWithBrowse.getTextField(), true);
        }
      }
    };

    myGeneratedDescriptorRadioButton.addActionListener(listener);
    myCustomDescriptorRadioButton.addActionListener(listener);

    myCustomDescriptorTextWithBrowse.addBrowseFolderListener(null, null, module.getProject(), FlexUtils.createFileChooserDescriptor("xml"));
  }

  @Nls
  public String getDisplayName() {
    return "AIR Package";
  }

  public void setDisplayName(final String name) {
  }

  public String getBannerSlogan() {
    return "AIR Package";
  }

  public Icon getIcon() {
    return null;
  }

  public AirDesktopPackagingOptions getEditableObject() {
    return myAirDesktopPackagingOptions;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  public boolean isModified() {
    if (myAirDesktopPackagingOptions.USE_GENERATED_DESCRIPTOR != myGeneratedDescriptorRadioButton.isSelected()) return true;
    if (!myAirDesktopPackagingOptions.CUSTOM_DESCRIPTOR_PATH
      .equals(FileUtil.toSystemIndependentName(myCustomDescriptorTextWithBrowse.getText().trim()))) {
      return true;
    }
    if (!myAirDesktopPackagingOptions.INSTALLER_FILE_NAME.equals(myInstallerFileNameTextField.getText().trim())) return true;

    return false;
  }

  public void apply() throws ConfigurationException {
    applyTo(myAirDesktopPackagingOptions);
  }

  public void applyTo(final AirDesktopPackagingOptions airDesktopPackagingOptions) {
    airDesktopPackagingOptions.USE_GENERATED_DESCRIPTOR = myGeneratedDescriptorRadioButton.isSelected();
    airDesktopPackagingOptions.CUSTOM_DESCRIPTOR_PATH = FileUtil.toSystemIndependentName(myCustomDescriptorTextWithBrowse.getText().trim());
    airDesktopPackagingOptions.INSTALLER_FILE_NAME = myInstallerFileNameTextField.getText().trim();
  }

  public void reset() {
    myGeneratedDescriptorRadioButton.setSelected(myAirDesktopPackagingOptions.USE_GENERATED_DESCRIPTOR);
    myCustomDescriptorRadioButton.setSelected(!myAirDesktopPackagingOptions.USE_GENERATED_DESCRIPTOR);
    myCustomDescriptorTextWithBrowse.setText(FileUtil.toSystemDependentName(myAirDesktopPackagingOptions.CUSTOM_DESCRIPTOR_PATH));
    myInstallerFileNameTextField.setText(myAirDesktopPackagingOptions.INSTALLER_FILE_NAME);
  }

  public void disposeUIResources() {
  }

  private void createUIComponents() {
    myFilesToPackageForm = new FilesToPackageForm(myModule.getProject());

    mySigningOptionsForm = new SigningOptionsForm(myModule.getProject(), new Computable.PredefinedValueComputable<Module>(myModule),
                                                  new Computable.PredefinedValueComputable<Sdk>(null), EmptyRunnable.INSTANCE);
    mySigningOptionsForm.setProvisioningProfileApplicable(false);
    mySigningOptionsForm.setCreateCertificateButtonApplicable(false);
  }
}
