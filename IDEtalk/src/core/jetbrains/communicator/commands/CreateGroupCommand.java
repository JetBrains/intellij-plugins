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
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.UIUtil;

import javax.swing.*;

/**
 * @author Kir Maximov
 */
public class CreateGroupCommand implements NamedUserCommand {
  private UserModel myUserModel;
  private IDEFacade myIdeFacade;

  public CreateGroupCommand(UserModel userModel, IDEFacade ideFacade) {
    myUserModel = userModel;
    myIdeFacade = ideFacade;
  }

  public boolean isEnabled() {
    return true;
  }

  public void execute() {
    String groupName = myIdeFacade.getMessageLine(
        StringUtil.getMsg("CreateGroupCommand.input"), StringUtil.getMsg("CreateGroupCommand.input.title"));
    myUserModel.addGroup(groupName);
  }

  public Icon getIcon() {
    return UIUtil.getIcon("/nodes/group_open.png");
  }

  public String getName() {
    return StringUtil.getMsg("CreateGroupCommand.text");
  }
}
