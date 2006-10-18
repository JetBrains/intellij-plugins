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
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.StringUtil;

import javax.swing.*;

/**
 * @author Kir
 */
public class SendCodePointerCommand implements FileCommand, NamedUserCommand {
  private final IDEFacade myIdeFacade;
  private final EventBroadcaster myEventBroadcaster;

  private CodePointer myCodePointer;
  private VFile myVFile;
  private User myUser;

  public SendCodePointerCommand(IDEFacade ideFacade, EventBroadcaster eventBroadcaster) {
    myIdeFacade = ideFacade;
    myEventBroadcaster = eventBroadcaster;
  }

  public boolean isEnabled() {
    return myCodePointer != null && myVFile != null && myUser != null;
  }

  public void execute() {
    assert isEnabled();
    final String message = myIdeFacade.getMessage(StringUtil.getMsg("code_pointer.message.input"),
               StringUtil.getMsg("code_pointer.message.input.title"), StringUtil.getMsg("send"));
    if (message != null) {
      myIdeFacade.fillFileContents(myVFile);
      myUser.sendCodeIntervalPointer(myVFile, myCodePointer, message, myEventBroadcaster);
    }
  }

  public void setCodePointer(CodePointer codePointer) {
    myCodePointer = codePointer;
  }

  public void setVFile(VFile vFile) {
    myVFile = vFile;
  }

  public void setUser(User user) {
    myUser = user;
  }

  public Icon getIcon() {
    return null;
  }

  public String getName() {
    if (myVFile != null && myUser != null) {
      return StringUtil.getMsg("send.code.pointer.to", myUser.getDisplayName());
    }
    return StringUtil.getMsg("send.code.pointer");
  }
}
