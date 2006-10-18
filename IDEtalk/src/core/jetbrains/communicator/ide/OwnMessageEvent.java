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

package jetbrains.communicator.ide;

import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.users.User;

/**
 * @author Kir
 */
public abstract class OwnMessageEvent implements IDEtalkEvent {

  private final String myMessage;
  private final User myTargetUser;

  public OwnMessageEvent(String message, User user) {
    assert user != null;
    myMessage = message;
    myTargetUser = user;
  }

  public String getMessage() {
    return myMessage;
  }

  public User getTargetUser() {
    return myTargetUser;
  }

  public void accept(EventVisitor visitor) {
    visitor.visitOwnMessageEvent(this);
  }
}
