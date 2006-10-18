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

  public final boolean isEnabled() {
    return isFocused() && enabled();
  }

  public abstract boolean enabled();

  protected boolean isFocused() {
    Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
    return focusOwner == myUserListComponent.getComponent();
  }
}
