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
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.components.JBRadioButton;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionListener;

public class DartPubBuildDialog extends DialogWrapper {

  private static final String DART_PUB_BUILD_MODE_KEY = "DART_PUB_BUILD_MODE";
  private static final String DART_PUB_CUSTOM_BUILD_MODE_KEY = "DART_PUB_CUSTOM_BUILD_MODE";

  private static final String RELEASE_MODE = "release";
  private static final String DEBUG_MODE = "debug";
  private static final String OTHER_MODE = "other";
  private static final String DEFAULT_MODE = RELEASE_MODE;

  private static final String DART_PUB_BUILD_OUTPUT_KEY = "DART_PUB_BUILD_OUTPUT_KEY";
  private static final String DEFAULT_OUTPUT_FOLDER = "build";

  private JPanel myMainPanel;
  private JBRadioButton myReleaseRadioButton;
  private JBRadioButton myDebugRadioButton;
  private JBRadioButton myOtherRadioButton;
  private JTextField myOtherModeTextField;
  private TextFieldWithBrowseButton myOutputFolderField;

  private final @NotNull Project myProject;

  public DartPubBuildDialog(@NotNull final Project project, @NotNull final VirtualFile packageDir) {
    super(project);
    myProject = project;

    setTitle(DartBundle.message("dart.pub.build.title"));

    ActionListener listener = e -> updateControls();
    myReleaseRadioButton.addActionListener(listener);
    myDebugRadioButton.addActionListener(listener);
    myOtherRadioButton.addActionListener(listener);
    myOtherRadioButton.addActionListener(e -> {
      if (myOtherRadioButton.isSelected()) {
        IdeFocusManager.getInstance(myProject).requestFocus(myOtherModeTextField, true);
      }
    });

    final String packagePathSlash = FileUtil.toSystemDependentName(packageDir.getPath() + "/");
    myOutputFolderField.addBrowseFolderListener("Output Folder", null, myProject,
                                                FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                                                new TextComponentAccessor<JTextField>() {
                                                  @Override
                                                  public String getText(JTextField component) {
                                                    final String path = component.getText();
                                                    if (SystemInfo.isWindows && FileUtil.isWindowsAbsolutePath(path) ||
                                                        !SystemInfo.isWindows && FileUtil.isUnixAbsolutePath(path)) {
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

    final String mode = propertiesComponent.getValue(DART_PUB_BUILD_MODE_KEY, DEFAULT_MODE);
    if (mode.equals(RELEASE_MODE)) {
      myReleaseRadioButton.setSelected(true);
    }
    else if (mode.equals(DEBUG_MODE)) {
      myDebugRadioButton.setSelected(true);
    }
    else {
      myOtherRadioButton.setSelected(true);
    }

    myOtherModeTextField.setText(propertiesComponent.getValue(DART_PUB_CUSTOM_BUILD_MODE_KEY, ""));

    myOutputFolderField.setText(propertiesComponent.getValue(DART_PUB_BUILD_OUTPUT_KEY, DEFAULT_OUTPUT_FOLDER));

    updateControls();
  }

  private void updateControls() {
    myOtherModeTextField.setEnabled(myOtherRadioButton.isSelected());
  }

  @Nullable
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Nullable
  public JComponent getPreferredFocusedComponent() {
    if (myOtherRadioButton.isSelected()) return myOtherModeTextField;
    return null;
  }

  @Nullable
  protected ValidationInfo doValidate() {
    if (myOtherRadioButton.isSelected() && StringUtil.isEmptyOrSpaces(myOtherModeTextField.getText())) {
      return new ValidationInfo(DartBundle.message("pub.build.mode.not.specified"));
    }
    return null;
  }

  protected void doOKAction() {
    saveDialogState();
    super.doOKAction();
  }

  private void saveDialogState() {
    final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(myProject);

    final String mode = myReleaseRadioButton.isSelected() ? RELEASE_MODE
                                                          : myDebugRadioButton.isSelected() ? DEBUG_MODE
                                                                                            : OTHER_MODE;
    propertiesComponent.setValue(DART_PUB_BUILD_MODE_KEY, mode, DEFAULT_MODE);

    if (myOtherRadioButton.isSelected()) {
      propertiesComponent.setValue(DART_PUB_CUSTOM_BUILD_MODE_KEY, myOtherModeTextField.getText().trim());
    }

    final String outputPath = StringUtil.nullize(myOutputFolderField.getText().trim());
    propertiesComponent.setValue(DART_PUB_BUILD_OUTPUT_KEY, outputPath, DEFAULT_OUTPUT_FOLDER);
  }

  @NotNull
  public String getPubBuildMode() {
    if (myReleaseRadioButton.isSelected()) return RELEASE_MODE;
    if (myDebugRadioButton.isSelected()) return DEBUG_MODE;
    return myOtherModeTextField.getText().trim();
  }

  @NotNull
  public String getOutputFolder() {
    String path = myOutputFolderField.getText().trim();
    if (path.isEmpty()) path = DEFAULT_OUTPUT_FOLDER;
    return path;
  }
}
