// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.viewFiles;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Kir
 */
public abstract class BaseVFileAction extends AnAction {
  protected final JTree myFileTree;
  protected final IDEFacade myIdeFacade;

  public BaseVFileAction(String text, String description, Icon icon, JTree fileTree, IDEFacade ideFacade) {
    super(text, description, icon);
    myFileTree = fileTree;
    myIdeFacade = ideFacade;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {

    e.getPresentation().setEnabled(isEnabled());
  }

  boolean isEnabled() {
    VFile vFile = getVFile(myFileTree);
    return vFile != null && myIdeFacade.hasFile(vFile);
  }

  protected static VFile getVFile(JTree fileTree) {
    if (fileTree.getSelectionCount() != 1) {
      return null;
    }
    Object lastPathComponent = fileTree.getSelectionPath().getLastPathComponent();
    if (lastPathComponent instanceof ViewFilesPanel.FileNode) {
      ViewFilesPanel.FileNode fileNode = (ViewFilesPanel.FileNode) lastPathComponent;
      return fileNode.getVFile();
    }
    return null;
  }
}
