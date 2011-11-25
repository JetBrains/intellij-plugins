package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.ModulesComboboxWrapper;
import com.intellij.lang.javascript.flex.run.FlexLauncherDialog;
import com.intellij.lang.javascript.flex.run.FlexRunConfiguration;
import com.intellij.lang.javascript.flex.run.LauncherParameters;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.EnumComboBoxModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FlexUnitRunConfigurationForm extends SettingsEditor<FlexUnitRunConfiguration> {
  private final Project myProject;

  private JComboBox myModuleCombo;
  private JPanel myPanel;
  private WhatToTestForm myWhatToTestForm;

  private JComboBox myLogLevelCombo;
  private JCheckBox myShowLogCheckBox;
  private JCheckBox myRunTrustedCheckBox;
  private JLabel myLaunchWithLabel;
  private TextFieldWithBrowseButton myLaunchWithTextWithBrowse;
  private FlexSdkComboBoxWithBrowseButton myDebuggerSdkCombo;
  private ModulesComboboxWrapper myModulesComboboxWrapper;

  private LauncherParameters.LauncherType myLauncherType;
  private BrowsersConfiguration.BrowserFamily myBrowserFamily;
  private String myPlayerPath;

  public FlexUnitRunConfigurationForm(final Project project) {
    myProject = project;

    myModulesComboboxWrapper = new ModulesComboboxWrapper(myModuleCombo);
    myModulesComboboxWrapper.addActionListener(new ModulesComboboxWrapper.Listener() {
      public void moduleChanged() {
        myWhatToTestForm.updateOnModuleChange(getSelectedModuleName());
        updateRunTrustedOptionVisibility();
        updateLauncherTextWithBrowse();
        final Module module = myModulesComboboxWrapper.getSelectedModule();
        myDebuggerSdkCombo.setModuleSdk(module == null ? null : FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module));
      }
    });

    myShowLogCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (myShowLogCheckBox.isSelected()) {
          myLogLevelCombo.setEnabled(true);
          if (myLogLevelCombo.getSelectedItem() == null) {
            myLogLevelCombo.setSelectedItem(FlexUnitRunnerParameters.OutputLogLevel.values()[0]);
          }
          IdeFocusManager.getInstance(project).requestFocus(myLogLevelCombo, false);
        }
        else {
          myLogLevelCombo.setEnabled(false);
        }
      }
    });

    myLogLevelCombo.setModel(new EnumComboBoxModel<FlexUnitRunnerParameters.OutputLogLevel>(FlexUnitRunnerParameters.OutputLogLevel.class));

    myLaunchWithTextWithBrowse.getTextField().setEditable(false);
    myLaunchWithTextWithBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final FlexLauncherDialog dialog = new FlexLauncherDialog(myProject, myLauncherType, myBrowserFamily, myPlayerPath);
        dialog.show();
        if (dialog.isOK()) {
          myLauncherType = dialog.getLauncherType();
          final BrowsersConfiguration.BrowserFamily browser = dialog.getBrowserFamily();
          if (browser != null) {
            myBrowserFamily = browser;
          }
          myPlayerPath = dialog.getPlayerPath();

          updateLauncherTextWithBrowse();
        }
      }
    });

    myDebuggerSdkCombo.showModuleSdk(true);
  }

  protected void resetEditorFrom(FlexUnitRunConfiguration s) {
    final FlexUnitRunnerParameters params = s.getRunnerParameters();

    myModulesComboboxWrapper.configure(myProject, params.getModuleName());
    myWhatToTestForm.resetFrom(params);

    myShowLogCheckBox.setSelected(params.getOutputLogLevel() != null);
    myLogLevelCombo.setEnabled(params.getOutputLogLevel() != null);
    myLogLevelCombo.setSelectedItem(params.getOutputLogLevel() == null ? null : params.getOutputLogLevel());

    myRunTrustedCheckBox.setSelected(params.isRunTrusted());

    myLauncherType = params.getLauncherType();
    myBrowserFamily = params.getBrowserFamily();
    myPlayerPath = params.getPlayerPath();

    final Module module = myModulesComboboxWrapper.getSelectedModule();
    myDebuggerSdkCombo.setModuleSdk(module == null ? null : FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module));
    myDebuggerSdkCombo.setSelectedSdkRaw(params.getDebuggerSdkRaw());

    updateLauncherTextWithBrowse();
    updateRunTrustedOptionVisibility();
  }

  private void updateLauncherTextWithBrowse() {
    if (myLauncherType == null) {
      // not initialized yet
      return;
    }

    final Module module = myModulesComboboxWrapper.getSelectedModule();
    if (module != null && FlexSdkUtils.hasDependencyOnAir(module)) {
      myLaunchWithTextWithBrowse.setText("AIR");
      myLaunchWithLabel.setEnabled(false);
      myLaunchWithTextWithBrowse.setEnabled(false);
    }
    else {
      myLaunchWithTextWithBrowse.setText(FlexRunConfiguration.getLauncherDescription(myLauncherType, myBrowserFamily, myPlayerPath));
      myLaunchWithLabel.setEnabled(true);
      myLaunchWithTextWithBrowse.setEnabled(true);
    }
  }

  private void updateRunTrustedOptionVisibility() {
    final Module module = myModulesComboboxWrapper.getSelectedModule();
    myRunTrustedCheckBox.setEnabled(module != null && !FlexSdkUtils.hasDependencyOnAir(module));
  }

  protected void applyEditorTo(FlexUnitRunConfiguration s) throws ConfigurationException {
    final FlexUnitRunnerParameters params = s.getRunnerParameters();

    myWhatToTestForm.applyTo(params);

    params.setModuleName(myModulesComboboxWrapper.getSelectedText());

    final FlexUnitCommonParameters.OutputLogLevel logLevel = myShowLogCheckBox.isSelected()
                                                             ? (FlexUnitRunnerParameters.OutputLogLevel)myLogLevelCombo.getSelectedItem()
                                                             : null;
    params.setOutputLogLevel(logLevel);

    params.setRunTrusted(myRunTrustedCheckBox.isSelected());

    params.setLauncherType(myLauncherType);
    params.setBrowserFamily(myBrowserFamily);
    params.setPlayerPath(myPlayerPath);
    params.setDebuggerSdkRaw(myDebuggerSdkCombo.getSelectedSdkRaw());
  }

  @NotNull
  protected JComponent createEditor() {
    return myPanel;
  }

  @SuppressWarnings({"BoundFieldAssignment"})
  protected void disposeEditor() {
    myModuleCombo = null;
    myPanel = null;
    myModulesComboboxWrapper = null;
    myWhatToTestForm.dispose();
  }

  public String getSelectedModuleName() {
    return myModulesComboboxWrapper.getSelectedText();
  }

  private void createUIComponents() {
    myWhatToTestForm = new WhatToTestForm(myProject,
                                          new ThrowableComputable<Module, RuntimeConfigurationError>() {
                                            public Module compute() throws RuntimeConfigurationError {
                                              final Module module = myModulesComboboxWrapper.getSelectedModule();
                                              if (module != null) return module;
                                              throw new RuntimeConfigurationError(FlexBundle.message("module.not.specified"));
                                            }
                                          },
                                          new ThrowableComputable<FlexUnitSupport, RuntimeConfigurationError>() {
                                            public FlexUnitSupport compute() throws RuntimeConfigurationError {
                                              return FlexUnitRunConfiguration.getFlexUnitSupport(myProject, getSelectedModuleName()).second;
                                            }
                                          }
    );

    myDebuggerSdkCombo = new FlexSdkComboBoxWithBrowseButton(FlexSdkComboBoxWithBrowseButton.FLEX_RELATED_SDK);
  }
}
