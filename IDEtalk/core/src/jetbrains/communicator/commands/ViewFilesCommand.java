// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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
