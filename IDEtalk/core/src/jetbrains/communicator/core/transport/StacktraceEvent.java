// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.transport;

import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.util.CommunicatorStrings;

/**
 * @author kir
 */
public class StacktraceEvent extends MessageEvent {

  private final String myStacktrace;

  StacktraceEvent(Transport transport, String remoteUser, String stacktrace, String comment) {
    super(transport, remoteUser, comment);
    myStacktrace = stacktrace;
  }

  public String getStacktrace() {
    return myStacktrace;
  }

  @Override
  public void accept(EventVisitor visitor) {
    visitor.visitStacktraceEvent(this);
  }

  public String toString() {
    return CommunicatorStrings.toString(getClass(), new Object[]{
      getMessage(),
      getRemoteUser(),
      getTransport()
    });
  }

}
