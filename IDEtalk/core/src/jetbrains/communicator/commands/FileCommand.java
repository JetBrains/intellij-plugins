// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.commands;

import jetbrains.communicator.core.commands.UserCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.VFile;

/**
 * @author Kir
 */
public interface FileCommand extends UserCommand {
  void setUser(User user);
  void setVFile(VFile vFile);
}
