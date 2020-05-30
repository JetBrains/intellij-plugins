// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.ide;

import jetbrains.communicator.core.commands.NamedUserCommand;

import java.awt.*;

/**
 * @author Kir
 */
public interface StatusToolbar {
  /** Add a toolbar button. Class namedCommandClass should be a class for {@link NamedUserCommand} */
  void addToolbarCommand(Class<? extends NamedUserCommand> namedCommandClass);

  Component createComponent();
}
