/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package jetbrains.communicator.commands;

import icons.IdeTalkCoreIcons;
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.StringUtil;

import javax.swing.*;

/**
 * @author Kir Maximov
 */
public class CreateGroupCommand implements NamedUserCommand {
  private final UserModel myUserModel;
  private final IDEFacade myIdeFacade;

  public CreateGroupCommand(UserModel userModel, IDEFacade ideFacade) {
    myUserModel = userModel;
    myIdeFacade = ideFacade;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void execute() {
    String groupName = myIdeFacade.getMessageLine(
        StringUtil.getMsg("CreateGroupCommand.input"), StringUtil.getMsg("CreateGroupCommand.input.title"));
    myUserModel.addGroup(groupName);
  }

  @Override
  public Icon getIcon() {
    return IdeTalkCoreIcons.Nodes.Group_close;
  }

  @Override
  public String getName() {
    return StringUtil.getMsg("CreateGroupCommand.text");
  }
}
