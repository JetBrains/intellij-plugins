package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.run.BCCombo;
import com.intellij.lang.javascript.flex.run.FlexLauncherDialog;
import com.intellij.lang.javascript.flex.run.LauncherParameters;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.EnumComboBoxModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FlexUnitRunConfigurationForm extends SettingsEditor<FlexUnitRunConfiguration> {
  private JPanel myMainPanel;

  private BCCombo myBCCombo;
  private WhatToTestForm myWhatToTestForm;

  private JCheckBox myShowLogCheckBox;
  private JComboBox myLogLevelCombo;
  private JLabel myLauncherParametersLabel;
  private TextFieldWithBrowseButton myLauncherParametersTextWithBrowse;
  private JCheckBox myRunTrustedCheckBox;

  private final Project myProject;

  private LauncherParameters myLauncherParameters;

  public FlexUnitRunConfigurationForm(final Project project) {
    myProject = project;

    myBCCombo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        myWhatToTestForm.updateOnBCChange(myBCCombo.getBC(), myBCCombo.getModule());
        updateControls();
      }
    });

    initLogLevelControls();
    initLaunchWithTextWithBrowse();
  }

  private void initLogLevelControls() {
    myShowLogCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (myShowLogCheckBox.isSelected()) {
          IdeFocusManager.getInstance(myProject).requestFocus(myLogLevelCombo, false);
        }
        updateControls();
      }
    });

    myLogLevelCombo.setModel(new EnumComboBoxModel<FlexUnitRunnerParameters.OutputLogLevel>(FlexUnitRunnerParameters.OutputLogLevel.class));
  }

  private void initLaunchWithTextWithBrowse() {
    myLauncherParametersTextWithBrowse.getTextField().setEditable(false);
    myLauncherParametersTextWithBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final FlexLauncherDialog dialog = new FlexLauncherDialog(myProject, myLauncherParameters);
        dialog.show();
        if (dialog.isOK()) {
          myLauncherParameters = dialog.getLauncherParameters();
          updateControls();
        }
      }
    });
  }

  private void updateControls() {
    if (myShowLogCheckBox.isSelected()) {
      myLogLevelCombo.setEnabled(true);
      if (myLogLevelCombo.getSelectedItem() == null) {
        myLogLevelCombo.setSelectedItem(FlexUnitRunnerParameters.OutputLogLevel.values()[0]);
      }
    }
    else {
      myLogLevelCombo.setEnabled(false);
    }

    final FlexIdeBuildConfiguration bc = myBCCombo.getBC();
    final boolean webPlatform = bc != null && bc.getTargetPlatform() == TargetPlatform.Web;
    myLauncherParametersLabel.setVisible(webPlatform);
    myLauncherParametersTextWithBrowse.setVisible(webPlatform);
    myRunTrustedCheckBox.setVisible(webPlatform);

    if (webPlatform) {
      myLauncherParametersTextWithBrowse.getTextField().setText(myLauncherParameters.getPresentableText());
    }

  }

  @NotNull
  protected JComponent createEditor() {
    return myMainPanel;
  }

  protected void resetEditorFrom(final FlexUnitRunConfiguration config) {
    final FlexUnitRunnerParameters params = config.getRunnerParameters();
    myLauncherParameters = params.getLauncherParameters().clone(); // must be before myBCsCombo.setModel()

    myBCCombo.resetFrom(params);
    myWhatToTestForm.resetFrom(myBCCombo.getModule(), myBCCombo.getBC(), params);

    myShowLogCheckBox.setSelected(params.getOutputLogLevel() != null);
    myLogLevelCombo.setEnabled(params.getOutputLogLevel() != null);
    myLogLevelCombo.setSelectedItem(params.getOutputLogLevel() == null ? null : params.getOutputLogLevel());

    myRunTrustedCheckBox.setSelected(params.isTrusted());
  }

  protected void applyEditorTo(final FlexUnitRunConfiguration config) throws ConfigurationException {
    final FlexUnitRunnerParameters params = config.getRunnerParameters();

    myBCCombo.applyTo(params);
    myWhatToTestForm.applyTo(params);

    final FlexUnitRunnerParameters.OutputLogLevel logLevel = myShowLogCheckBox.isSelected()
                                                             ? (FlexUnitRunnerParameters.OutputLogLevel)myLogLevelCombo.getSelectedItem()
                                                             : null;
    params.setOutputLogLevel(logLevel);
    params.setLauncherParameters(myLauncherParameters);
    params.setTrusted(myRunTrustedCheckBox.isSelected());
  }

  protected void disposeEditor() {
    myBCCombo.dispose();
    myWhatToTestForm.dispose();
  }

  private void createUIComponents() {
    myBCCombo = new BCCombo(myProject);
    myWhatToTestForm = new WhatToTestForm(myProject,
                                          new ThrowableComputable<Module, RuntimeConfigurationError>() {
                                            public Module compute() throws RuntimeConfigurationError {
                                              final Module module = myBCCombo.getModule();
                                              if (module != null) return module;
                                              throw new RuntimeConfigurationError(FlexBundle.message("bc.not.specified"));
                                            }
                                          },
                                          new ThrowableComputable<FlexUnitSupport, RuntimeConfigurationError>() {
                                            public FlexUnitSupport compute() throws RuntimeConfigurationError {
                                              final FlexIdeBuildConfiguration bc = myBCCombo.getBC();
                                              if (bc == null) throw new RuntimeConfigurationError(FlexBundle.message("bc.not.specified"));
                                              final FlexUnitSupport support = FlexUnitSupport.getSupport(bc, myBCCombo.getModule());
                                              if (support != null) return support;

                                              throw new RuntimeConfigurationError(
                                                FlexBundle.message("flexunit.not.found.for.bc", bc.getName()));
                                            }
                                          }
    );
  }
}
