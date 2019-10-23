// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

  @Override
  public void accept(EventVisitor visitor) {
    visitor.visitTransportEvent(this);
  }
}
