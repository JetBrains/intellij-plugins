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

import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.UserListComponent;
import jetbrains.communicator.util.StringUtil;


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

  public void execute() {
    myFacade.invokeSendMessage(myUserModel.getAllUsers(), getDefaultTargetUsers(), myMessage, this);
  }

  public void doSendMessage(final User[] targetUsers, final String message) {
    if (StringUtil.isEmpty(message) || targetUsers.length == 0) return;

    for (User targetUser : targetUsers) {
      targetUser.sendMessage(message, myEventBroadcaster);
    }
  }
}
