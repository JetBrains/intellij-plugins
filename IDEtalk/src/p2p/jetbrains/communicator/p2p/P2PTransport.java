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
package jetbrains.communicator.p2p;

import jetbrains.communicator.core.*;
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.dispatcher.AsyncMessageDispatcher;
import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.transport.WasAddedXmlMessage;
import jetbrains.communicator.core.transport.XmlMessage;
import jetbrains.communicator.core.users.*;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.NullProgressIndicator;
import jetbrains.communicator.ide.ProgressIndicator;
import jetbrains.communicator.p2p.commands.AddOnlineUserP2PCommand;
import jetbrains.communicator.p2p.commands.SendXmlMessageP2PCommand;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.UIUtil;
import jetbrains.communicator.util.WaitFor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.Disposable;
import org.picocontainer.MutablePicoContainer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.*;

/**
 * @author Kir Maximov
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class P2PTransport implements Transport, UserMonitorClient, Disposable {
  private static final Logger LOG = Logger.getLogger(P2PTransport.class);

  static final String CODE = "P2P";

  static final int XML_RPC_PORT = MulticastPingThread.MULTICAST_PORT + 1;
  private int myPort;

  private P2PServer myP2PServer;
  private UserMonitorThread myUserMonitorThread;

  private final Map<User,OnlineUserInfo> myUser2Info = new HashMap<User, OnlineUserInfo>();
  private final Map<User,OnlineUserInfo> myUser2InfoNew = new HashMap<User, OnlineUserInfo>();

  private final Collection<User> myOnlineUsers =
      Collections.synchronizedCollection(new HashSet<User>());

  private final EventBroadcaster myEventBroadcaster;
  private final IDEtalkListener myUserAddedCallbackListener;
  private final AsyncMessageDispatcher myAsyncMessageDispatcher;
  private final UserModel myUserModel;
  private UserPresence myOwnPresence;

  public P2PTransport(AsyncMessageDispatcher asyncMessageDispatcher, UserModel userModel) throws IOException {
    this(asyncMessageDispatcher, userModel, UserMonitorThread.WAIT_USER_RESPONSES_TIMEOUT);
  }
  public P2PTransport(AsyncMessageDispatcher asyncMessageDispatcher, UserModel userModel, long waitUserResponsesTimeout) throws IOException {

    myEventBroadcaster = userModel.getBroadcaster();
    myAsyncMessageDispatcher = asyncMessageDispatcher;
    myUserModel = userModel;
    myUserAddedCallbackListener = new TransportUserListener(this) {
      protected void processAfterChange(UserEvent event) {
        event.accept(new EventVisitor() {
          public void visitUserAdded(UserEvent.Added event) {
            super.visitUserAdded(event);
            sendUserAddedCallback(event.getUser());
          }
        });
      }
    };

    myOwnPresence = new UserPresence(true);

    startup(waitUserResponsesTimeout);

    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        dispose();
      }
    }));
  }

  private void initializeXmlRpcPort() {
    int port = XML_RPC_PORT;

    if (NetworkUtil.isPortBusy(port)) {
      ServerSocket socket = null;
      try {
        socket = new ServerSocket(0);
        port = socket.getLocalPort();
      } catch (IOException e) {
        final String msg = "Unable to get free port for IDEtalk: " + e.getMessage();
        LOG.warn(msg);
        LOG.debug(msg, e);
        port = -1;
      }
      finally {
        try { if (socket != null) socket.close(); } catch (IOException e) {  }
      }
    }

    myPort = port;
  }

  private void startup(long waitUserResponsesTimeout) throws IOException {
    myUserMonitorThread = new UserMonitorThread(this, waitUserResponsesTimeout);

    initializeXmlRpcPort();
    if (myPort >= 0) {
      myP2PServer = new P2PServer(myPort, new P2PCommand[] {
        new SendXmlMessageP2PCommand(myEventBroadcaster, this),
        new AddOnlineUserP2PCommand(myUserMonitorThread),
      });
      LOG.info("Internal Web server is bound to port " + myPort);

      myUserMonitorThread.start();
      myUserMonitorThread.triggerFindNow();
      new WaitFor() { protected boolean condition() { return myUserMonitorThread.isRunning(); } };

      myEventBroadcaster.addListener(myUserAddedCallbackListener);
    }
  }

  IDEFacade getIdeFacade() {
    return myAsyncMessageDispatcher.getIdeFacade();
  }

  public void dispose() {
    myEventBroadcaster.removeListener(myUserAddedCallbackListener);
    myUserMonitorThread.shutdown();
    if (myP2PServer != null) {
      myP2PServer.shutdown();
    }

    myOnlineUsers.clear();
  }

  public void initializeProject(final String projectName, MutablePicoContainer projectLevelContainer) {
    getIdeFacade().runOnPooledThread(new Runnable() {
      public void run() {
        User[] users = findUsers(new NullProgressIndicator());
        Set<User> ourUsers = new HashSet<User>();
        for (User user : users) {
          if (Arrays.asList(user.getProjects()).contains(projectName)) {
            ourUsers.add(user);
          }
        }

        if (canAddUsers(projectName, ourUsers)) {
          for (User user : ourUsers) {
            if (!myUserModel.hasUser(user)) {
              user.setGroup(projectName, myUserModel);
              myUserModel.addUser(user);
            }
          }
        }
      }
    });
  }

  boolean canAddUsers(String projectName, Collection<User> users) {
    if (users.size() == 0) return false;
    return hasGroup(projectName) || hasNoUsersFrom(users);
  }

  private boolean hasNoUsersFrom(Collection<User> users) {
    for (User user : users) {
      if (myUserModel.hasUser(user)) {
        return false;
      }
    }
    return true;
  }

  private boolean hasGroup(String projectName) {
    return Arrays.asList(myUserModel.getGroups()).contains(projectName);
  }

  public Class<? extends NamedUserCommand> getSpecificFinderClass() {
    return null;
  }

  public String getName() {
    return CODE;
  }

  public User[] findUsers(ProgressIndicator progressIndicator) {
    myUserMonitorThread.findNow(progressIndicator);

    return myOnlineUsers.toArray(new User[myOnlineUsers.size()]);
  }

  public boolean isOnline() {
    return myOwnPresence.isOnline();
  }

  public boolean hasIDEtalkClient(User user) {
    return true;
  }

  public UserPresence getUserPresence(User user) {
    boolean online = myOnlineUsers.contains(user);
    if (online) {
      return getNotNullOnlineInfo(user).getPresence();
    }
    return new UserPresence(false);
  }

  public InetAddress getAddress(User p2PUser) {
    return getNotNullOnlineInfo(p2PUser).getAddress();
  }

  public boolean isSelf(User user) {
    InetAddress address = getAddress(user);
    return StringUtil.getMyUsername().equals(user.getName()) && NetworkUtil.isOwnAddress(address);
  }

  public String getIconPath(UserPresence userPresence) {
    return UIUtil.getIcon(userPresence, "/ideTalk/user.png", "/ideTalk/user_dnd.png");
  }

  public int getPort(User user) {
    return getNotNullOnlineInfo(user).getPort();
  }

  public String[] getProjects(User user) {
    Collection<String> projects = getNotNullOnlineInfo(user).getProjects();
    return projects.toArray(new String[projects.size()]);
  }

  @NotNull
  private OnlineUserInfo getNotNullOnlineInfo(User user) {
    OnlineUserInfo result = myUser2Info.get(user);
    if (result == null) {
      result = new OnlineUserInfo(null, -1, new HashSet<String>(), new UserPresence(false));
    }
    return result;
  }

  public String getAddressString(User user) {
    return getAddress(user).getHostAddress() + ':' + getPort(user);
  }

  public void sendXmlMessage(User user, XmlMessage message) {
    Message msg = SendXmlMessageP2PCommand.createNetworkMessage(message);
    if (message.needsResponse()) {
      myAsyncMessageDispatcher.sendNow(user, msg);
    }
    else {
      myAsyncMessageDispatcher.sendLater(user, msg);
    }
  }

  public UserPresence getOwnPresence() {
    return myOwnPresence;
  }

  public void setOwnPresence(UserPresence userPresence) {

    if (!myOwnPresence.isOnline() && userPresence.isOnline()) {
      try {
        startup(myUserMonitorThread.getWaitUserResponsesTimeout());
      } catch (IOException e) {
        LOG.error(e.getMessage() + " server port " + myPort, e);
      }
    } else if (myOwnPresence.isOnline() && !userPresence.isOnline()) {
      dispose();
    }

    if (selfBecomeAvailable(userPresence)) {
      notifyUsersAboutOnlineImmediately();
    }

    myOwnPresence = userPresence;
  }

  private boolean selfBecomeAvailable(UserPresence userPresence) {
    return
        myOwnPresence.getPresenceMode() != PresenceMode.AVAILABLE &&
        userPresence.getPresenceMode() == PresenceMode.AVAILABLE;
  }

  private void notifyUsersAboutOnlineImmediately() {
    for (User user : myUserModel.getAllUsers()) {
      if (user.getTransportCode().equals(getName())) {
        user.sendXmlMessage(new BecomeAvailableXmlMessage());
      }
    }
  }

  public synchronized void setOnlineUsers(Collection<User> onlineUsers) {
    assert onlineUsers != null;

    removeOfflineUsers_And_UpdateOldOnlineUsers(onlineUsers);
    addNewOnlineUsers(onlineUsers);
    myUser2InfoNew.clear();
  }

  public void setAvailable(String remoteUser) {
    final User user = myUserModel.findUser(remoteUser, getName());
    if (user != null) {
      UserPresence oldPresence = getNotNullOnlineInfo(user).getPresence();
      final UserPresence newPresence = new UserPresence(true);

      myEventBroadcaster.doChange(new UserEvent.Updated(user, "presence", oldPresence, newPresence), new Runnable() {
        public void run() {
          makeUserOnline(user);
        }
      });
    }
  }

  private void makeUserOnline(User user) {
    OnlineUserInfo onlineInfo = getNotNullOnlineInfo(user);
    onlineInfo.setPresence(new UserPresence(true));
    myUser2Info.put(user, onlineInfo);
  }

  public synchronized User createUser(String remoteUsername, @NotNull OnlineUserInfo onlineUserInfo) {
    User user = myUserModel.createUser(remoteUsername, CODE);
    myUser2InfoNew.put(user, onlineUserInfo);
    return user;
  }

  void flushCurrentUsers() {
    myUserMonitorThread.flushOnlineUsers();
  }

  UserMonitorThread getUserMonitorThread() {
    return myUserMonitorThread;
  }

  protected void sendUserAddedCallback(User user) {
    sendXmlMessage(user, new WasAddedXmlMessage());
  }

  public int getPort() {
    return myPort;
  }

  private void addNewOnlineUsers(Collection<User> onlineUsers) {
    for (final User user : onlineUsers) {
      if (!myOnlineUsers.contains(user) && myUser2InfoNew.containsKey(user)) {
        myEventBroadcaster.doChange(new UserEvent.Online(user), new Runnable() {
          public void run() {
            myOnlineUsers.add(user);
            myUser2Info.put(user, myUser2InfoNew.get(user));
          }
        });
      }
    }
  }

  private void removeOfflineUsers_And_UpdateOldOnlineUsers(Collection onlineUsers) {
    for (final Iterator<User> it = myOnlineUsers.iterator(); it.hasNext();) {
      final User user = it.next();
      if (!onlineUsers.contains(user)) {  // User was removed
        myEventBroadcaster.doChange(new UserEvent.Offline(user), new Runnable() {
              public void run() {
                it.remove();
                myUser2Info.remove(user);
              }
            });
      }
      else { // User already exists
        UserPresence oldPresence = getNotNullOnlineInfo(user).getPresence();
        final OnlineUserInfo onlineUserInfo = myUser2InfoNew.get(user);
        if (onlineUserInfo == null) return;
        UserPresence newPresence = onlineUserInfo.getPresence();

        if (!newPresence.equals(oldPresence)) {
          myEventBroadcaster.doChange(new UserEvent.Updated(user, "presence", oldPresence, newPresence), new Runnable() {
            public void run() {
              myUser2Info.put(user, onlineUserInfo);
            }
          });
        }
        else {
          myUser2Info.put(user, onlineUserInfo);
        }
      }
    }
  }

  public static P2PTransport getInstance() {
    return (P2PTransport) Pico.getInstance().getComponentInstanceOfType(P2PTransport.class);
  }

}

