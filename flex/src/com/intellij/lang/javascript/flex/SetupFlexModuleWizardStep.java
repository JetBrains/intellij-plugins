package com.intellij.lang.javascript.flex;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.sdk.AirMobileSdkType;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

class SetupFlexModuleWizardStep extends ModuleWizardStep {
  private final FlexModuleBuilder myModuleBuilder;

  private JPanel myMainPanel;
  private FlexSdkComboBoxWithBrowseButton myFlexSdkComboWithBrowse;
  private FlashPlayerVersionForm myTargetPlayerVersionForm;
  private JCheckBox myCreateSampleAppCheckBox;
  private JTextField mySampleAppFileNameTextField;
  private JCheckBox myPureActionScriptSampleCheckBox;
  private JCheckBox myCreateCompilerConfigCheckBox;
  private JTextField myCompilerConfigFileNameTextField;
  private JCheckBox myCreateHtmlWrapperCheckBox;
  private JCheckBox myCreateAirDescriptorCheckBox;
  private JTextField myAirDescriptorFileNameTextField;
  private ServerTechnologyForm myServerTechnologyForm;
  private JCheckBox myCreateRunConfigurationCheckBox;
  private JRadioButton myApplicationOutputTypeRadioButton;
  private JRadioButton myLibraryOutputTypeRadioButton;

  public SetupFlexModuleWizardStep(final FlexModuleBuilder moduleBuilder, final WizardContext wizardContext) {
    myModuleBuilder = moduleBuilder;

    myFlexSdkComboWithBrowse.addComboboxListener(new FlexSdkComboBoxWithBrowseButton.Listener() {
      public void stateChanged() {
        updateControls();
        resetTargetPlayerVersion();
      }
    });

    final ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateControls();
      }
    };

    myApplicationOutputTypeRadioButton.addActionListener(actionListener);
    myLibraryOutputTypeRadioButton.addActionListener(actionListener);
    myCreateSampleAppCheckBox.addActionListener(actionListener);
    myCreateCompilerConfigCheckBox.addActionListener(actionListener);
    myCreateHtmlWrapperCheckBox.addActionListener(actionListener);
    myCreateAirDescriptorCheckBox.addActionListener(actionListener);

    myPureActionScriptSampleCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateSampleAppFileExtension();
      }
    });

    resetTargetPlayerVersion();
  }

  private void updateSampleAppFileExtension() {
    String sampleFileName = mySampleAppFileNameTextField.getText().trim();
    if (sampleFileName.endsWith(".as")) {
      sampleFileName = sampleFileName.substring(0, sampleFileName.length() - ".as".length());
    }
    else if (sampleFileName.endsWith(".mxml")) {
      sampleFileName = sampleFileName.substring(0, sampleFileName.length() - ".mxml".length());
    }
    sampleFileName += myPureActionScriptSampleCheckBox.isSelected() ? ".as" : ".mxml";
    mySampleAppFileNameTextField.setText(sampleFileName);
  }

  private void resetTargetPlayerVersion() {
    final Sdk sdk = myFlexSdkComboWithBrowse.getSelectedSdk();
    myTargetPlayerVersionForm.setPlayerVersion(TargetPlayerUtils.getTargetPlayerVersion(sdk));
  }

  private void updateControls() {
    final Sdk sdk = myFlexSdkComboWithBrowse.getSelectedSdk();
    final boolean srcRootExists = myModuleBuilder.getSourcePaths() != null && !myModuleBuilder.getSourcePaths().isEmpty();

    updateTargetPlayerControls(sdk);
    updateCreateSampleAppControls(sdk, srcRootExists);
    updateCreateCompilerConfigControls(sdk, srcRootExists);
    updateCreateHtmlWrapperControls(sdk, srcRootExists);
    updateCreateAirDescriptorControls(sdk, srcRootExists);
  }

  private void updateTargetPlayerControls(final Sdk sdk) {
    final boolean applicable = sdk != null && TargetPlayerUtils.isTargetPlayerApplicable(sdk);
    UIUtil.setEnabled(myTargetPlayerVersionForm.getMainPanel(), applicable, true);
  }

  private void updateCreateSampleAppControls(final Sdk sdk, final boolean srcRootExists) {
    myCreateSampleAppCheckBox.setEnabled(myApplicationOutputTypeRadioButton.isSelected() && sdk != null && srcRootExists);

    if (sdk != null) {
      if (sdk.getSdkType() instanceof AirMobileSdkType) {
        myCreateSampleAppCheckBox.setText(FlexBundle.message("create.sample.mobile.air.application"));
      }
      else if (sdk.getSdkType() instanceof AirSdkType) {
        myCreateSampleAppCheckBox.setText(FlexBundle.message("create.sample.air.application"));
      }
      else {
        myCreateSampleAppCheckBox.setText(FlexBundle.message("create.sample.flex.application"));
      }
    }

    mySampleAppFileNameTextField.setEnabled(myCreateSampleAppCheckBox.isEnabled() && myCreateSampleAppCheckBox.isSelected());

    myPureActionScriptSampleCheckBox.setEnabled(mySampleAppFileNameTextField.isEnabled());

    myCreateRunConfigurationCheckBox.setEnabled(mySampleAppFileNameTextField.isEnabled());
  }

  private void updateCreateCompilerConfigControls(final Sdk sdk, final boolean srcRootExists) {
    myCreateCompilerConfigCheckBox.setEnabled(myApplicationOutputTypeRadioButton.isSelected() && srcRootExists);

    myCompilerConfigFileNameTextField.setEnabled(myCreateCompilerConfigCheckBox.isEnabled() && myCreateCompilerConfigCheckBox.isSelected());
  }

  private void updateCreateHtmlWrapperControls(final Sdk sdk, final boolean srcRootExists) {
    myCreateHtmlWrapperCheckBox.setEnabled(
      myApplicationOutputTypeRadioButton.isSelected() && sdk != null && sdk.getSdkType() instanceof FlexSdkType && srcRootExists);
  }

  private void updateCreateAirDescriptorControls(final Sdk sdk, final boolean srcRootExists) {
    myCreateAirDescriptorCheckBox.setEnabled(myApplicationOutputTypeRadioButton.isSelected() &&
                                             srcRootExists &&
                                             sdk != null &&
                                             (sdk.getSdkType() instanceof AirSdkType || sdk.getSdkType() instanceof AirMobileSdkType));
    myAirDescriptorFileNameTextField.setEnabled(myCreateAirDescriptorCheckBox.isEnabled() && myCreateAirDescriptorCheckBox.isSelected());
  }

  public void updateStep() {
    if (StringUtil.isEmptyOrSpaces(mySampleAppFileNameTextField.getText())) {
      String className = myModuleBuilder.getName().replaceAll("[^\\p{Alnum}]", "_");
      if (className.length() > 0 && Character.isLowerCase(className.charAt(0))) {
        className = Character.toUpperCase(className.charAt(0)) + className.substring(1);
      }
      final String extension = myPureActionScriptSampleCheckBox.isSelected() ? ".as" : ".mxml";
      mySampleAppFileNameTextField.setText(className + extension);
    }
    updateControls();
  }

  public JComponent getComponent() {
    return myMainPanel;
  }

  public boolean validate() throws ConfigurationException {
    final Sdk flexSdk = myFlexSdkComboWithBrowse.getSelectedSdk();
    if (flexSdk != null &&
        TargetPlayerUtils.isTargetPlayerApplicable(flexSdk) &&
        !TargetPlayerUtils.isTargetPlayerValid(myTargetPlayerVersionForm.getPlayerVersion())) {
      throw new ConfigurationException(FlexBundle.message("invalid.target.player.version"));
    }

    if (myCreateSampleAppCheckBox.isSelected()) {
      final String correctExtension = myPureActionScriptSampleCheckBox.isSelected() ? ".as" : ".mxml";
      if (!mySampleAppFileNameTextField.getText().trim().endsWith(correctExtension)) {
        throw new ConfigurationException(MessageFormat.format("Sample application file must have {0} extension", correctExtension));
      }
      if (!JSUtils.isValidClassName(StringUtil.trimEnd(mySampleAppFileNameTextField.getText().trim(), correctExtension), false)) {
        throw new ConfigurationException("Sample application file name is invalid");
      }
    }

    if (myCreateCompilerConfigCheckBox.isSelected()) {
      if (!myCompilerConfigFileNameTextField.getText().trim().endsWith(".xml")) {
        throw new ConfigurationException("Compiler configuration file must have .xml extension");
      }
    }

    if (myCreateAirDescriptorCheckBox.isSelected()) {
      if (!myAirDescriptorFileNameTextField.getText().trim().endsWith(".xml")) {
        throw new ConfigurationException("AIR application descriptor file must have .xml extension");
      }
    }

    return true;
  }

  public void updateDataModel() {
    myModuleBuilder.setSdk(myFlexSdkComboWithBrowse.getSelectedSdk());
    myModuleBuilder.setTargetPlayerVersion(myTargetPlayerVersionForm.getPlayerVersion());
    myModuleBuilder.setCreateSampleApp(myCreateSampleAppCheckBox.isEnabled() && myCreateSampleAppCheckBox.isSelected());
    myModuleBuilder.setSampleAppFileName(mySampleAppFileNameTextField.getText().trim());
    myModuleBuilder
      .setCreateCustomCompilerConfig(myCreateCompilerConfigCheckBox.isEnabled() && myCreateCompilerConfigCheckBox.isSelected());
    myModuleBuilder.setCustomCompilerConfigFileName(myCompilerConfigFileNameTextField.getText().trim());
    myModuleBuilder.setCreateHtmlWrapper(myCreateHtmlWrapperCheckBox.isSelected() && myCreateHtmlWrapperCheckBox.isEnabled());
    myModuleBuilder.setCreateAirDescriptor(myCreateAirDescriptorCheckBox.isSelected() && myCreateAirDescriptorCheckBox.isEnabled());
    myModuleBuilder.setAirDescriptorFileName(myAirDescriptorFileNameTextField.getText().trim());
    myModuleBuilder
      .setCreateRunConfiguration(myCreateRunConfigurationCheckBox.isSelected() && myCreateRunConfigurationCheckBox.isEnabled());
    myModuleBuilder.setPathToServicesConfigXml(myServerTechnologyForm.getPathToServicesConfigXml());
    myModuleBuilder.setContextRoot(myServerTechnologyForm.getContextRoot());
    myModuleBuilder.setFlexOutputType(
      myApplicationOutputTypeRadioButton.isSelected() ? FlexBuildConfiguration.APPLICATION : FlexBuildConfiguration.LIBRARY);
  }

  public String getHelpId() {
    return FlexModuleType.CREATE_MODULE_HELP_ID;
  }
}
