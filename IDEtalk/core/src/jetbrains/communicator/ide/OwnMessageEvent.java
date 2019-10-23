// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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

  @Override
  public void accept(EventVisitor visitor) {
    visitor.visitOwnMessageEvent(this);
  }
}
