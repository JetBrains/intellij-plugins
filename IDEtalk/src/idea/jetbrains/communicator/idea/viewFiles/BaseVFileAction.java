/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.communicator.idea.viewFiles;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;

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

  public void update(AnActionEvent e) {
    super.update(e);

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
