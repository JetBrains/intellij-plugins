package com.intellij.flex.uiDesigner.preview;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class MxmlPreviewToolWindowForm {
  private PsiFile file;
  private JPanel contentPanel;

  public MxmlPreviewToolWindowForm(Project project, MxmlPreviewToolWindowManager mxmlPreviewToolWindowManager) {

  }

  public JPanel getContentPanel() {
    return contentPanel;
  }

  public PsiFile getFile() {
    return file;
  }

  public void setFile(@Nullable PsiFile value) {
    final boolean fileChanged = !Comparing.equal(file, value);
    file = value;
  }
}