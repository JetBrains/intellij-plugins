// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.commands;

import icons.IdeTalkCoreIcons;
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.CanceledException;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.TalkProgressIndicator;
import jetbrains.communicator.util.CommunicatorStrings;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Kir
 */
public class FindUsersCommand implements NamedUserCommand {

  private final UserModel myUserModel;
  private final Transport[] myTransports;
  private final IDEFacade myIdeFacade;

  public FindUsersCommand(UserModel userModel, Transport[] transports, IDEFacade ideFacade) {
    myUserModel = userModel;
    myTransports = transports;
    myIdeFacade = ideFacade;
  }

  @Override
  public String getName() {
    return CommunicatorStrings.getMsg("FindUsersCommand.name");
  }

  @Override
  public Icon getIcon() {
    return IdeTalkCoreIcons.IdeTalk.User;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void execute() {
    final List<User>[] finalUsers = new List[1];

    try {
      myIdeFacade.runLongProcess(CommunicatorStrings.getMsg("FindUsersCommand.dialog.title"), new IDEFacade.Process() {
        @Override
        public void run(TalkProgressIndicator indicator) {
          List<User> result = new ArrayList<>();
          for (Transport transport : myTransports) {
            result.addAll(Arrays.asList(transport.findUsers(indicator)));
          }

          finalUsers[0] = result;
        }
      });
    } catch (CanceledException e) {
      return;
    }
    List<User> users = finalUsers[0];
    if (users == null) return;

    for (Iterator<User> it = users.iterator(); it.hasNext();) {
      User user = it.next();
      if ( user.isSelf() || myUserModel.hasUser(user)) {
        it.remove();
      }
    }


    if (users.size() < 1) {
      showNoUsersFoundMessage();
      return;
    }

    UsersInfo usersInfo = myIdeFacade.chooseUsersToBeAdded(users, myUserModel.getGroups());
    User[] toBeAdded = usersInfo.getUsers();
    for (User user : toBeAdded) {
      String group = usersInfo.getGroup();
      if (UserModel.AUTO_GROUP.equals(group)) {
        String[] userProjects = user.getProjects();
        group = userProjects.length > 0 ? userProjects[0] : UserModel.DEFAULT_GROUP;
      }
      user.setGroup(group, myUserModel);
      myUserModel.addUser(user);
    }

  }

  private void showNoUsersFoundMessage() {
    myIdeFacade.showMessage(CommunicatorStrings.getMsg("FindUsersCommand.notFound.title"),
                            CommunicatorStrings.getMsg("FindUsersCommand.notFound.text"));
  }

  public static class UsersInfo {
    private final User[] myUsers;
    private final String myGroup;

    public UsersInfo() {
      this(new User[0], "");
    }

    public UsersInfo(User[] users, String group) {
      myUsers = users;
      myGroup = group;
    }

    public User[] getUsers() {
      return myUsers;
    }

    public String getGroup() {
      return myGroup;
    }
  }
}
