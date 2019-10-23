// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
