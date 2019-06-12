// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.viewFiles;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import jetbrains.communicator.commands.ShowDiffCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.idea.actions.BaseAction;
import jetbrains.communicator.util.CommunicatorStrings;
import org.jetbrains.annotations.NotNull;

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

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setText(CommunicatorStrings.getMsg("diff.action.text"));
    e.getPresentation().setDescription(CommunicatorStrings.getMsg("diff.action.description"));
    e.getPresentation().setIcon(AllIcons.Actions.Diff);

    ShowDiffCommand showDiffCommand = getCommand(e);
    if (showDiffCommand != null) {
      showDiffCommand.setVFile(BaseVFileAction.getVFile(myFileTree));
      showDiffCommand.setUser(getUser());
    }

    super.update(e);
  }

  protected abstract User getUser();

}
