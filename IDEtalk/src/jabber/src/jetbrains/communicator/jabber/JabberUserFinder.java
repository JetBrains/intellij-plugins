// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.jabber;

import jetbrains.communicator.ide.TalkProgressIndicator;
import jetbrains.communicator.core.users.User;

/**
 * @author Kir
 */
public interface JabberUserFinder {
  User[] findUsers(TalkProgressIndicator progressIndicator);

  void registerForProject(String jabberUserId);
}
