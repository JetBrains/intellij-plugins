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

import jetbrains.communicator.core.commands.UserCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.UserListComponent;
import jetbrains.communicator.util.CommunicatorStrings;

/**
 * @author Kir
 */
public class ViewFilesCommand implements UserCommand {

  private final IDEFacade myFacade;
  private final UserListComponent myUserListComponent;

  public ViewFilesCommand(UserListComponent userListComponent, IDEFacade facade) {
    myFacade = facade;
    myUserListComponent = userListComponent;
  }

  @Override
  public boolean isEnabled() {
    User selectedUser = myUserListComponent.getSelectedUser();
    return selectedUser != null && selectedUser.isOnline() && selectedUser.hasIDEtalkClient();
  }

  @Override
  public void execute() {
    User user = myUserListComponent.getSelectedUser();
    assert user != null;
    ProjectsData projectsData = user.getProjectsData(myFacade);
    if (projectsData.isEmpty()) {
      myFacade.showMessage(CommunicatorStrings.getMsg("ViewFilesCommand.no.information"),
                           CommunicatorStrings.getMsg("ViewFilesCommand.no.information.for.user", user.getDisplayName())
      );
    }
    else {
      myFacade.showUserFiles(user, projectsData);
    }
  }

}
