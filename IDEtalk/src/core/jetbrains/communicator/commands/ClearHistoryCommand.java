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
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.ide.CanceledException;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.UIUtil;

/**
 * @author Kir
 */
public class ClearHistoryCommand implements UserCommand {
  private LocalMessageDispatcher myMessageDispatcher;
  private IDEFacade myIdeFacade;

  public ClearHistoryCommand(LocalMessageDispatcher messageDispatcher, IDEFacade ideFacade) {
    myMessageDispatcher = messageDispatcher;
    myIdeFacade = ideFacade;
  }

  public void execute() {
    if (myIdeFacade.askQuestion(
        StringUtil.getMsg("ClearHistoryCommand.title"),
        StringUtil.getMsg("ClearHistoryCommand.text"))) {
      try {
        UIUtil.run(myIdeFacade, StringUtil.getMsg("ClearHistoryCommand.title"), new Runnable() {
          public void run() {
            myMessageDispatcher.clearHistory();
          }
        });
      } catch (CanceledException e) {
        // ignore
      }
    }
  }

  public boolean isEnabled() {
    return !myMessageDispatcher.isHistoryEmpty();
  }
}
