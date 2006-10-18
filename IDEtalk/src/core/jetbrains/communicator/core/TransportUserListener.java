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

  public final void beforeChange(IDEtalkEvent event) {
    super.beforeChange(event);
    event.accept(new EventVisitor() {
      public void visitUserEvent(UserEvent event) {
        super.visitUserEvent(event);
        if (event.getUser().getTransportCode().equals(myTransport.getName())) {
          processBeforeChange(event);
        }
      }
    });
  }

  public final void afterChange(IDEtalkEvent event) {
    event.accept(new EventVisitor() {
      public void visitUserEvent(UserEvent event) {
        super.visitUserEvent(event);
        if (event.getUser().getTransportCode().equals(myTransport.getName())) {
          processAfterChange(event);
        }
      }
    });
  }

  protected void processBeforeChange(UserEvent event){}
  protected abstract void processAfterChange(UserEvent event);

}
