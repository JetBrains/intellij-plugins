// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server.ui;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.ide.util.TreeFileChooser;
import com.intellij.ide.util.TreeFileChooserFactory;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBCheckBox;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunnerParameters;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DartCommandLineConfigurationEditorForm extends SettingsEditor<DartCommandLineRunConfiguration> {
  private JPanel myMainPanel;
  private JLabel myDartFileLabel;
  private TextFieldWithBrowseButton myFileField;
  private RawCommandLineEditor myVMOptions;
  private JBCheckBox myCheckedModeOrEnableAssertsCheckBox;
  private RawCommandLineEditor myArguments;
  private TextFieldWithBrowseButton myWorkingDirectory;
  private EnvironmentVariablesComponent myEnvironmentVariables;

  public DartCommandLineConfigurationEditorForm(final Project project) {
    initDartFileTextWithBrowse(project, myFileField);

    myWorkingDirectory.addBrowseFolderListener(project, FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle(DartBundle.message("dialog.title.select.working.directory")));

    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk != null && StringUtil.compareVersionNumbers(sdk.getVersion(), "2") < 0) {
      myCheckedModeOrEnableAssertsCheckBox.setText(DartBundle.message("command.line.run.config.checkbox.checked.mode"));
      myCheckedModeOrEnableAssertsCheckBox.setMnemonic('c');
    }
    else {
      myCheckedModeOrEnableAssertsCheckBox.setText(DartBundle.message("command.line.run.config.checkbox.enable.asserts"));
      myCheckedModeOrEnableAssertsCheckBox.setMnemonic('l');
    }

    // 'Environment variables' is the widest label, anchored by myDartFileLabel
    myDartFileLabel.setPreferredSize(myEnvironmentVariables.getLabel().getPreferredSize());
    myEnvironmentVariables.setAnchor(myDartFileLabel);
  }

  public static void initDartFileTextWithBrowse(final @NotNull Project project,
                                                final @NotNull TextFieldWithBrowseButton textWithBrowse) {
    textWithBrowse.getButton().addActionListener(e -> {
      final String initialPath = FileUtil.toSystemIndependentName(textWithBrowse.getText().trim());
      final VirtualFile initialFile = initialPath.isEmpty() ? null : LocalFileSystem.getInstance().findFileByPath(initialPath);
      final PsiFile initialPsiFile = initialFile == null ? null : PsiManager.getInstance(project).findFile(initialFile);

      TreeFileChooser fileChooser = TreeFileChooserFactory.getInstance(project)
        .createFileChooser(DartBundle.message("choose.dart.main.file"), initialPsiFile, DartFileType.INSTANCE, null);

      fileChooser.showDialog();

      final PsiFile selectedFile = fileChooser.getSelectedFile();
      final VirtualFile virtualFile = selectedFile == null ? null : selectedFile.getVirtualFile();
      if (virtualFile != null) {
        final String path = FileUtil.toSystemDependentName(virtualFile.getPath());
        textWithBrowse.setText(path);
      }
    });
  }

  @Override
  protected void resetEditorFrom(final @NotNull DartCommandLineRunConfiguration configuration) {
    final DartCommandLineRunnerParameters parameters = configuration.getRunnerParameters();

    myFileField.setText(FileUtil.toSystemDependentName(StringUtil.notNullize(parameters.getFilePath())));
    myArguments.setText(StringUtil.notNullize(parameters.getArguments()));
    myVMOptions.setText(StringUtil.notNullize(parameters.getVMOptions()));
    myCheckedModeOrEnableAssertsCheckBox.setSelected(parameters.isCheckedModeOrEnableAsserts());
    myWorkingDirectory.setText(FileUtil.toSystemDependentName(StringUtil.notNullize(parameters.getWorkingDirectory())));
    myEnvironmentVariables.setEnvs(parameters.getEnvs());
    myEnvironmentVariables.setPassParentEnvs(parameters.isIncludeParentEnvs());
  }

  @Override
  protected void applyEditorTo(final @NotNull DartCommandLineRunConfiguration configuration) {
    final DartCommandLineRunnerParameters parameters = configuration.getRunnerParameters();

    parameters.setFilePath(StringUtil.nullize(FileUtil.toSystemIndependentName(myFileField.getText().trim()), true));
    parameters.setArguments(StringUtil.nullize(myArguments.getText(), true));
    parameters.setVMOptions(StringUtil.nullize(myVMOptions.getText(), true));
    parameters.setCheckedModeOrEnableAsserts(myCheckedModeOrEnableAssertsCheckBox.isSelected());
    parameters.setWorkingDirectory(StringUtil.nullize(FileUtil.toSystemIndependentName(myWorkingDirectory.getText().trim()), true));
    parameters.setEnvs(myEnvironmentVariables.getEnvs());
    parameters.setIncludeParentEnvs(myEnvironmentVariables.isPassParentEnvs());
  }

  @Override
  protected @NotNull JComponent createEditor() {
    return myMainPanel;
  }
}
