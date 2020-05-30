// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl.dispatcher;

import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.users.User;

import java.util.List;
import java.util.Map;

/**
 * @author Kir
 */
public class MessagesStorable {
  private final Map<User, List<Message>> myUser2Messages;

  public MessagesStorable(Map<User,List<Message>> user2Messages) {
    myUser2Messages = user2Messages;
  }

  public Map<User,List<Message>> getUser2Messages() {
    return myUser2Messages;
  }
}
