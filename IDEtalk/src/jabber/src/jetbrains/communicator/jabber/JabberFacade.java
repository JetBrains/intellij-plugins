// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.jabber;

import jetbrains.communicator.core.users.UserPresence;
import org.jetbrains.annotations.NonNls;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import java.util.List;

/**
 * @author Kir
 */
public interface JabberFacade {
  @NonNls
  String IDETALK_RESOURCE = "IDEtalk";

  /** Returns sorted Jabber server names */
  String[] getServers();

  AccountInfo getMyAccount();

  /** Tries to create jabber connection according to current credentials. If valid connection already exists,
   * does nothing.
   * @return null if connection was successful or already exists, error message otherwise
  * @see #connect(String, String, String, int, boolean) */
  String connect();

  /** Tries to create Jabber connection according to given credentials.
   * If successfull, stores the credentials in the AccountInfo.
   * @return null if connection was successful, error message otherwise
   * @see #getMyAccount()   */
  String connect(String username, String password, String server, int port, boolean forceOldSSL);

  /** @see #connect(String, String, String, int, boolean) */
  String createAccountAndConnect(String username, String password, String server, int port, boolean forceOldSSL);

  void disconnect();

  /** Set firstName and lastName for currently connected user in Jabber VCard */
  void setVCardInfo(String nickName, String firstName, String lastName) throws XMPPException;
  /** Return some user information for userId*/
  VCardInfo getVCard(String userId);


  /** @return true if Jabber connection exists and user is logged in  */
  boolean isConnectedAndAuthenticated();

  void saveSettings();

  /** Adds users specified by list of JabberIDs to the contact list */
  void addUsers(String group, List<String>/*<String>*/ list);

  void addConnectionListener(ConnectionListener connectionListener);
  void removeConnectionListener(ConnectionListener connectionListener);

  XMPPConnection getConnection();

  /** Method should send a packet to user 'from' which would allow to add
   * self to from's contact list */
  void changeSubscription(String from, boolean subscribe);

  void setOnlinePresence(UserPresence userPresence);

}
