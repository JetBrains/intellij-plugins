package com.intellij.flex.uiDesigner.preview;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class MxmlPreviewToolWindowForm {
  private VirtualFile file;
  private JPanel contentPanel;
  private MxmlPreviewPanel previewPanel;

  volatile boolean waitingForGetDocument;

  MxmlPreviewToolWindowForm() {
  }

  public JPanel getContentPanel() {
    return contentPanel;
  }

  public VirtualFile getFile() {
    return file;
  }

  public void setFile(@Nullable VirtualFile value) {
    waitingForGetDocument = false;
    file = value;
  }

  public MxmlPreviewPanel getPreviewPanel() {
    return previewPanel;
  }
}