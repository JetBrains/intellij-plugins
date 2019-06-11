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
package jetbrains.communicator.core.transport;

import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.util.CommunicatorStrings;

/**
 * @author kir
 */
public class MessageEvent extends TransportEvent {

  private final String myComment;

  MessageEvent(Transport transport, String remoteUser, String comment) {
    super(transport, remoteUser);
    myComment = comment;
  }

  public String getMessage() {
    return myComment;
  }

  @Override
  public void accept(EventVisitor visitor) {
    visitor.visitMessageEvent(this);
  }

  public String toString() {
    return CommunicatorStrings.toString(getClass(), new Object[]{
      myComment,
      getRemoteUser(),
      getTransport()
    });
  }
}
