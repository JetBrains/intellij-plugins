// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.commands;

import javax.swing.*;

/**
 * @author Kir
 *
 * Named commands are used in actions.
 */
public interface NamedUserCommand extends UserCommand {
  String getName();
  Icon getIcon();
}
