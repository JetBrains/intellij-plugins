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
package jetbrains.communicator.jabber;

import com.intellij.openapi.ui.Messages;
import jetbrains.communicator.util.CommunicatorStrings;

/**
 * @author Kir
 */
public class JabberConnectionCommand extends BaseJabberConnectionCommand {
  public JabberConnectionCommand(JabberFacade jabberFacade, JabberUI jabberUi) {
    super(jabberFacade, jabberUi);
  }

  @Override
  public String getName() {
    if (isConnected()) {
      return CommunicatorStrings.getMsg("JabberConnectionCommand.connected",
          myJabberFacade.getMyAccount().getJabberId()
          + '/' + JabberFacade.IDETALK_RESOURCE);
    }
    else {
      return CommunicatorStrings.getMsg("JabberConnectionCommand.disconnected");
    }
  }

  @Override
  public void execute() {
    if (isConnected()) {
      if (Messages.YES == Messages.showYesNoDialog(CommunicatorStrings.getMsg("disconnect.from.jabber.account"),
                                                   CommunicatorStrings.getMsg("disconnect.confirmation"), Messages.getQuestionIcon())) {
        myJabberFacade.disconnect();
        myJabberFacade.getMyAccount().setLoginAllowed(false);
        myJabberFacade.saveSettings();
      }
    }
    else {
      myJabberUi.login(myParentComponent);
    }
  }
}
