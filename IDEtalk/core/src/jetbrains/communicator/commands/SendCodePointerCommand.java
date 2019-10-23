// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.commands;

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.CommunicatorStrings;

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

  @Override
  public boolean isEnabled() {
    return myCodePointer != null && myVFile != null && myUser != null;
  }

  @Override
  public void execute() {
    assert isEnabled();
    final String message = myIdeFacade.getMessage(CommunicatorStrings.getMsg("code_pointer.message.input"),
                                                  CommunicatorStrings.getMsg("code_pointer.message.input.title"), CommunicatorStrings
                                                    .getMsg("send"));
    if (message != null) {
      myIdeFacade.fillFileContents(myVFile);
      myUser.sendCodeIntervalPointer(myVFile, myCodePointer, message, myEventBroadcaster);
    }
  }

  public void setCodePointer(CodePointer codePointer) {
    myCodePointer = codePointer;
  }

  @Override
  public void setVFile(VFile vFile) {
    myVFile = vFile;
  }

  @Override
  public void setUser(User user) {
    myUser = user;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getName() {
    if (myVFile != null && myUser != null) {
      return CommunicatorStrings.getMsg("send.code.pointer.to", myUser.getDisplayName());
    }
    return CommunicatorStrings.getMsg("send.code.pointer");
  }
}
