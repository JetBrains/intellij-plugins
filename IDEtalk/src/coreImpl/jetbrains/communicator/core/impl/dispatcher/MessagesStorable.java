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
