// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponentNoThrow;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.io.File;

public class AskPathDialog extends DialogWrapper {
  private JPanel myMainPanel;
  private LabeledComponentNoThrow<TextFieldWithBrowseButton> myPathComponent;

  protected AskPathDialog(final String title, final String label, final String initialPath) {
    super(true);
    setTitle(title);
    myPathComponent.setText(label);
    myPathComponent.getComponent().setText(FileUtil.toSystemDependentName(initialPath));
    myPathComponent.getComponent().addBrowseFolderListener(null, FileChooserDescriptorFactory.createSingleFolderDescriptor());
    init();
  }

  @Override
  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  @Override
  protected void doOKAction() {
    final File dir = new File(getPath());
    if (dir.isDirectory() && dir.list().length > 0) {
      if (Messages.YES !=
          Messages
            .showYesNoDialog(myMainPanel, FlexBundle.message("folder.not.empty", dir.getPath()), getTitle(), Messages.getWarningIcon())) {
        return;
      }
    }
    super.doOKAction();
  }

  public String getPath() {
    return FileUtil.toSystemIndependentName(myPathComponent.getComponent().getText().trim());
  }
}
