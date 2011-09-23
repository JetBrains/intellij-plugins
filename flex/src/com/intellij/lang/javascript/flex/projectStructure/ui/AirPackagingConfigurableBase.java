package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.actions.FilesToPackageForm;
import com.intellij.lang.javascript.flex.actions.SigningOptionsForm;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class AirPackagingConfigurableBase<T extends ModifiableAirPackagingOptions> extends NamedConfigurable<T> {

  private JPanel myMainPanel;

  private AirDescriptorForm myAirDescriptorForm;
  private JTextField myPackageFileNameTextField;
  private FilesToPackageForm myFilesToPackageForm;
  private SigningOptionsForm mySigningOptionsForm;
  private JCheckBox myEnabledCheckBox;

  private final Module myModule;
  private final T myModel;

  private final boolean isAndroid;
  private final boolean isIOS;

  public AirPackagingConfigurableBase(final Module module, final T model) {
    myModule = module;
    myModel = model;

    isAndroid = model instanceof ModifiableAndroidPackagingOptions;
    isIOS = model instanceof ModifiableIosPackagingOptions;

    myEnabledCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    });
  }

  private void updateControls() {
    final boolean enabled = !myEnabledCheckBox.isVisible() || myEnabledCheckBox.isSelected();
    UIUtil.setEnabled(myMainPanel, enabled, true);
    myEnabledCheckBox.setEnabled(true);
    myAirDescriptorForm.updateControls();
    mySigningOptionsForm.setEnabled(enabled);
  }

  public T getEditableObject() {
    return myModel;
  }

  public void setDisplayName(final String name) {
  }

  public String getBannerSlogan() {
    return getDisplayName();
  }

  public Icon getIcon() {
    return null;
  }

  public String getHelpTopic() {
    return null;
  }

  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  public void reset() {
    myEnabledCheckBox.setVisible(isAndroid || isIOS);

    if (isAndroid) myEnabledCheckBox.setSelected(((AndroidPackagingOptions)myModel).isEnabled());
    if (isIOS) myEnabledCheckBox.setSelected(((IosPackagingOptions)myModel).isEnabled());

    myAirDescriptorForm.resetFrom(myModel);
    myPackageFileNameTextField.setText(myModel.getPackageFileName());
    myFilesToPackageForm.resetFrom(myModel.getFilesToPackage());
    mySigningOptionsForm.resetFrom(myModel.getSigningOptions());

    updateControls();
  }

  public boolean isModified() {
    if (isAndroid && myEnabledCheckBox.isSelected() != ((AndroidPackagingOptions)myModel).isEnabled()) return true;
    if (isIOS && myEnabledCheckBox.isSelected() != ((IosPackagingOptions)myModel).isEnabled()) return true;

    if (myAirDescriptorForm.isModified(myModel)) return true;
    if (!myModel.getPackageFileName().equals(myPackageFileNameTextField.getText().trim())) return true;
    if (myFilesToPackageForm.isModified(myModel.getFilesToPackage())) return true;
    if (mySigningOptionsForm.isModified(myModel.getSigningOptions())) return true;

    return false;
  }

  public void apply() throws ConfigurationException {
    applyTo(myModel);
  }

  public void applyTo(final ModifiableAirPackagingOptions model) {
    if (isAndroid) ((ModifiableAndroidPackagingOptions)model).setEnabled(myEnabledCheckBox.isSelected());
    if (isIOS) ((ModifiableIosPackagingOptions)model).setEnabled(myEnabledCheckBox.isSelected());

    myAirDescriptorForm.applyTo(model);
    model.setPackageFileName(myPackageFileNameTextField.getText().trim());
    myFilesToPackageForm.applyTo(model.getFilesToPackage());
    mySigningOptionsForm.applyTo(model.getSigningOptions());
  }

  public void disposeUIResources() {
  }

  private void createUIComponents() {
    myAirDescriptorForm = new AirDescriptorForm(myModule.getProject(), isIOS);
    myFilesToPackageForm = new FilesToPackageForm(myModule.getProject());

    mySigningOptionsForm = new SigningOptionsForm(myModule.getProject(), new Computable.PredefinedValueComputable<Module>(myModule),
                                                  new Computable.PredefinedValueComputable<Sdk>(null), EmptyRunnable.INSTANCE);
    mySigningOptionsForm.setUseTempCertificateCheckBoxVisible(!isIOS);
    mySigningOptionsForm.setProvisioningProfileApplicable(isIOS);
    mySigningOptionsForm.setCreateCertificateButtonApplicable(false);
  }
}
