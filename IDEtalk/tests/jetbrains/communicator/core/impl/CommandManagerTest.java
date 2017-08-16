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
package jetbrains.communicator.core.impl;

import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.commands.UserCommand;
import org.picocontainer.MutablePicoContainer;

/**
 * @author kir
 */
public class CommandManagerTest extends BaseTestCase {

  private CommandManagerImpl myCommandManager;
  private MutablePicoContainer myContainer;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myCommandManager = new CommandManagerImpl();
    myContainer = Pico.getInstance().makeChildContainer();
  }

  public void testGetCommand() {
    UserCommand command = myCommandManager.getCommand(MyTestCommand.class, myContainer);
    assertNotNull(command);
    assertSame(MyTestCommand.class, command.getClass());

    assertNotNull(myContainer.getComponentInstanceOfType(MyTestCommand.class));
  }

  public void testGetCommandTwice() {
    UserCommand command = myCommandManager.getCommand(MyTestCommand.class, myContainer);
    UserCommand command1 = myCommandManager.getCommand(MyTestCommand.class, myContainer);
    assertSame(command, command1);
  }


  public static class MyTestCommand implements UserCommand {

    @Override
    public boolean isEnabled() {
      return true;
    }

    @Override
    public void execute() {
    }
  }
}
