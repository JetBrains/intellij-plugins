// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.commands;

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.commands.UserCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.UserListComponent;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Kir
 */
abstract class BaseSendCommand  implements UserCommand {
  protected final IDEFacade myFacade;
  protected final UserModel myUserModel;
  protected final EventBroadcaster myEventBroadcaster;

  private final UserListComponent myUserListComponent;

  private User myDefaultTargetUser;

  BaseSendCommand(UserModel userModel, UserListComponent userListComponent, IDEFacade facade) {
    myUserModel = userModel;
    myFacade = facade;
    myUserListComponent = userListComponent;
    myEventBroadcaster = myUserModel.getBroadcaster();
  }

  @Override
  public final boolean isEnabled() {
    return getDefaultTargetUsers().length == 1;
  }

  public void setUser(User user) {
    myDefaultTargetUser = user;
  }

  protected User[] getDefaultTargetUsers() {
    Object[] selectedNodes = myUserListComponent.getSelectedNodes();
    List selectedUsers = new ArrayList();
    for (Object selectedNode : selectedNodes) {
      if (selectedNode instanceof User) {
        selectedUsers.add(selectedNode);
      }
    }

    if (myDefaultTargetUser != null) {
      selectedUsers.add(myDefaultTargetUser);
    }

    return (User[]) selectedUsers.toArray(new User[0]);
  }
}
