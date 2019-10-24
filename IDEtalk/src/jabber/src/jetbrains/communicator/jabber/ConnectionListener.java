// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.jabber;

import org.jivesoftware.smack.XMPPConnection;

/**
 * @author Kir
 */
public interface ConnectionListener {
  void connected(XMPPConnection connection);
  void authenticated();

  void disconnected(boolean onError);
}
