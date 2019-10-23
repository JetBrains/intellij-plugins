// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.dispatcher;

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.users.User;

/**
 * Provides a queue of {@link Message}s per {@link User}. Subclasses provide methods for sending
 * messages.
 * @author Kir
 */
public interface MessageDispatcher {

  boolean hasUsersWithMessages();
  User[] getUsersWithMessages();
  Message[] getPendingMessages(User user);

  EventBroadcaster getBroadcaster();

  /** Should return true if the message was successfully sent. */
  boolean sendNow(User user, Message message);
}
