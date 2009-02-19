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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;
import jetbrains.communicator.commands.ShowDiffCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.idea.actions.BaseAction;
import jetbrains.communicator.util.StringUtil;

import javax.swing.*;

/**
 * @author Kir
 */
abstract class DiffAction extends BaseAction<ShowDiffCommand> {
  private final JTree myFileTree;
  public static final String USER = "IDETalkUser";

  DiffAction(JTree fileTree) {
    super(ShowDiffCommand.class);
    myFileTree = fileTree;
  }

  public void update(AnActionEvent e) {
    e.getPresentation().setText(StringUtil.getMsg("diff.action.text"));
    e.getPresentation().setDescription(StringUtil.getMsg("diff.action.description"));
    e.getPresentation().setIcon(IconLoader.getIcon("/actions/diff.png"));

    ShowDiffCommand showDiffCommand = getCommand(e);
    if (showDiffCommand != null) {
      showDiffCommand.setVFile(BaseVFileAction.getVFile(myFileTree));
      showDiffCommand.setUser(getUser());
    }

    super.update(e);
  }

  protected abstract User getUser();

}
