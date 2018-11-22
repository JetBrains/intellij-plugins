/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package jetbrains.communicator.jabber;

import icons.IdeTalkCoreIcons;
import jetbrains.communicator.core.commands.NamedUserCommand;

import javax.swing.*;
import java.awt.*;

/**
 * @author Kir
 */
public abstract class BaseJabberConnectionCommand implements NamedUserCommand {
  protected final JabberFacade myJabberFacade;
  protected final JabberUI myJabberUi;
  protected Component myParentComponent;

  public BaseJabberConnectionCommand(JabberFacade jabberFacade, JabberUI jabberUi) {
    myJabberFacade = jabberFacade;
    myJabberUi = jabberUi;
  }

  public void setParentComponent(Component parentComponent) {
    myParentComponent = parentComponent;
  }

  @Override
  public Icon getIcon() {
    if (isConnected())
      return IdeTalkCoreIcons.IdeTalk.Jabber;
    return IdeTalkCoreIcons.IdeTalk.Offline;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  protected boolean isConnected() {
    return myJabberFacade.isConnectedAndAuthenticated();
  }
}
