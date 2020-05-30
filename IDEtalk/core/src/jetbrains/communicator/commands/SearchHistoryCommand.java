// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
