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

package jetbrains.communicator.commands;

import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.UserListComponent;
import jetbrains.communicator.util.StringUtil;

import javax.swing.*;

/**
 * @author Kir
 */
public class RenameCommand extends EnabledWhenFocusedCommand implements NamedUserCommand {

  public RenameCommand(UserListComponent facade) {
    super(facade);
  }

  public boolean enabled() {
    return myUserListComponent.isSingleItemSelected();
  }

  public void execute() {
    myUserListComponent.startEditing();
  }

  public Icon getIcon() {
    return null;
  }

  public String getName() {
    Object[] nodes = myUserListComponent.getSelectedNodes();
    String msgCode = "RenameCommand.rename";
    if (nodes.length == 1) {
      msgCode = nodes[0] instanceof User ? "RenameCommand.rename.user" : "RenameCommand.rename.group";
    }
    return StringUtil.getMsg(msgCode);
  }
}
