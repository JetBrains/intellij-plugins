// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.commands;

import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.CommunicatorStrings;

import javax.swing.*;

/**
 * @author Kir
 */
public class ShowDiffCommand implements FileCommand, NamedUserCommand {
  private final IDEFacade myFacade;
  private User myUser;
  private VFile myVFile;

  public ShowDiffCommand(IDEFacade ideFacade) {
    myFacade = ideFacade;
  }

  @Override
  public void setUser(User user) {
    myUser = user;
  }

  @Override
  public void setVFile(VFile vFile) {
    myVFile = vFile;
  }

  @Override
  public boolean isEnabled() {
    return myUser != null && myVFile != null && myFacade.hasFile(myVFile);
  }

  @Override
  public void execute() {
    String content = myUser.getVFile(myVFile, myFacade);
    if (content != null) {
      myFacade.showDiffFor(myUser, myVFile, content);
    }
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getName() {
    if (myVFile != null && myUser != null) {
      return CommunicatorStrings.getMsg("show.diff.for.file.with.user", myUser.getDisplayName());
    }
    return CommunicatorStrings.getMsg("show.diff.with.user");
  }
}
