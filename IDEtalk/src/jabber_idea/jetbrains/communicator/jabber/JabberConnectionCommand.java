// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.jabber;

import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import jetbrains.communicator.util.CommunicatorStrings;

/**
 * @author Kir
 */
public final class JabberConnectionCommand extends BaseJabberConnectionCommand {
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
      if (Messages.YES == MessageDialogBuilder.yesNo(CommunicatorStrings.getMsg("disconnect.confirmation"), CommunicatorStrings.getMsg("disconnect.from.jabber.account")).show()) {
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
