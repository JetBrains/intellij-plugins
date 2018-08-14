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
package jetbrains.communicator.jabber.register;

import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.jabber.AccountInfo;
import jetbrains.communicator.jabber.ConnectionListener;
import jetbrains.communicator.jabber.JabberFacade;
import jetbrains.communicator.jabber.VCardInfo;
import org.jivesoftware.smack.XMPPConnection;

import java.util.List;

/**
 * @author Kir
 */
public class MockJabberFacade implements JabberFacade {
  private final AccountInfo myAccountInfo = new AccountInfo();
  private boolean myConnected;
  private boolean mySkipConnection;
  private String myLog = "";
  public static final String ERROR_LINE = "connection error";

  @Override
  public String[] getServers() {
    return new String[] {"foo.bar", "mu.no", "jabber.ru", "jabber.org"};
  }

  @Override
  public XMPPConnection getConnection() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void disconnect() {
  }

  @Override
  public VCardInfo getVCard(String userId) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void changeSubscription(String from, boolean subscribe) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void setOnlinePresence(UserPresence userPresence) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public AccountInfo getMyAccount() {
    return myAccountInfo;
  }

  @Override
  public String connect() {
    myLog += "connect";
    return myConnected ? null : ERROR_LINE;
  }

  @Override
  public String connect(String username, String password, String server, int port, boolean forceOldSSL) {
    myLog += username + ":" + password + "@" + server + ":" + port + ":" + forceOldSSL;
    return myConnected ? null : ERROR_LINE;
  }

  @Override
  public String createAccountAndConnect(String username, String password, String server, int port, boolean forceOldSSL) {
    myLog += "createAccount_" + username + ":" + password + "@" + server + ":" + port + ":" + forceOldSSL;
    return myConnected ? null : ERROR_LINE;
  }

  @Override
  public void setVCardInfo(String nickName, String firstName, String lastName) {
    myLog += "_setVCard" + nickName + firstName + lastName;
  }

  @Override
  public boolean isConnectedAndAuthenticated() {
    return myConnected;
  }

  public void setConnected(boolean b) {
    myConnected = b;
  }

  @Override
  public void saveSettings() {
    myLog += "saveSettings";
  }

  @Override
  public void addUsers(String group, List<String>/*<String>*/ list) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void addConnectionListener(ConnectionListener connectionListener) {
  }

  @Override
  public void removeConnectionListener(ConnectionListener connectionListener) {
  }


  public void unsubscribe(String user) {
  }

  public String getLog() {
    return myLog;
  }

  public void clearLog() {
    myLog = "";
  }
}
