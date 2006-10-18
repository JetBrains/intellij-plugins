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
package jetbrains.communicator.idea;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.SendCodePointerEvent;
import jetbrains.communicator.idea.codePointer.IncomingCodePointerMessage;
import jetbrains.communicator.idea.messagesWindow.OwnConsoleMessage;
import jetbrains.communicator.util.StringUtil;

/**
 * @author Kir
 */
public class OutgoingCodePointerLocalMessage extends BaseOutgoingLocalMessage {
  private final SendCodePointerEvent myEvent;

  public OutgoingCodePointerLocalMessage(SendCodePointerEvent event) {
    super(event.getMessage());
    myEvent = event;
  }

  public boolean containsString(String searchString) {
    return super.containsString(searchString) || myEvent.getFile().containsSearchString(searchString);
  }

  public ConsoleMessage createConsoleMessage(User user) {
    return new OwnConsoleMessage(user, StringUtil.getMsg("code.pointer"), getWhen()) {
      public void printMessage(Project project, ConsoleView console) {
        final IDEAFacade ideFacade =
            (IDEAFacade) Pico.getInstance().getComponentInstanceOfType(IDEFacade.class);

        IncomingCodePointerMessage printer = new IncomingCodePointerMessage(myEvent, ideFacade);
        printer.outputMessage(console);
      }
    };
  }
}
