package com.intellij.flex.uiDesigner.preview;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

class MxmlPreviewToolWindowForm {
  private XmlFile file;
  private JPanel contentPanel;
  private MxmlPreviewPanel previewPanel;

  public MxmlPreviewToolWindowForm(Project project, MxmlPreviewToolWindowManager mxmlPreviewToolWindowManager) {
  }

  public JPanel getContentPanel() {
    return contentPanel;
  }

  public XmlFile getFile() {
    return file;
  }

  public void setFile(@Nullable XmlFile value) {
    final boolean fileChanged = !Comparing.equal(file, value);
    file = value;
  }

  public MxmlPreviewPanel getPreviewPanel() {
    return previewPanel;
  }
}