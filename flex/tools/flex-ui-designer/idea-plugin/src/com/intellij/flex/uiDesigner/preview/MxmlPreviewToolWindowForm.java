package com.intellij.flex.uiDesigner.preview;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class MxmlPreviewToolWindowForm {
  private VirtualFile file;
  private JPanel contentPanel;
  private MxmlPreviewPanel previewPanel;

  public MxmlPreviewToolWindowForm(Project project, MxmlPreviewToolWindowManager mxmlPreviewToolWindowManager) {
  }

  public JPanel getContentPanel() {
    return contentPanel;
  }

  public VirtualFile getFile() {
    return file;
  }

  public void setFile(@Nullable VirtualFile value) {
    file = value;
  }

  public MxmlPreviewPanel getPreviewPanel() {
    return previewPanel;
  }
}