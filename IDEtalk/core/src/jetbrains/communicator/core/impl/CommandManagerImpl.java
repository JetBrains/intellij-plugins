// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl;

import jetbrains.communicator.core.commands.CommandManager;
import jetbrains.communicator.core.commands.UserCommand;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Kir
 */
public class CommandManagerImpl implements CommandManager {

  @Override
  public <T extends UserCommand> T getCommand(Class<T> commandClass, MutablePicoContainer registerIn) {
    T result = (T) registerIn.getComponentInstanceOfType(commandClass);
    if (result == null) {
      registerIn.registerComponentImplementation(commandClass);
      result = (T) registerIn.getComponentInstanceOfType(commandClass);
    }
    return result;
  }
}
