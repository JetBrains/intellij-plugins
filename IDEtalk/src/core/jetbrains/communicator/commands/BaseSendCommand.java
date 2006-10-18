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
    
    return (User[]) selectedUsers.toArray(new User[selectedUsers.size()]);
  }
}
