// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.dispatcher;

import jetbrains.communicator.core.users.User;

/**
 *
 * This interface is used both for outgoing messages (network async messages)
 * and for the queue of incoming messages. <p>
 * A message can deliver itself.
 * @author Kir
 */
public interface Message {
  /**
   * @param user A User, associated with this message.
   * @return true if sending was successful */
  boolean send(User user);
}
