// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.jabber.impl;

import jetbrains.communicator.jabber.AccountInfo;

/**
 * @author Kir
 */
class JabberSettings {
  private final AccountInfo myAccount = new AccountInfo();
  private boolean mySkipJabberConnection;
  private boolean myWasOnceConnectedSuccessfully;

  public AccountInfo getAccount() {
    return myAccount;
  }
}
