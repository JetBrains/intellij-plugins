// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.SendCodePointerEvent;
import jetbrains.communicator.idea.codePointer.IncomingCodePointerMessage;
import jetbrains.communicator.idea.messagesWindow.OwnConsoleMessage;
import jetbrains.communicator.util.CommunicatorStrings;

/**
 * @author Kir
 */
public class OutgoingCodePointerLocalMessage extends BaseOutgoingLocalMessage {
  private final SendCodePointerEvent myEvent;

  public OutgoingCodePointerLocalMessage(SendCodePointerEvent event) {
    super(event.getMessage());
    myEvent = event;
  }

  @Override
  public boolean containsString(String searchString) {
    return super.containsString(searchString) || myEvent.getFile().containsSearchString(searchString);
  }

  @Override
  public ConsoleMessage createConsoleMessage(User user) {
    return new OwnConsoleMessage(user, CommunicatorStrings.getMsg("code.pointer"), getWhen()) {
      @Override
      public void printMessage(Project project, ConsoleView console) {
        final IDEAFacade ideFacade =
            (IDEAFacade) Pico.getInstance().getComponentInstanceOfType(IDEFacade.class);

        IncomingCodePointerMessage printer = new IncomingCodePointerMessage(myEvent, ideFacade);
        printer.outputMessage(console);
      }
    };
  }
}
