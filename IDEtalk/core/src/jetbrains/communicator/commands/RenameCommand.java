// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.commands;

import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.UserListComponent;
import jetbrains.communicator.util.CommunicatorStrings;

import javax.swing.*;

/**
 * @author Kir
 */
public class RenameCommand extends EnabledWhenFocusedCommand implements NamedUserCommand {

  public RenameCommand(UserListComponent facade) {
    super(facade);
  }

  @Override
  public boolean enabled() {
    return myUserListComponent.isSingleItemSelected();
  }

  @Override
  public void execute() {
    myUserListComponent.startEditing();
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getName() {
    Object[] nodes = myUserListComponent.getSelectedNodes();
    String msgCode = "RenameCommand.rename";
    if (nodes.length == 1) {
      msgCode = nodes[0] instanceof User ? "RenameCommand.rename.user" : "RenameCommand.rename.group";
    }
    return CommunicatorStrings.getMsg(msgCode);
  }
}
