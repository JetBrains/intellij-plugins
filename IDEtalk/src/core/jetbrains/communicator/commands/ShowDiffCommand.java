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
