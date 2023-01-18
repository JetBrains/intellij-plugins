package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ui.SdkEditor;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;

public class FlexSdkEditorDialog extends DialogWrapper {
  private JPanel myContentPane;
  private final SdkEditor mySdkEditor;

  protected FlexSdkEditorDialog(Project project, String sdkPath) {
    super(project);
    mySdkEditor = null;//new SdkEditor();
  }

  @Override
  protected JComponent createCenterPanel() {
    return myContentPane;
  }
}
