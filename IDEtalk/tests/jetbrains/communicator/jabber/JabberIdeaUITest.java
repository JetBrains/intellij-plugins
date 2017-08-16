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

import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.ide.StatusToolbar;
import org.picocontainer.MutablePicoContainer;

import java.awt.*;

/**
 * @author Kir
 */
public class JabberIdeaUITest extends BaseTestCase implements StatusToolbar {
  private StringBuffer myLog;

  public void testInitialize() {

    myLog = new StringBuffer();

    JabberIdeaUI jabberIdeaUI = new JabberIdeaUI(null, null);

    MutablePicoContainer projectLevelContainer = Pico.getInstance().makeChildContainer();
    projectLevelContainer.registerComponentInstance(this); // register StatusToolbar

    jabberIdeaUI.initPerProject(projectLevelContainer);

    assertEquals("Should register the command", JabberConnectionCommand.class.getName(), myLog.toString());
  }

  @Override
  public void addToolbarCommand(Class<? extends NamedUserCommand> namedCommandClass) {
    myLog.append(namedCommandClass.getName());
  }

  @Override
  public Component createComponent() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }
}
