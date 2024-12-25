// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.test.ui;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.server.ui.DartCommandLineConfigurationEditorForm;
import com.jetbrains.lang.dart.ide.runner.test.DartTestRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.test.DartTestRunnerParameters;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

import static com.jetbrains.lang.dart.ide.runner.test.DartTestRunnerParameters.Scope.*;

public class DartTestConfigurationEditorForm extends SettingsEditor<DartTestRunConfiguration> {

  private JPanel myMainPanel;
  private JComboBox<DartTestRunnerParameters.Scope> myScopeCombo;
  private JLabel myTestFileLabel;
  private TextFieldWithBrowseButton myFileField;
  private JLabel myDirLabel;
  private TextFieldWithBrowseButton myDirField;
  private JLabel myTestNameLabel;
  private JTextField myTestNameField;
  private JLabel myTargetNameLabel;
  private JTextField myTargetNameField;
  private RawCommandLineEditor myVMOptions;
  private JTextField myTestRunnerOptionsField;
  private EnvironmentVariablesComponent myEnvironmentVariables;

  public DartTestConfigurationEditorForm(final @NotNull Project project) {
    DartCommandLineConfigurationEditorForm.initDartFileTextWithBrowse(project, myFileField);
    // Unfortunately, withFileFilter() only works for files, not directories.
    myDirField.addBrowseFolderListener(project, FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle(DartBundle.message("choose.dart.directory")));
    myDirField.addActionListener(e -> onTestDirChanged(project));

    myScopeCombo.setModel(
      new DefaultComboBoxModel<>(new DartTestRunnerParameters.Scope[]{FOLDER, FILE, GROUP_OR_TEST_BY_NAME}));

    myScopeCombo.setRenderer(SimpleListCellRenderer.create("", DartTestRunnerParameters.Scope::getPresentableName));

    myScopeCombo.addActionListener(e -> onScopeChanged());

    final DocumentAdapter dirListener = new DocumentAdapter() {
      @Override
      protected void textChanged(final @NotNull DocumentEvent e) {
        onTestDirChanged(project);
      }
    };

    myDirField.getTextField().getDocument().addDocumentListener(dirListener);

    // 'Environment variables' is the widest label, anchored by myTestFileLabel
    myTestFileLabel.setPreferredSize(myEnvironmentVariables.getLabel().getPreferredSize());
    myDirLabel.setPreferredSize(myEnvironmentVariables.getLabel().getPreferredSize());
    myEnvironmentVariables.setAnchor(myTestFileLabel);
  }

  @Override
  protected void resetEditorFrom(final @NotNull DartTestRunConfiguration configuration) {
    final DartTestRunnerParameters parameters = configuration.getRunnerParameters();

    myScopeCombo.setSelectedItem(parameters.getScope());
    String testPath = FileUtil.toSystemDependentName(StringUtil.notNullize(parameters.getFilePath()));
    if (parameters.getScope() == FOLDER) {
      myDirField.setText(testPath);
      myTargetNameField.setText(parameters.getTargetName());
    }
    else {
      myFileField.setText(testPath);
    }
    myTestNameField.setText(
      parameters.getScope() == GROUP_OR_TEST_BY_NAME ? StringUtil.notNullize(parameters.getTestName()) : "");
    myVMOptions.setText(parameters.getVMOptions());
    myTestRunnerOptionsField.setText(parameters.getTestRunnerOptions());
    myEnvironmentVariables.setEnvs(parameters.getEnvs());
    myEnvironmentVariables.setPassParentEnvs(parameters.isIncludeParentEnvs());

    onScopeChanged();
  }

  @Override
  protected void applyEditorTo(final @NotNull DartTestRunConfiguration configuration) {
    final DartTestRunnerParameters parameters = configuration.getRunnerParameters();

    final DartTestRunnerParameters.Scope scope = (DartTestRunnerParameters.Scope)myScopeCombo.getSelectedItem();
    parameters.setScope(scope);
    TextFieldWithBrowseButton pathSource = scope == FOLDER ? myDirField : myFileField;
    parameters.setFilePath(StringUtil.nullize(FileUtil.toSystemIndependentName(pathSource.getText().trim())));
    parameters.setTestName(
      scope == GROUP_OR_TEST_BY_NAME ? StringUtil.nullize(myTestNameField.getText().trim()) : null);
    parameters
      .setTargetName(scope == FOLDER ? StringUtil.nullize(myTargetNameField.getText().trim()) : null);
    parameters.setVMOptions(StringUtil.nullize(myVMOptions.getText(), true));
    parameters.setTestRunnerOptions(StringUtil.nullize(myTestRunnerOptionsField.getText().trim()));
    parameters.setEnvs(myEnvironmentVariables.getEnvs());
    parameters.setIncludeParentEnvs(myEnvironmentVariables.isPassParentEnvs());
  }

  private void onScopeChanged() {
    final DartTestRunnerParameters.Scope scope = (DartTestRunnerParameters.Scope)myScopeCombo.getSelectedItem();
    myTestNameLabel.setVisible(scope == GROUP_OR_TEST_BY_NAME);
    myTestNameField.setVisible(scope == GROUP_OR_TEST_BY_NAME);

    boolean folderMode = scope == FOLDER;
    boolean projectWithoutPubspec = Registry.is("dart.projects.without.pubspec", false);
    myFileField.setVisible(!folderMode);
    myTestFileLabel.setVisible(!folderMode);
    myDirField.setVisible(folderMode);
    myDirLabel.setVisible(folderMode);
    myTargetNameField.setVisible(folderMode && projectWithoutPubspec);
    myTargetNameLabel.setVisible(folderMode && projectWithoutPubspec);
  }

  private void onTestDirChanged(Project project) {
    if (!isDirApplicable(myDirField.getText(), project)) {
      myDirField.getTextField().setForeground(JBColor.RED);
      final String message = DartBundle.message("test.dir.not.in.project");
      myDirField.getTextField().setToolTipText(message);
    }
    else {
      myDirField.getTextField().setForeground(UIUtil.getFieldForegroundColor());
      myDirField.getTextField().setToolTipText(null);
    }
  }

  @Override
  protected @NotNull JComponent createEditor() {
    return myMainPanel;
  }

  private static boolean isDirApplicable(final @NotNull String path, final @NotNull Project project) {
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
    return file != null && file.isDirectory() && PubspecYamlUtil.findPubspecYamlFile(project, file) != null;
  }
}
