// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.actions;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DartPubBuildDialog extends DialogWrapper {

  private static final String DART_BUILD_INPUT_KEY = "DART_BUILD_INPUT_KEY";
  private static final String DEFAULT_INPUT_FOLDER = "web";
  private static final String DART_BUILD_OUTPUT_KEY = "DART_PUB_BUILD_OUTPUT_KEY"; // _PUB_ - for compatibility
  private static final String DEFAULT_OUTPUT_FOLDER = "build";

  private JPanel myMainPanel;

  private JTextField myInputFolderTextField;

  private TextFieldWithBrowseButton myOutputFolderField;

  private final @NotNull Project myProject;

  public DartPubBuildDialog(final @NotNull Project project, final @NotNull VirtualFile packageDir) {
    super(project);
    myProject = project;

    setTitle(DartBundle.message("dart.webdev.build.title"));
    setOKButtonText(DartBundle.message("button.text.build2"));

    var packagePathSlash = FileUtil.toSystemDependentName(packageDir.getPath() + "/");
    myOutputFolderField.addBrowseFolderListener(
      myProject,
      FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle(DartBundle.message("button.browse.dialog.title.output.folder")),
      new TextComponentAccessor<>() {
        @Override
        public String getText(JTextField component) {
          final String path = component.getText();
          if (SystemInfo.isWindows && FileUtil.isWindowsAbsolutePath(path) || !SystemInfo.isWindows && FileUtil.isUnixAbsolutePath(path)) {
            return path;
          }
          return packagePathSlash + path;
        }

        @Override
        public void setText(JTextField component, @NotNull String text) {
          if (text.startsWith(packagePathSlash) && !text.equals(packagePathSlash)) {
            component.setText(text.substring(packagePathSlash.length()));
          }
          else {
            component.setText(text);
          }
        }
      });

    reset();
    init();
  }

  private void reset() {
    final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(myProject);

    myInputFolderTextField.setText(propertiesComponent.getValue(DART_BUILD_INPUT_KEY, DEFAULT_INPUT_FOLDER));
    myOutputFolderField.setText(propertiesComponent.getValue(DART_BUILD_OUTPUT_KEY, DEFAULT_OUTPUT_FOLDER));
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  protected @Nullable ValidationInfo doValidate() {
    if (myInputFolderTextField.getText().trim().isEmpty()) {
      return new ValidationInfo(DartBundle.message("validation.info.input.folder.not.specified"));
    }

    if (myOutputFolderField.getText().trim().isEmpty()) {
      return new ValidationInfo(DartBundle.message("validation.info.output.folder.not.specified"));
    }

    if (myInputFolderTextField.getText().trim().equals(myOutputFolderField.getText().trim())) {
      return new ValidationInfo(DartBundle.message("validation.info.input.and.output.folders.must.be.different"));
    }

    return null;
  }

  @Override
  protected void doOKAction() {
    saveDialogState();
    super.doOKAction();
  }

  private void saveDialogState() {
    final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(myProject);

    final String inputPath = StringUtil.nullize(myInputFolderTextField.getText().trim());
    propertiesComponent.setValue(DART_BUILD_INPUT_KEY, inputPath, DEFAULT_INPUT_FOLDER);

    final String outputPath = StringUtil.nullize(myOutputFolderField.getText().trim());
    propertiesComponent.setValue(DART_BUILD_OUTPUT_KEY, outputPath, DEFAULT_OUTPUT_FOLDER);
  }

  public @NotNull String getInputFolder() {
    String path = myInputFolderTextField.getText().trim();
    if (path.isEmpty()) path = DEFAULT_INPUT_FOLDER;
    return path;
  }

  public @NotNull String getOutputFolder() {
    String path = myOutputFolderField.getText().trim();
    if (path.isEmpty()) path = DEFAULT_OUTPUT_FOLDER;
    return path;
  }
}
