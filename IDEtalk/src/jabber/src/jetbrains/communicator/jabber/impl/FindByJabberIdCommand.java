// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.jabber.impl;

import icons.IdeTalkCoreIcons;
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.jabber.JabberFacade;
import jetbrains.communicator.jabber.JabberUI;
import jetbrains.communicator.util.CommunicatorStrings;

import javax.swing.*;
import java.util.Arrays;

/**
 * @author Kir
 */
public class FindByJabberIdCommand implements NamedUserCommand {
  private final JabberUI myJabberUI;
  private final JabberFacade myJabberFacade;
  private final UserModel myUserModel;

  public FindByJabberIdCommand(JabberFacade jabberFacade, JabberUI jabberUI, UserModel userModel) {
    myJabberUI = jabberUI;
    myJabberFacade = jabberFacade;
    myUserModel = userModel;
  }

  @Override
  public String getName() {
    return CommunicatorStrings.getMsg("jabber.findByIdCommandName");
  }

  @Override
  public Icon getIcon() {
    return IdeTalkCoreIcons.IdeTalk.Jabber;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void execute() {
    if (myJabberUI.connectAndLogin(null)) {
      String findByIdData = myJabberUI.getFindByIdData(Arrays.asList(myUserModel.getGroups()));
      if (findByIdData == null) return;

      int separator = findByIdData.indexOf(':');
      String group = findByIdData.substring(0, separator);
      String jabberIDs = findByIdData.substring(separator + 1);

      String []ids = jabberIDs.split("[ \r\n,]+");
      if (ids.length > 0 && ids[0].trim().length() > 0) {
        myJabberFacade.addUsers(group, Arrays.asList(ids));
      }
    }
  }
}
