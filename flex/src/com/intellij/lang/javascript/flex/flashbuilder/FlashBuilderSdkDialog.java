package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FlashBuilderSdkDialog extends DialogWrapper {

  private JPanel myMainPanel;
  private JLabel myLabel;
  private FlexSdkComboBoxWithBrowseButton myFlexSdkComboWithBrowse;

  public FlashBuilderSdkDialog(final Project project,
                               final int projectsCount) {
    super(project);

    myLabel.setText(FlexBundle.message("sdk.for.imported.projects", projectsCount));
    setTitle(FlexBundle.message("flash.builder.project.import.title"));
    init();
  }

  protected Action[] createActions() {
    return new Action[]{getOKAction()};
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Nullable
  public Sdk getSdk() {
    return myFlexSdkComboWithBrowse.getSelectedSdk();
  }
}
