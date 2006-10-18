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
