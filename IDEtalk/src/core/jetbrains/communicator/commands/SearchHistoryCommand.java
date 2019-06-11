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

import jetbrains.communicator.core.commands.UserCommand;
import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.CommunicatorStrings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kir
 */
public class SearchHistoryCommand implements UserCommand {
  private final LocalMessageDispatcher myMessageDispatcher;
  private final IDEFacade myIdeFacade;
  private User myUser;

  public SearchHistoryCommand(LocalMessageDispatcher messageDispatcher, IDEFacade ideFacade) {
    myMessageDispatcher = messageDispatcher;
    myIdeFacade = ideFacade;
  }

  @Override
  public void execute() {
    String searchString = myIdeFacade.getMessage(
      CommunicatorStrings.getMsg("SearchHistoryCommand.enter.query.string"),
      CommunicatorStrings.getMsg("SearchHistoryCommand.search.history", myUser.getDisplayName()),
      CommunicatorStrings.getMsg("search"));

    if (searchString != null) {
      LocalMessage[] localMessages = myMessageDispatcher.getHistory(myUser, null);
      List<LocalMessage> result = new ArrayList<>();
      for (LocalMessage message : localMessages) {
        if (message.containsString(searchString)) {
          result.add(message);
        }
      }

      if (result.size() == 0) {
        myIdeFacade.showMessage(CommunicatorStrings.getMsg("SearchHistoryCommand.search.history", myUser.getDisplayName()),
                                CommunicatorStrings.getMsg("SearchHistoryCommand.no.results"));
      }
      else {
        myIdeFacade.showSearchHistoryResults(result, myUser);
      }
    }
  }

  @Override
  public boolean isEnabled() {
    return myUser != null && !myMessageDispatcher.isHistoryEmpty();
  }

  public void setUser(User user) {
    myUser = user;
  }
}
