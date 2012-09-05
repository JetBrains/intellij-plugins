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

import icons.IdetalkCoreIcons;
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.CanceledException;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.ProgressIndicator;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.UIUtil;

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

  public String getName() {
    return StringUtil.getMsg("FindUsersCommand.name");
  }

  public Icon getIcon() {
    return IdetalkCoreIcons.IdeTalk.User;
  }

  public boolean isEnabled() {
    return true;
  }

  public void execute() {
    final List<User>[] finalUsers = new List[1];

    try {
      myIdeFacade.runLongProcess(StringUtil.getMsg("FindUsersCommand.dialog.title"), new IDEFacade.Process() {
        public void run(ProgressIndicator indicator) {
          List<User> result = new ArrayList<User>();
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
    myIdeFacade.showMessage(StringUtil.getMsg("FindUsersCommand.notFound.title"),
        StringUtil.getMsg("FindUsersCommand.notFound.text"));
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
