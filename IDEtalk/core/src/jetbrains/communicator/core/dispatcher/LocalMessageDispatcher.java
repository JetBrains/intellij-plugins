// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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
