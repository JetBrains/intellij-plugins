// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.commands;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.UserListComponent;


/**
 * @author Kir
 */
public class SendMessageCommand  extends BaseSendCommand implements SendMessageInvoker {
  private String myMessage = "";

  public SendMessageCommand(UserModel userModel, UserListComponent userListComponent, IDEFacade facade) {
    super(userModel, userListComponent, facade);
  }

  public void setMessage(String text) {
    myMessage = text;
  }

  @Override
  public void execute() {
    myFacade.invokeSendMessage(myUserModel.getAllUsers(), getDefaultTargetUsers(), myMessage, this);
  }

  @Override
  public void doSendMessage(final User[] targetUsers, final String message) {
    if (StringUtil.isEmptyOrSpaces(message) || targetUsers.length == 0) return;

    for (User targetUser : targetUsers) {
      targetUser.sendMessage(message, myEventBroadcaster);
    }
  }
}
