package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SelectFlexSdkDialog extends DialogWrapper {

  private JPanel myMainPanel;
  private JLabel myLabel;
  private FlexSdkComboBoxWithBrowseButton myFlexSdkComboWithBrowse;

  public SelectFlexSdkDialog(final Project project, final String title, final String label) {
    super(project);
    setTitle(title);
    myLabel.setText(label);
    init();
  }

  @Override
  protected Action @NotNull [] createActions() {
    return new Action[]{getOKAction()};
  }

  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Nullable
  public Sdk getSdk() {
    return myFlexSdkComboWithBrowse.getSelectedSdk();
  }
}
