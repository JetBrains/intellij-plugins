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
package jetbrains.communicator.jabber.impl;

import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.jabber.JabberFacade;
import jetbrains.communicator.jabber.JabberUI;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.UIUtil;

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

  public String getName() {
    return StringUtil.getMsg("jabber.findByIdCommandName");
  }

  public Icon getIcon() {
    return UIUtil.getIcon("/ideTalk/jabber.png");
  }

  public boolean isEnabled() {
    return true;
  }

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
