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
package jetbrains.communicator.idea.messagesWindow;

import jetbrains.communicator.core.users.User;
import jetbrains.communicator.idea.ConsoleMessage;
import jetbrains.communicator.idea.ConsoleUtil;
import jetbrains.communicator.util.StringUtil;

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

  public User getUser() {
    return myUser;
  }

  public Date getWhen() {
    return myDate;
  }

  public String getTitle() {
    return buildHeader();
  }

  public String getUsername() {
    return StringUtil.getMyUsername();
  }

  private String buildHeader() {
    return StringUtil.getMsg("console.selfline", ConsoleUtil.formatDate(myDate));
  }
}
