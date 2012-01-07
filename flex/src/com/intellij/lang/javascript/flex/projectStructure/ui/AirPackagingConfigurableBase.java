package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.FilesToPackageForm;
import com.intellij.lang.javascript.flex.actions.SigningOptionsForm;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.util.Consumer;
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

  private final Computable<String> myMainClassComputable;
  private final Computable<String> myAirVersionComputable;
  private final Computable<Boolean> myAndroidEnabledComputable;
  private final Computable<Boolean> myIOSEnabledComputable;
  private final Consumer<String> myCreatedDescriptorConsumer;

  public AirPackagingConfigurableBase(final Module module,
                                      final T model,
                                      final Computable<String> mainClassComputable,
                                      final Computable<String> airVersionComputable,
                                      final Computable<Boolean> androidEnabledComputable,
                                      final Computable<Boolean> iosEnabledComputable,
                                      final Consumer<String> createdDescriptorConsumer) {
    myModule = module;
    myModel = model;
    myMainClassComputable = mainClassComputable;
    myAirVersionComputable = airVersionComputable;
    myAndroidEnabledComputable = androidEnabledComputable;
    myIOSEnabledComputable = iosEnabledComputable;
    myCreatedDescriptorConsumer = createdDescriptorConsumer;

    isAndroid = model instanceof ModifiableAndroidPackagingOptions;
    isIOS = model instanceof ModifiableIosPackagingOptions;

    myEnabledCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    });
  }

  private void updateControls() {
    final boolean enabled = isPackagingEnabled();
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
    final Runnable descriptorCreator = new Runnable() {
      public void run() {
        final String folderPath = FlexUtils.getContentOrModuleFolderPath(myModule);
        final String mainClass = myMainClassComputable.compute();
        final String airVersion = myAirVersionComputable.compute();
        final boolean androidEnabled = myAndroidEnabledComputable.compute();
        final boolean iosEnabled = myIOSEnabledComputable.compute();

        final CreateAirDescriptorTemplateDialog dialog =
          new CreateAirDescriptorTemplateDialog(myModule.getProject(), folderPath, mainClass, airVersion, androidEnabled, iosEnabled);

        dialog.show();

        if (dialog.isOK()) {
          final String descriptorPath = dialog.getDescriptorPath();
          setUseCustomDescriptor(descriptorPath);

          if (androidEnabled && iosEnabled && dialog.isBothAndroidAndIosSelected()) {
            final int choice =
              Messages.showYesNoDialog(myModule.getProject(), FlexBundle.message("use.same.descriptor.for.android.and.ios"),
                                       CreateAirDescriptorTemplateDialog.TITLE, Messages.getQuestionIcon());
            if (choice == Messages.YES) {
              myCreatedDescriptorConsumer.consume(descriptorPath);
            }
          }
        }
      }
    };

    myAirDescriptorForm = new AirDescriptorForm(myModule.getProject(), descriptorCreator);
    myFilesToPackageForm = new FilesToPackageForm(myModule.getProject());

    mySigningOptionsForm = new SigningOptionsForm(myModule.getProject(), new Computable.PredefinedValueComputable<Module>(myModule),
                                                  new Computable.PredefinedValueComputable<Sdk>(null), EmptyRunnable.INSTANCE);
    mySigningOptionsForm.setUseTempCertificateCheckBoxVisible(!isIOS);
    mySigningOptionsForm.setProvisioningProfileApplicable(isIOS);
    mySigningOptionsForm.setCreateCertificateButtonApplicable(false);
  }

  public void setUseCustomDescriptor(final String descriptorPath) {
    myAirDescriptorForm.setUseCustomDescriptor(descriptorPath);
  }

  public boolean isPackagingEnabled() {
    return !myEnabledCheckBox.isVisible() || myEnabledCheckBox.isSelected();
  }
}
