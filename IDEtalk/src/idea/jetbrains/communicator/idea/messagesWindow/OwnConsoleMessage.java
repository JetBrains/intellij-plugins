// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.messagesWindow;

import jetbrains.communicator.core.users.User;
import jetbrains.communicator.idea.ConsoleMessage;
import jetbrains.communicator.idea.ConsoleUtil;
import jetbrains.communicator.util.CommunicatorStrings;

import java.util.Date;

/**
 * @author Kir
 */
public abstract class OwnConsoleMessage implements ConsoleMessage {
  private final User myUser;
  private final String myShortName;
  private final Date myDate;

  public OwnConsoleMessage(User user, String shortName, Date date) {
    myUser = user;
    myShortName = shortName;
    myDate = date;
  }

  @Override
  public User getUser() {
    return myUser;
  }

  @Override
  public Date getWhen() {
    return myDate;
  }

  @Override
  public String getTitle() {
    return buildHeader();
  }

  @Override
  public String getUsername() {
    return CommunicatorStrings.getMyUsername();
  }

  private String buildHeader() {
    return CommunicatorStrings.getMsg("console.selfline", ConsoleUtil.formatDate(myDate));
  }
}
