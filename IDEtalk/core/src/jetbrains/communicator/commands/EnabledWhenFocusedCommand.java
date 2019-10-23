// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.commands;

import jetbrains.communicator.core.commands.UserCommand;
import jetbrains.communicator.ide.UserListComponent;

import java.awt.*;

/**
 * @author Kir
 */
abstract class EnabledWhenFocusedCommand implements UserCommand {
  protected final UserListComponent myUserListComponent;

  protected EnabledWhenFocusedCommand(UserListComponent userListComponent) {
    myUserListComponent = userListComponent;
  }

  @Override
  public final boolean isEnabled() {
    return isFocused() && enabled();
  }

  public abstract boolean enabled();

  protected boolean isFocused() {
    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
    return focusOwner == myUserListComponent.getComponent();
  }
}
