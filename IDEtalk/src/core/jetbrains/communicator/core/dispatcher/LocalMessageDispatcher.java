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

import javax.swing.*;
import java.util.Date;

/**
 * @author Kir
 */
public interface LocalMessageDispatcher extends MessageDispatcher {

  void addPendingMessage(User user, LocalMessage message);

  int countPendingMessages();

  /** If there are active incoming messages, returns the icon specific for the first message.
   * It can used for some notification. */
  Icon getBlinkingIcon();

  /** Oldest messages go first */
  LocalMessage[] getHistory(User user, Date since);

  void clearHistory();
  boolean isHistoryEmpty();
}
