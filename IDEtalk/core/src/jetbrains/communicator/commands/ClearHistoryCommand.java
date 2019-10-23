// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.commands;

import jetbrains.communicator.core.commands.UserCommand;
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.ide.CanceledException;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.CommunicatorStrings;
import jetbrains.communicator.util.UIUtil;

/**
 * @author Kir
 */
public class ClearHistoryCommand implements UserCommand {
  private final LocalMessageDispatcher myMessageDispatcher;
  private final IDEFacade myIdeFacade;

  public ClearHistoryCommand(LocalMessageDispatcher messageDispatcher, IDEFacade ideFacade) {
    myMessageDispatcher = messageDispatcher;
    myIdeFacade = ideFacade;
  }

  @Override
  public void execute() {
    if (myIdeFacade.askQuestion(
      CommunicatorStrings.getMsg("ClearHistoryCommand.title"),
      CommunicatorStrings.getMsg("ClearHistoryCommand.text"))) {
      try {
        UIUtil.run(myIdeFacade, CommunicatorStrings.getMsg("ClearHistoryCommand.title"), () -> myMessageDispatcher.clearHistory());
      } catch (CanceledException e) {
        // ignore
      }
    }
  }

  @Override
  public boolean isEnabled() {
    return !myMessageDispatcher.isHistoryEmpty();
  }
}
