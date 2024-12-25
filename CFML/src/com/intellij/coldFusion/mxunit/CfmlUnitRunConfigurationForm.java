// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.mxunit;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CfmlUnitRunConfigurationForm extends SettingsEditor<CfmlUnitRunConfiguration> {
  private JRadioButton myDirectoryRadioButton;
  private JRadioButton myComponentRadioButton;
  private JRadioButton myMethodRadioButton;
  private JLabel myWebPathLabel;
  private JPanel myClassPanel;
  private JLabel myFileOrDirectoryLabel;
  private TextFieldWithBrowseButton myDirectoryOrFileField;
  private JPanel myMethodPanel;
  private TextFieldWithBrowseButton myMethodField;
  private JLabel myMethodLabel;
  private JTextField myWebPathTextField;
  private JPanel myPanel;
  private final ChangeListener myScopeChangeListener = new ChangeListener() {
    @Override
    public void stateChanged(ChangeEvent e) {
      updateOnScopeChange();
    }
  };

  private final Project myProject;

  private final ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> myFileChooser;
  private final ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> myDirectoryChooser;

  public CfmlUnitRunConfigurationForm(Project project) {
    myProject = project;
    myMethodPanel.setVisible(false);
    myFileChooser = new ComponentWithBrowseButton.BrowseFolderActionListener<>(
      myDirectoryOrFileField, project, FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor(), TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
    myDirectoryChooser = new ComponentWithBrowseButton.BrowseFolderActionListener<>(
      myDirectoryOrFileField, project, FileChooserDescriptorFactory.createSingleFolderDescriptor(), TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
    myMethodRadioButton.addChangeListener(myScopeChangeListener);
    myDirectoryRadioButton.addChangeListener(myScopeChangeListener);
    myComponentRadioButton.addChangeListener(myScopeChangeListener);
  }

  private void removeActionListeners() {
    myDirectoryOrFileField.removeActionListener(myFileChooser);
    myDirectoryOrFileField.removeActionListener(myDirectoryChooser);
  }

  protected void updateOnScopeChange() {
    removeActionListeners();
    if (myDirectoryRadioButton.isSelected()) {
      myMethodPanel.setVisible(false);
      myDirectoryOrFileField.addActionListener(myDirectoryChooser);
    }
    else if (myComponentRadioButton.isSelected()) {
      myMethodPanel.setVisible(false);
      myDirectoryOrFileField.addActionListener(myFileChooser);
    }
    else if (myMethodRadioButton.isSelected()) {
      myMethodPanel.setVisible(true);
      myDirectoryOrFileField.addActionListener(myFileChooser);
    }
  }

  @Override
  protected void resetEditorFrom(@NotNull CfmlUnitRunConfiguration s) {
    final CfmlUnitRunnerParameters parameters = s.getRunnerParameters();
    if (parameters.getScope() == CfmlUnitRunnerParameters.Scope.Component) {
      myComponentRadioButton.setSelected(true);
    }
    else if (parameters.getScope() == CfmlUnitRunnerParameters.Scope.Directory) {
      myDirectoryRadioButton.setSelected(true);
    }
    else if (parameters.getScope() == CfmlUnitRunnerParameters.Scope.Method) {
      myMethodRadioButton.setSelected(true);
    }
    myDirectoryOrFileField.setText(parameters.getPath());
    myWebPathTextField.setText(parameters.getWebPath());
    myMethodField.setText(parameters.getMethod());
  }

  @Override
  protected void applyEditorTo(@NotNull CfmlUnitRunConfiguration s) throws ConfigurationException {
    final CfmlUnitRunnerParameters parameters = s.getRunnerParameters();
    if (myComponentRadioButton.isSelected()) {
      parameters.setScope(CfmlUnitRunnerParameters.Scope.Component);
    }
    else if (myDirectoryRadioButton.isSelected()) {
      parameters.setScope(CfmlUnitRunnerParameters.Scope.Directory);
    }
    else if (myMethodRadioButton.isSelected()) {
      parameters.setScope(CfmlUnitRunnerParameters.Scope.Method);
    }

    parameters.setWebPath(myWebPathTextField.getText());
    parameters.setMethod(myMethodField.getText());
    parameters.setPath(myDirectoryOrFileField.getText());
  }

  @Override
  protected @NotNull JComponent createEditor() {
    return myPanel;
  }
}
