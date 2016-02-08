package com.jetbrains.lang.dart.ide.actions;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.components.JBRadioButton;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DartPubBuildDialog extends DialogWrapper {

  private static final String DART_PUB_BUILD_MODE_KEY = "DART_PUB_BUILD_MODE";
  private static final String DART_PUB_CUSTOM_BUILD_MODE_KEY = "DART_PUB_CUSTOM_BUILD_MODE";
  private static final String RELEASE_MODE = "release";
  private static final String DEBUG_MODE = "debug";
  private static final String OTHER_MODE = "other";

  private JPanel myMainPanel;
  private JBRadioButton myReleaseRadioButton;
  private JBRadioButton myDebugRadioButton;
  private JBRadioButton myOtherRadioButton;
  private JTextField myOtherModeTextField;

  private final @NotNull Project myProject;

  public DartPubBuildDialog(@NotNull final Project project) {
    super(project);
    myProject = project;

    setTitle(DartBundle.message("dart.pub.build.title"));

    ActionListener listener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    };
    myReleaseRadioButton.addActionListener(listener);
    myDebugRadioButton.addActionListener(listener);
    myOtherRadioButton.addActionListener(listener);
    myOtherRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if (myOtherRadioButton.isSelected()) {
          IdeFocusManager.getInstance(myProject).requestFocus(myOtherModeTextField, true);
        }
      }
    });

    reset();
    init();
  }

  private void reset() {
    final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(myProject);

    final String mode = propertiesComponent.getValue(DART_PUB_BUILD_MODE_KEY, RELEASE_MODE);
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
    propertiesComponent.setValue(DART_PUB_BUILD_MODE_KEY, mode);

    if (myOtherRadioButton.isSelected()) {
      propertiesComponent.setValue(DART_PUB_CUSTOM_BUILD_MODE_KEY, myOtherModeTextField.getText().trim());
    }
  }

  @NotNull
  public String getPubBuildMode() {
    if (myReleaseRadioButton.isSelected()) return RELEASE_MODE;
    if (myDebugRadioButton.isSelected()) return DEBUG_MODE;
    return myOtherModeTextField.getText().trim();
  }
}
