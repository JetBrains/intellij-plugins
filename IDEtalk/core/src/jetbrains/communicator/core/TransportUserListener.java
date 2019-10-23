// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core;

import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.users.UserEvent;

/**
 * @author Kir
 */
public abstract class TransportUserListener extends IDEtalkAdapter {
  protected final Transport myTransport;

  public TransportUserListener(Transport transport) {
    myTransport = transport;
  }

  @Override
  public final void beforeChange(IDEtalkEvent event) {
    super.beforeChange(event);
    event.accept(new EventVisitor() {
      @Override
      public void visitUserEvent(UserEvent event) {
        super.visitUserEvent(event);
        if (event.getUser().getTransportCode().equals(myTransport.getName())) {
          processBeforeChange(event);
        }
      }
    });
  }

  @Override
  public final void afterChange(IDEtalkEvent event) {
    event.accept(new EventVisitor() {
      @Override
      public void visitUserEvent(UserEvent event) {
        super.visitUserEvent(event);
        if (event.getUser().getTransportCode().equals(myTransport.getName())) {
          processAfterChange(event);
        }
      }
    });
  }

  protected void processBeforeChange(UserEvent event) {
  }

  protected abstract void processAfterChange(UserEvent event);
}
