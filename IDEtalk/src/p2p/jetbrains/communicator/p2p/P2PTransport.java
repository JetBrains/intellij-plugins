// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.p2p;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.util.Pair;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.SmartList;
import icons.IdeTalkCoreIcons;
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
import jetbrains.communicator.ide.TalkProgressIndicator;
import jetbrains.communicator.p2p.commands.AddOnlineUserP2PCommand;
import jetbrains.communicator.p2p.commands.SendXmlMessageP2PCommand;
import jetbrains.communicator.util.CommunicatorStrings;
import jetbrains.communicator.util.UIUtil;
import jetbrains.communicator.util.WaitFor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.BuiltInServerManager;
import org.jetbrains.ide.CustomPortServerManager;
import org.jetbrains.io.CustomPortServerManagerBase;
import org.picocontainer.Disposable;
import org.picocontainer.MutablePicoContainer;

import javax.swing.*;
import java.net.InetAddress;
import java.util.*;

/**
 * @author Kir Maximov
 */
public final class P2PTransport implements Transport, UserMonitorClient, Disposable {
  private static final Logger LOG = Logger.getLogger(P2PTransport.class);

  static final String CODE = "P2P";

  static final int XML_RPC_PORT = MulticastPingThread.MULTICAST_PORT + 1;

  private final UserMonitorThread myUserMonitorThread;

  private final Object myLock = new Object();
  private final Map<User, OnlineUserInfo> myUserToInfo = new HashMap<>();
  private final Map<User, OnlineUserInfo> myUserToInfoNew = new HashMap<>();
  private final Collection<User> myOnlineUsers = new HashSet<>();

  private final EventBroadcaster myEventBroadcaster;
  private final IDEtalkListener myUserAddedCallbackListener;
  private final AsyncMessageDispatcher myAsyncMessageDispatcher;
  private final UserModel myUserModel;
  private UserPresence myOwnPresence;

  public P2PTransport(AsyncMessageDispatcher asyncMessageDispatcher, UserModel userModel) {
    this(asyncMessageDispatcher, userModel, UserMonitorThread.WAIT_USER_RESPONSES_TIMEOUT);
  }

  public P2PTransport(AsyncMessageDispatcher asyncMessageDispatcher, UserModel userModel, long waitUserResponsesTimeout) {
    myEventBroadcaster = userModel.getBroadcaster();
    myAsyncMessageDispatcher = asyncMessageDispatcher;
    myUserModel = userModel;
    myUserAddedCallbackListener = new TransportUserListener(this) {
      @Override
      protected void processAfterChange(UserEvent event) {
        event.accept(new EventVisitor() {
          @Override
          public void visitUserAdded(UserEvent.Added event) {
            super.visitUserAdded(event);
            sendUserAddedCallback(event.getUser());
          }
        });
      }
    };

    myOwnPresence = new UserPresence(true);
    myUserMonitorThread = new UserMonitorThread(this, waitUserResponsesTimeout);

    Map<String, Object> handlers = CustomPortServerManager.EP_NAME.findExtensionOrFail(P2PCustomPortServerManager.class).handlers;
    for (P2PCommand command : new P2PCommand[]{
      new SendXmlMessageP2PCommand(myEventBroadcaster, this),
      new AddOnlineUserP2PCommand(myUserMonitorThread),
    }) {
      handlers.put(command.getXmlRpcId(), command);
    }

    startup();

    Runtime.getRuntime().addShutdownHook(new IDETalkShutdownHook());
  }

  class IDETalkShutdownHook extends Thread {
    IDETalkShutdownHook() {
      super("IDE Talk shutdown hook");
      setDaemon(true);
    }

    @Override
    public void run() {
      dispose();
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  private static final class P2PCustomPortServerManager extends CustomPortServerManagerBase {
    private final Map<String, Object> handlers = Collections.synchronizedMap(new HashMap<>());

    @Override
    public void cannotBind(@NotNull Exception e, int port) {
      String groupDisplayId = "IDETalk XmlRpc Server";
      Notifications.Bus.register(groupDisplayId, NotificationDisplayType.STICKY_BALLOON);
      new Notification(groupDisplayId, "IDETalk XmlRpc server on custom port " + port + " disabled",
                       "Cannot start IDETalk XmlRpc server on custom port " + port + "." +
                       "Please ensure that port is free (or check your firewall settings) and restart " + ApplicationNamesInfo.getInstance().getFullProductName(),
                       NotificationType.ERROR).notify(null);
    }

    @Override
    public int getPort() {
      return XML_RPC_PORT;
    }

    @Override
    public boolean isAvailableExternally() {
      return true;
    }

    @NotNull
    @Override
    public Map<String, Object> createXmlRpcHandlers() {
      return handlers;
    }
  }

  private void startup() {
    Application application = ApplicationManager.getApplication();
    if (application.isUnitTestMode()) {
      doStart();
    }
    else {
      application.executeOnPooledThread(() -> doStart());
    }
  }

  private void doStart() {
    BuiltInServerManager.getInstance().waitForStart();
    myUserMonitorThread.start();
    myUserMonitorThread.triggerFindNow();
    new WaitFor() {
      @Override
      protected boolean condition() {
        return myUserMonitorThread.isRunning();
      }
    };

    myEventBroadcaster.addListener(myUserAddedCallbackListener);
  }

  IDEFacade getIdeFacade() {
    return myAsyncMessageDispatcher.getIdeFacade();
  }

  @Override
  public void dispose() {
    try {
      myEventBroadcaster.removeListener(myUserAddedCallbackListener);
      myUserMonitorThread.shutdown();
    }
    catch (Throwable e) {
      LOG.info(e);
    }
    myOnlineUsers.clear();
  }

  @Override
  public void initializeProject(final String projectName, MutablePicoContainer projectLevelContainer) {
    getIdeFacade().runOnPooledThread(() -> {
      User[] users = findUsers(new NullProgressIndicator());
      Set<User> ourUsers = new HashSet<>();
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

  @Override
  public Class<? extends NamedUserCommand> getSpecificFinderClass() {
    return null;
  }

  @Override
  public String getName() {
    return CODE;
  }

  @Override
  public User[] findUsers(TalkProgressIndicator progressIndicator) {
    myUserMonitorThread.findNow(progressIndicator);

    return myOnlineUsers.toArray(new User[0]);
  }

  @Override
  public boolean isOnline() {
    return myOwnPresence.isOnline();
  }

  @Override
  public boolean hasIdeTalkClient(User user) {
    return true;
  }

  @Override
  public UserPresence getUserPresence(User user) {
    boolean online;
    synchronized (myLock) {
      online = myOnlineUsers.contains(user);
      if (online) {
        return getNotNullOnlineInfo(user).getPresence();
      }
    }
    return new UserPresence(false);
  }

  public InetAddress getAddress(User p2PUser) {
    return getNotNullOnlineInfo(p2PUser).getAddress();
  }

  @Override
  public boolean isSelf(User user) {
    InetAddress address = getAddress(user);
    return CommunicatorStrings.getMyUsername().equals(user.getName()) && NetworkUtil.isOwnAddress(address);
  }

  @Override
  public Icon getIcon(UserPresence userPresence) {
    return UIUtil.getIcon(userPresence, IdeTalkCoreIcons.IdeTalk.User, IdeTalkCoreIcons.IdeTalk.User_dnd);
  }

  public int getPort(User user) {
    return getNotNullOnlineInfo(user).getPort();
  }

  @Override
  public String[] getProjects(User user) {
    return ArrayUtilRt.toStringArray(getNotNullOnlineInfo(user).getProjects());
  }

  @NotNull
  private OnlineUserInfo getNotNullOnlineInfo(User user) {
    OnlineUserInfo result;
    synchronized (myLock) {
      result = myUserToInfo.get(user);
    }
    if (result == null) {
      result = new OnlineUserInfo(null, -1, new HashSet<>(), new UserPresence(false));
    }
    return result;
  }

  @Override
  public String getAddressString(User user) {
    return getAddress(user).getHostAddress() + ':' + getPort(user);
  }

  @Override
  public void sendXmlMessage(User user, XmlMessage message) {
    Message msg = SendXmlMessageP2PCommand.createNetworkMessage(message);
    if (message.needsResponse()) {
      myAsyncMessageDispatcher.sendNow(user, msg);
    }
    else {
      myAsyncMessageDispatcher.sendLater(user, msg);
    }
  }

  @Override
  public UserPresence getOwnPresence() {
    return myOwnPresence;
  }

  @Override
  public void setOwnPresence(UserPresence userPresence) {
    if (!myOwnPresence.isOnline() && userPresence.isOnline()) {
      startup();
    }
    else if (myOwnPresence.isOnline() && !userPresence.isOnline()) {
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

  @Override
  public void setOnlineUsers(@NotNull Collection<User> onlineUsers) {
    removeOfflineUsersAndUpdateOldOnlineUsers(onlineUsers);
    addNewOnlineUsers(onlineUsers);
    synchronized (myLock) {
      myUserToInfoNew.clear();
    }
  }

  public void setAvailable(String remoteUser) {
    final User user = myUserModel.findUser(remoteUser, getName());
    if (user != null) {
      UserPresence oldPresence = getNotNullOnlineInfo(user).getPresence();
      UserPresence newPresence = new UserPresence(true);
      myEventBroadcaster.doChange(new UserEvent.Updated(user, "presence", oldPresence, newPresence), new MySyncRunnable() {
        @Override
        protected void execute() {
          OnlineUserInfo onlineInfo = getNotNullOnlineInfo(user);
          onlineInfo.setPresence(new UserPresence(true));
          myUserToInfo.put(user, onlineInfo);
        }
      });
    }
  }

  private abstract class MySyncRunnable implements Runnable {
    @Override
    public final void run() {
      synchronized (myLock) {
        execute();
      }
    }

    protected abstract void execute();
  }

  @Override
  public User createUser(String remoteUsername, @NotNull OnlineUserInfo onlineUserInfo) {
    synchronized (myLock) {
      User user = myUserModel.createUser(remoteUsername, CODE);
      myUserToInfoNew.put(user, onlineUserInfo);
      return user;
    }
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

  @Override
  public int getPort() {
    return XML_RPC_PORT;
  }

  private void addNewOnlineUsers(@NotNull Collection<User> onlineUsers) {
    List<Pair<IDEtalkEvent, Runnable>> events = new SmartList<>();
    synchronized (myLock) {
      for (final User user : onlineUsers) {
        if (!myOnlineUsers.contains(user) && myUserToInfoNew.containsKey(user)) {
          events.add(new Pair<>(new UserEvent.Online(user), new MySyncRunnable() {
            @Override
            protected void execute() {
              myOnlineUsers.add(user);
              myUserToInfo.put(user, myUserToInfoNew.get(user));
            }
          }));
        }
      }
    }

    dispatchEvents(events);
  }

  private void removeOfflineUsersAndUpdateOldOnlineUsers(@NotNull Collection onlineUsers) {
    List<Pair<IDEtalkEvent, Runnable>> events = new SmartList<>();
    synchronized (myLock) {
      for (final User user : myOnlineUsers) {
        if (!onlineUsers.contains(user)) {
          // User was removed
          events.add(new Pair<>(new UserEvent.Offline(user), new MySyncRunnable() {
            @Override
            protected void execute() {
              myOnlineUsers.remove(user);
              myUserToInfo.remove(user);
            }
          }));
        }
        else {
          // User already exists
          UserPresence oldPresence = getNotNullOnlineInfo(user).getPresence();
          final OnlineUserInfo onlineUserInfo = myUserToInfoNew.get(user);
          if (onlineUserInfo == null) {
            return;
          }

          UserPresence newPresence = onlineUserInfo.getPresence();
          if (!newPresence.equals(oldPresence)) {
            events.add(new Pair<>(new UserEvent.Updated(user, "presence", oldPresence, newPresence), new MySyncRunnable() {
              @Override
              protected void execute() {
                myUserToInfo.put(user, onlineUserInfo);
              }
            }));
          }
          else {
            myUserToInfo.put(user, onlineUserInfo);
          }
        }
      }
    }

    dispatchEvents(events);
  }

  private void dispatchEvents(List<Pair<IDEtalkEvent, Runnable>> events) {
    for (Pair<IDEtalkEvent, Runnable> event : events) {
      try {
        myEventBroadcaster.doChange(event.first, event.second);
      }
      catch (Throwable e) {
        LOG.error(e);
      }
    }
  }

  public static P2PTransport getInstance() {
    return (P2PTransport) Pico.getInstance().getComponentInstanceOfType(P2PTransport.class);
  }
}

