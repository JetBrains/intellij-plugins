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
package jetbrains.communicator.jabber.impl;

import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.thoughtworks.xstream.XStream;
import jetbrains.communicator.core.users.PresenceMode;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.jabber.AccountInfo;
import jetbrains.communicator.jabber.ConnectionListener;
import jetbrains.communicator.jabber.JabberFacade;
import jetbrains.communicator.jabber.VCardInfo;
import jetbrains.communicator.util.CommunicatorStrings;
import jetbrains.communicator.util.WaitFor;
import jetbrains.communicator.util.XStreamUtil;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NonNls;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;
import org.picocontainer.Disposable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kir
 */
public class JabberFacadeImpl implements JabberFacade, Disposable {
  @NonNls
  private static final Logger LOG = Logger.getLogger(JabberFacadeImpl.class);

  public static final String FILE_NAME = "jabberSettings.xml";
  private JabberSettings mySettings;
  private final IDEFacade myIdeFacade;
  private XStream myXStream;

  private final List<ConnectionListener> myConnectionListeners = ContainerUtil.createLockFreeCopyOnWriteList();
  private XMPPConnection myConnection;

  public JabberFacadeImpl(IDEFacade ideFacade) {
    myIdeFacade = ideFacade;
  }

  private void initSettingsIfNeeded() {
    if (mySettings == null) {
      myXStream = XStreamUtil.createXStream();
      mySettings = (JabberSettings) XStreamUtil.fromXml(myXStream, myIdeFacade.getConfigDir(), FILE_NAME, false);
      if (mySettings == null) {
        mySettings = new JabberSettings();
      }
    }
  }

  @Override
  public void dispose() {
    disconnect();
  }

  @Override
  public void disconnect() {
    if (myConnection != null && myConnection.isConnected()) {
      myConnection.close();
    }
    myConnection = null;
  }

  @Override
  public String[] getServers() {
    SAXBuilder saxBuilder = new SAXBuilder();
    try {
      Document document = saxBuilder.build(getClass().getResource("servers.xml"));
      Element rootElement = document.getRootElement();
      List children = rootElement.getChildren("item", rootElement.getNamespace());
      List<String> result = new ArrayList<>();
      for (Object aChildren : children) {
        Element element = (Element) aChildren;
        result.add(element.getAttributeValue("jid"));
      }
      return ArrayUtil.toStringArray(result);
    } catch (JDOMException e) {
      LOG.error(e.getMessage(), e);
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
    return new String[]{"jabber.org"};
  }

  @Override
  public AccountInfo getMyAccount() {
    initSettingsIfNeeded();
    return mySettings.getAccount();
  }

  @Override
  public String connect() {
    if (isConnectedAndAuthenticated()) return null;
    AccountInfo info = getMyAccount();
    if (info == null || !info.isLoginAllowed()) return null;
    return connect(info.getUsername(), info.getPassword(), info.getServer(), info.getPort(), info.isForceSSL());
  }

  @Override
  public String connect(String username, String password, String server, int port, boolean forceOldSSL) {
    return _createConnection(server, port, username, password, false, forceOldSSL);
  }

  @Override
  public String createAccountAndConnect(String username, String password, String server, int port, boolean forceOldSSL) {
    return _createConnection(server, port, username, password, true, forceOldSSL);
  }

  private String _createConnection(String server, int port, final String username, String password, boolean createAccount, boolean forceOldSSL) {
    try {
      initSettingsIfNeeded();

      XMPPConnection.addConnectionListener(new ConnectionEstablishedListener() {
        @Override
        public void connectionEstablished(XMPPConnection connection) {
          XMPPConnection.removeConnectionListener(this);
          fireConnected(connection);
        }
      });

      String serviceName = server;
      if ("talk.google.com".equals(server)) {
        serviceName = "gmail.com";
      }

      String user = username;
      int at = username.indexOf('@');
      if (at > 0) {
        serviceName = username.substring(at + 1);
        user = username.substring(0, at);
      }

      XMPPConnection connection;
      if (forceOldSSL) {
        connection = new SSLXMPPConnection(server, port, serviceName);
      }
      else {
        connection = new XMPPConnection(server, port, serviceName);
      }

      if (createAccount && connection.getAccountManager().supportsAccountCreation()) {
        connection.getAccountManager().createAccount(user, password.replaceAll("&", "&amp;"));
      }

      if (!connection.isConnected()) return CommunicatorStrings.getMsg("unable.to.connect.to", server, port);
      connection.login(user, password, IDETALK_RESOURCE);

      saveAccountData(server, port, username, password, forceOldSSL);

      if (rosterIsNotAvailable(connection)) {
        connection.close();
        return CommunicatorStrings.getMsg("no.roster.try.again");
      }

      myConnection = connection;

      fireAuthenticated();
      myConnection.addConnectionListener(new SmackConnectionListener());
    } catch (XMPPException e) {
      String message = getMessage(e);
      if (message != null && (message.indexOf("authentication failed") == -1 || password.length() != 0)) {
        LOG.info(message, e);
      }
      return message;
    } catch (IllegalStateException e) {
      LOG.info(e, e);
      return e.getMessage();
    }

    return null;
  }

  private boolean rosterIsNotAvailable(final XMPPConnection connection) {
    new WaitFor(3000) {
      @Override
      protected boolean condition() {
        return connection.getRoster() != null;
      }
    };
    return connection.getRoster() == null;
  }

  private String getMessage(XMPPException e) {
    return e.getXMPPError() == null ? e.getMessage() : e.getXMPPError().toString();
  }

  private void saveAccountData(String server, int port, String username, String password, boolean forceOldSSL) {
    getMyAccount().setServer(server);
    getMyAccount().setPort(port);
    getMyAccount().setUsername(username);
    getMyAccount().setPassword(password);
    getMyAccount().setForceSSL(forceOldSSL);
  }

  @Override
  public void setVCardInfo(String nickName, String firstName, String lastName) throws XMPPException {
    assert isConnectedAndAuthenticated() : "Not connected or authenticated";
    VCard vCard = new VCard();
    vCard.setFirstName(firstName);
    vCard.setLastName(lastName);
    vCard.setNickName(nickName);

    PacketCollector collector = myConnection.createPacketCollector(new PacketIDFilter(vCard.getPacketID()));
    vCard.save(myConnection);

    IQ response = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
    collector.cancel();

    if (response == null) {
      throw new XMPPException("No response from the server.");
    }
    // If the server replied with an error, throw an exception.
    else if (response.getType() == IQ.Type.ERROR) {
      throw new XMPPException(response.getError());
    }
  }

  @Override
  public VCardInfo getVCard(String jabberId) {
    assert isConnectedAndAuthenticated() : "Not connected or authenticated";
    VCard vCard = new VCard();
    try {
      if (jabberId == null) {
        vCard.load(myConnection);
      } else {
        vCard.load(myConnection, jabberId);
      }
    } catch (XMPPException e) {
      return new VCardInfo("N/A", "N/A", "N/A");
    }
    return new VCardInfo(vCard.getFirstName(), vCard.getLastName(), vCard.getNickName());
  }

  @Override
  public boolean isConnectedAndAuthenticated() {
    return myConnection != null && myConnection.isAuthenticated();
  }

  @Override
  public XMPPConnection getConnection() {
    return myConnection;
  }

  @Override
  public void changeSubscription(String from, boolean subscribe) {
    LOG.info((subscribe ? "Accepted": "Denied" ) + " adding self to " + from + "'s contact list.");
    changeSubscription(from, subscribe ? Presence.Type.subscribed : Presence.Type.unsubscribed);
  }

  private void changeSubscription(String user, Presence.Type type) {
    Presence reply = new Presence(type);
    reply.setTo(user);
    myConnection.sendPacket(reply);
  }

  @Override
  public void setOnlinePresence(UserPresence userPresence) {
    final Presence.Mode mode;
    String status = "";
    PresenceMode presenceMode = userPresence.getPresenceMode();
    switch(presenceMode) {
      case AWAY:
        mode = Presence.Mode.away;
        break;
      case EXTENDED_AWAY:
        mode = Presence.Mode.xa;
        break;
      case DND: mode = Presence.Mode.dnd; break;
      default: mode = Presence.Mode.available;
    }
    Presence presence = new Presence(Presence.Type.available, status, 0, mode);
    myConnection.sendPacket(presence);
  }

  @Override
  public void saveSettings() {
    initSettingsIfNeeded();
    if (!mySettings.getAccount().shouldRememberPassword()) {
      mySettings.getAccount().setPassword("");
    }
    XStreamUtil.toXml(myXStream, myIdeFacade.getConfigDir(), FILE_NAME, mySettings);
  }

  @Override
  public void addUsers(String group, List<String> list) {
    if (!isConnectedAndAuthenticated()) return;

    String self = getConnection().getUser();
    for (String id : list) {
      if (!self.startsWith(id)) {
        try {
          Roster roster = getConnection().getRoster();
          RosterEntry oldEntry = roster.getEntry(id);
          if (oldEntry != null) {
            roster.removeEntry(oldEntry);
          }
          roster.createEntry(id, JabberTransport.getSimpleId(id), new String[]{group});
        } catch (XMPPException e) {
          myIdeFacade.showMessage(CommunicatorStrings.getMsg("jabber.error.while.adding.user.title", id)
              , CommunicatorStrings.getMsg("jabber.error.while.adding.user.text", id, getMessage(e))
          );
          LOG.info(getMessage(e), e);
        }
      }
    }
  }

  @Override
  public void addConnectionListener(ConnectionListener connectionListener) {
    myConnectionListeners.add(connectionListener);
  }

  @Override
  public void removeConnectionListener(ConnectionListener connectionListener) {
    myConnectionListeners.remove(connectionListener);
  }

  protected void fireConnected(XMPPConnection connection) {
    for (ConnectionListener listener : myConnectionListeners) {
      listener.connected(connection);
    }
  }

  protected void fireAuthenticated() {
    for (ConnectionListener listener : myConnectionListeners) {
      listener.authenticated();
    }
  }

  protected void fireDisconnected(boolean onError) {
    for (ConnectionListener listener : myConnectionListeners) {
      listener.disconnected(onError);
    }
  }

  private class SmackConnectionListener implements org.jivesoftware.smack.ConnectionListener {
    @Override
    public void connectionClosed() {
      myConnection.removeConnectionListener(this);
      fireDisconnected(false);
    }

    @Override
    public void connectionClosedOnError(Exception exception) {
      try {
        myConnection.removeConnectionListener(this);
      } finally {
        fireDisconnected(true);
      }
    }

    public void reconectionSuccessful() {
    }

    public void reconnectingIn(int seconds) {
    }

    public void reconnectionFailed(Exception e) {
      try {
        myConnection.removeConnectionListener(this);
      } finally {
        fireDisconnected(true);
      }
    }

  }
}
