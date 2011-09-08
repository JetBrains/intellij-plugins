package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.FilesToPackageForm;
import com.intellij.lang.javascript.flex.actions.SigningOptionsForm;
import com.intellij.lang.javascript.flex.projectStructure.model.AirDesktopPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableAirDesktopPackagingOptions;
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
  private final ModifiableAirDesktopPackagingOptions myModel;

  public AirDesktopPackagingConfigurable(final Module module, final ModifiableAirDesktopPackagingOptions model) {
    myModule = module;
    myModel = model;

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
    return myModel;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  public boolean isModified() {
    if (myModel.isUseGeneratedDescriptor() != myGeneratedDescriptorRadioButton.isSelected()) return true;
    if (!myModel.getCustomDescriptorPath().equals(FileUtil.toSystemIndependentName(myCustomDescriptorTextWithBrowse.getText().trim()))) {
      return true;
    }
    if (!myModel.getInstallerFileName().equals(myInstallerFileNameTextField.getText().trim())) return true;

    return false;
  }

  public void apply() throws ConfigurationException {
    applyTo(myModel);
  }

  public void applyTo(final ModifiableAirDesktopPackagingOptions model) {
    model.setUseGeneratedDescriptor(myGeneratedDescriptorRadioButton.isSelected());
    model.setCustomDescriptorPath(FileUtil.toSystemIndependentName(myCustomDescriptorTextWithBrowse.getText().trim()));
    model.setInstallerFileName(myInstallerFileNameTextField.getText().trim());
  }

  public void reset() {
    myGeneratedDescriptorRadioButton.setSelected(myModel.isUseGeneratedDescriptor());
    myCustomDescriptorRadioButton.setSelected(!myModel.isUseGeneratedDescriptor());
    myCustomDescriptorTextWithBrowse.setText(FileUtil.toSystemDependentName(myModel.getCustomDescriptorPath()));
    myInstallerFileNameTextField.setText(myModel.getInstallerFileName());
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
