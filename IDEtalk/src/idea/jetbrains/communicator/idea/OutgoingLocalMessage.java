// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.idea.messagesWindow.OwnConsoleMessage;
import jetbrains.communicator.util.CommunicatorStrings;

/**
 * @author Kir
 */
public class OutgoingLocalMessage extends BaseOutgoingLocalMessage {

  public OutgoingLocalMessage(String messageText) {
    super(messageText);
  }

  @Override
  public ConsoleMessage createConsoleMessage(User user) {
    return new OwnConsoleMessage(user, CommunicatorStrings.getMsg("message"), getWhen()) {
      @Override
      public void printMessage(Project project, ConsoleView console) {
        printComment(console);
      }
    };
  }
}
