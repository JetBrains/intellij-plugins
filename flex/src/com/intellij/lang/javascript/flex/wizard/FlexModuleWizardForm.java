package com.intellij.lang.javascript.flex.wizard;

import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleBuilder;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.NonFocusableCheckBox;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FlexModuleWizardForm {

  private JPanel myMainPanel;
  private JComboBox myTargetPlatformCombo;
  private NonFocusableCheckBox myPureActionScriptCheckBox;
  private JComboBox myOutputTypeCombo;

  private JLabel myTargetDevicesLabel;
  private JCheckBox myAndroidCheckBox;
  private JCheckBox myIOSCheckBox;

  private JLabel mySdkLabel;
  private FlexSdkComboBoxWithBrowseButton mySdkCombo;

  private JLabel myTargetPlayerLabel;
  private JComboBox myTargetPlayerCombo;

  private JCheckBox mySampleAppCheckBox;
  private JTextField mySampleAppTextField;

  private JCheckBox myHtmlWrapperCheckBox;
  private JCheckBox myEnableHistoryCheckBox;
  private JCheckBox myCheckPlayerVersionCheckBox;
  private JCheckBox myExpressInstallCheckBox;

  public FlexModuleWizardForm() {
    mySdkLabel.setLabelFor(mySdkCombo.getChildComponent());
    BCUtils.initTargetPlatformCombo(myTargetPlatformCombo);
    BCUtils.initOutputTypeCombo(myOutputTypeCombo);

    final ActionListener listener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    };

    myTargetPlatformCombo.addActionListener(listener);
    myOutputTypeCombo.addActionListener(listener);
    mySampleAppCheckBox.addActionListener(listener);
    myHtmlWrapperCheckBox.addActionListener(listener);
    myCheckPlayerVersionCheckBox.addActionListener(listener);

    mySdkCombo.addComboboxListener(new FlexSdkComboBoxWithBrowseButton.Listener() {
      @Override
      public void stateChanged() {
        BCUtils.updateAvailableTargetPlayers(mySdkCombo.getSelectedSdk(), myTargetPlayerCombo);
        updateControls();
      }
    });

    myPureActionScriptCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final String sampleApp = mySampleAppTextField.getText().trim();
        if (sampleApp.endsWith(".as") || sampleApp.endsWith(".mxml")) {
          mySampleAppTextField
            .setText(FileUtil.getNameWithoutExtension(sampleApp) + (myPureActionScriptCheckBox.isSelected() ? ".as" : ".mxml"));
        }
      }
    });

    reset();
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  public void setEnabled(final boolean enabled) {
    UIUtil.setEnabled(myMainPanel, enabled, true);
    if (enabled) {
      updateControls();
    }
  }

  private void reset() {
    myTargetPlayerCombo.setSelectedItem(TargetPlatform.Web);
    myPureActionScriptCheckBox.setSelected(false);
    myOutputTypeCombo.setSelectedItem(OutputType.Application);
    myAndroidCheckBox.setSelected(true);
    myIOSCheckBox.setSelected(true);
    BCUtils.updateAvailableTargetPlayers(mySdkCombo.getSelectedSdk(), myTargetPlayerCombo);
    mySampleAppCheckBox.setSelected(true);
    mySampleAppTextField.setText("Main.mxml");
    myHtmlWrapperCheckBox.setSelected(true);
    myEnableHistoryCheckBox.setSelected(true);
    myCheckPlayerVersionCheckBox.setSelected(true);
    myExpressInstallCheckBox.setSelected(true);

    updateControls();
  }

  private void updateControls() {
    final Sdk sdk = mySdkCombo.getSelectedSdk();
    final boolean sdkSet = sdk != null;
    final boolean web = myTargetPlatformCombo.getSelectedItem() == TargetPlatform.Web;
    final boolean mobile = myTargetPlatformCombo.getSelectedItem() == TargetPlatform.Mobile;
    final boolean app = myOutputTypeCombo.getSelectedItem() == OutputType.Application;

    final boolean createSampleAppWasEnabled = mySampleAppCheckBox.isEnabled();
    final boolean createHtmlWrapperWasEnabled = myHtmlWrapperCheckBox.isEnabled();
    final boolean targetDeviceWasEnabled = myTargetDevicesLabel.isEnabled();

    myTargetDevicesLabel.setEnabled(mobile && app);
    myAndroidCheckBox.setEnabled(mobile && app);
    myIOSCheckBox.setEnabled(mobile && app);

    myTargetPlayerLabel.setEnabled(web && sdkSet);
    myTargetPlayerCombo.setEnabled(web && sdkSet);

    mySampleAppCheckBox.setEnabled(app && sdkSet);
    mySampleAppTextField.setEnabled(app && sdkSet);

    myHtmlWrapperCheckBox.setEnabled(web && app && sdkSet);

    if (myTargetDevicesLabel.isEnabled() && !targetDeviceWasEnabled) {
      myAndroidCheckBox.setSelected(true);
      myIOSCheckBox.setSelected(true);
    }

    if (!myTargetDevicesLabel.isEnabled()) {
      // disabled but checked for mobile library
      myAndroidCheckBox.setSelected(mobile);
      myIOSCheckBox.setSelected(mobile);
    }

    if (!mySampleAppCheckBox.isEnabled() || !createSampleAppWasEnabled) {
      mySampleAppCheckBox.setSelected(mySampleAppCheckBox.isEnabled());
    }

    mySampleAppTextField.setEnabled(mySampleAppCheckBox.isEnabled() && mySampleAppCheckBox.isSelected());

    if (!myHtmlWrapperCheckBox.isEnabled() || !createHtmlWrapperWasEnabled) {
      myHtmlWrapperCheckBox.setSelected(myHtmlWrapperCheckBox.isEnabled());
    }

    myEnableHistoryCheckBox.setEnabled(myHtmlWrapperCheckBox.isSelected() && web && app);
    myCheckPlayerVersionCheckBox.setEnabled(myHtmlWrapperCheckBox.isSelected() && web && app);
    myExpressInstallCheckBox.setEnabled(myHtmlWrapperCheckBox.isSelected() && myCheckPlayerVersionCheckBox.isSelected() && web && app);
  }

  public void applyTo(final FlexModuleBuilder moduleBuilder) {
    moduleBuilder.setTargetPlatform((TargetPlatform)myTargetPlatformCombo.getSelectedItem());
    moduleBuilder.setPureActionScript(myPureActionScriptCheckBox.isSelected());
    moduleBuilder.setOutputType((OutputType)myOutputTypeCombo.getSelectedItem());
    moduleBuilder.setAndroidEnabled(myAndroidCheckBox.isSelected());
    moduleBuilder.setIOSEnabled(myIOSCheckBox.isSelected());
    moduleBuilder.setFlexSdk(mySdkCombo.getSelectedSdk());
    moduleBuilder.setTargetPlayer((String)myTargetPlayerCombo.getSelectedItem());
    moduleBuilder.setCreateSampleApp(mySampleAppCheckBox.isEnabled() && mySampleAppCheckBox.isSelected());
    moduleBuilder.setSampleAppName(mySampleAppTextField.getText().trim());
    moduleBuilder.setCreateHtmlWrapperTemplate(myHtmlWrapperCheckBox.isEnabled() && myHtmlWrapperCheckBox.isSelected());
    moduleBuilder.setHtmlWrapperTemplateParameters(myEnableHistoryCheckBox.isSelected(), myCheckPlayerVersionCheckBox.isSelected(),
                                                   myExpressInstallCheckBox.isEnabled() && myExpressInstallCheckBox.isSelected());
  }

  public boolean validate() throws ConfigurationException {
    final Sdk sdk = mySdkCombo.getSelectedSdk();
    if (sdk != null) {

      if (myTargetPlatformCombo.getSelectedItem() == TargetPlatform.Mobile &&
          !FlexSdkUtils.isAirSdkWithoutFlex(sdk) &&
          StringUtil.compareVersionNumbers(sdk.getVersionString(), "4.5") < 0) {
        throw new ConfigurationException(FlexBundle.message("sdk.does.not.support.air.mobile", sdk.getVersionString()));
      }

      if (FlexSdkUtils.isAirSdkWithoutFlex(sdk) && !myPureActionScriptCheckBox.isSelected()) {
        throw new ConfigurationException(StringUtil.capitalize(FlexBundle.message("air.sdk.requires.pure.as", sdk.getName())));
      }

      if (mySampleAppCheckBox.isSelected()) {
        final String fileName = mySampleAppTextField.getText().trim();
        if (fileName.isEmpty()) {
          throw new ConfigurationException(FlexBundle.message("sample.app.name.empty"));
        }

        final String extension = FileUtilRt.getExtension(fileName).toLowerCase();
        if (!"mxml".equals(extension) && !"as".equals(extension)) {
          throw new ConfigurationException(FlexBundle.message("sample.app.incorrect.extension"));
        }
      }
    }

    return true;
  }
}
