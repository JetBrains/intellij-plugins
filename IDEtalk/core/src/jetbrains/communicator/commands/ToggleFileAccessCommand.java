// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.commands;

import jetbrains.communicator.OptionFlag;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserAction;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.UserListComponent;
import jetbrains.communicator.util.CommunicatorStrings;

/**
 * @author Kir
 */
public class ToggleFileAccessCommand extends EnabledWhenFocusedCommand {
  private final UserModel myUserModel;

  public ToggleFileAccessCommand(UserModel userModel, UserListComponent userListComponent) {
    super(userListComponent);
    myUserModel = userModel;
  }

  @Override
  public boolean enabled() {
    Object[] nodes = myUserListComponent.getSelectedNodes();
    final Boolean[] val = new Boolean[1];

    return nodes.length > 0 && doForSelected(new UserAction() {
      @Override
      public boolean executeAndContinue(User user) {
        if (val[0] == null) {
          val[0] = user.canAccessMyFiles();
        }
        else {
          if (val[0] != user.canAccessMyFiles()) {
            return false;
          }
        }
        return true;
      }
    });
  }

  @Override
  public void execute() {
    doForSelected(new UserAction() {
      @Override
      public boolean executeAndContinue(User user) {
        toggleAccessToFiles(user);
        return true;
      }
    });
  }

  private void toggleAccessToFiles(User user) {
    user.setCanAccessMyFiles(!user.canAccessMyFiles(), myUserModel);
  }

  public boolean isSelected() {
    final Boolean[] val = new Boolean[1];

    doForSelected(new UserAction() {
      @Override
      public boolean executeAndContinue(User user) {
        val[0] = user.canAccessMyFiles();
        return false;
      }
    });

    return val[0] != null && val[0];
  }

  public String getText() {
    Object[] nodes = myUserListComponent.getSelectedNodes();

    if (nodes.length == 0) {
      return CommunicatorStrings.getMsg("ToggleFileAccess.text.single");
    }

    return nodes.length > 1 || !(nodes[0] instanceof User) ?
           CommunicatorStrings.getMsg("ToggleFileAccess.text.multiple") :
           CommunicatorStrings.getMsg("ToggleFileAccess.text.single");
  }

  private boolean doForSelected(UserAction action) {
    Object[] nodes = myUserListComponent.getSelectedNodes();
    return myUserModel.forEach(nodes, action, OptionFlag.OPTION_HIDE_OFFLINE_USERS.isSet() );
  }
}
