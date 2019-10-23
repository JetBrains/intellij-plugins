// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.dispatcher;

import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.IDEFacade;

/**
 * @author Kir
 */
public interface AsyncMessageDispatcher extends MessageDispatcher {

  void sendLater(User user, Message message);

  IDEFacade getIdeFacade();
}
