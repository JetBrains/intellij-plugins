// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.commands;

/**
 * @author Kir Maximov
 */
public interface UserCommand {
  boolean isEnabled();
  void execute();
}
