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

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.fileTypes.FileTypeManager;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.idea.IdeaDialog;
import jetbrains.communicator.util.CommunicatorStrings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Kir
 */
public class ViewFilesDialog extends IdeaDialog {
  private ViewFilesPanel myPanel;
  private final IDEFacade myFacade;

  public ViewFilesDialog(User user, ProjectsData projectsData, IDEFacade facade) {
    super(false);
    setModal(false);
    myFacade = facade;

    init();

    refreshData(user, projectsData);
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myPanel.getTree();
  }

  public void refreshData(User user, ProjectsData data) {
    setTitle(CommunicatorStrings.getMsg("open.files.for.0", user.getDisplayName()));
    myPanel.refreshData(user, data);
  }

  @Override
  protected Action @NotNull [] createActions() {
    setOKButtonText(CommunicatorStrings.getMsg("close"));
    return new Action[] {getOKAction()};
  }

  @Override
  protected JComponent createCenterPanel() {
    myPanel = new ViewFilesPanel(FileTypeManager.getInstance(), ActionManager.getInstance(), myFacade);
    return myPanel;
  }

}
