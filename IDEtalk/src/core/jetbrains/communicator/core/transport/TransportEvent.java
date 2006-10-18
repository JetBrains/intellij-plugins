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
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;

import java.util.Date;

/**
 * @author kir
 */
public abstract class TransportEvent implements IDEtalkEvent {
  private final Transport myTransport;
  private final String myRemoteUser;
  private long myDate;

  protected TransportEvent(Transport transport, String remoteUser) {
    myTransport = transport;
    myRemoteUser = remoteUser;
    myDate = System.currentTimeMillis();
  }

  public Transport getTransport() {
    return myTransport;
  }

  public String getRemoteUser() {
    return myRemoteUser;
  }

  public void setWhen(long when) {
    myDate = when;
  }

  public Date getWhen() {
    return new Date(myDate);
  }

  public User createUser(UserModel userModel) {
    return userModel.createUser(getRemoteUser(), getTransport().getName());
  }

  public void accept(EventVisitor visitor) {
    visitor.visitTransportEvent(this);
  }
}
