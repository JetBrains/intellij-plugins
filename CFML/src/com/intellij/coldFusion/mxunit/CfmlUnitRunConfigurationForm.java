/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  private ChangeListener myScopeChangeListener = new ChangeListener() {
    public void stateChanged(ChangeEvent e) {
      updateOnScopeChange();
    }
  };

  private Project myProject;

  private ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> myFileChooser;
  private ComponentWithBrowseButton.BrowseFolderActionListener<JTextField> myDirectoryChooser;

  public CfmlUnitRunConfigurationForm(Project project) {
    myProject = project;

    myMethodPanel.setVisible(false);

    myFileChooser = new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>(null, null, myDirectoryOrFileField, project,
                                                                                         FileChooserDescriptorFactory
                                                                                           .createSingleFileOrFolderDescriptor(),
                                                                                         TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);

    myDirectoryChooser = new ComponentWithBrowseButton.BrowseFolderActionListener<JTextField>(null, null, myDirectoryOrFileField, project,
                                                                                              FileChooserDescriptorFactory
                                                                                                .createSingleFolderDescriptor(),
                                                                                              TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT);
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
  protected void resetEditorFrom(CfmlUnitRunConfiguration s) {
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
  protected void applyEditorTo(CfmlUnitRunConfiguration s) throws ConfigurationException {
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

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myPanel;
  }
}
