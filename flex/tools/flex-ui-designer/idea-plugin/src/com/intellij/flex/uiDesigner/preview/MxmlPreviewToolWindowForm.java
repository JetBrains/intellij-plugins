package com.intellij.flex.uiDesigner.preview;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

class MxmlPreviewToolWindowForm {
  private VirtualFile file;
  private JPanel contentPanel;
  private MxmlPreviewPanel previewPanel;

  final AtomicBoolean waitingForGetDocument = new AtomicBoolean();

  public MxmlPreviewToolWindowForm(Project project, MxmlPreviewToolWindowManager mxmlPreviewToolWindowManager) {
  }

  public JPanel getContentPanel() {
    return contentPanel;
  }

  public VirtualFile getFile() {
    return file;
  }

  public void setFile(@Nullable VirtualFile value) {
    waitingForGetDocument.set(false);
    file = value;
  }

  public MxmlPreviewPanel getPreviewPanel() {
    return previewPanel;
  }
}