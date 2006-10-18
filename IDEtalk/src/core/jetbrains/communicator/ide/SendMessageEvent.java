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
import jetbrains.communicator.core.users.User;
import org.jetbrains.annotations.NonNls;

/**
 * @author Kir
 */
public class SendMessageEvent extends OwnMessageEvent {

  public SendMessageEvent(@NonNls String message, User user) {
    super(message, user);
  }

  public void accept(EventVisitor visitor) {
    visitor.visitSendMessageEvent(this);
  }

}
