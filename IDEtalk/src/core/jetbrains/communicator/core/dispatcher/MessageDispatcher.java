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
