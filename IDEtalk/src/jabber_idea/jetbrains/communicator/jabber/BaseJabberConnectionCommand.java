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

import icons.IdetalkCoreIcons;
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.util.UIUtil;

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

  public Icon getIcon() {
    if (isConnected())
      return IdetalkCoreIcons.IdeTalk.Jabber;
    return IdetalkCoreIcons.IdeTalk.Offline;
  }

  public boolean isEnabled() {
    return true;
  }

  protected boolean isConnected() {
    return myJabberFacade.isConnectedAndAuthenticated();
  }
}
