// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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

  @Override
  public void accept(EventVisitor visitor) {
    visitor.visitSendMessageEvent(this);
  }

}
