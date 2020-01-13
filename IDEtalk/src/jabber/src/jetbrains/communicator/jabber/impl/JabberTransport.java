// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.jabber.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;
import icons.IdeTalkCoreIcons;
import jetbrains.communicator.core.*;
import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.dispatcher.AsyncMessageDispatcher;
import jetbrains.communicator.core.transport.*;
import jetbrains.communicator.core.users.*;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.TalkProgressIndicator;
import jetbrains.communicator.jabber.ConnectionListener;
import jetbrains.communicator.jabber.JabberFacade;
import jetbrains.communicator.jabber.JabberUI;
import jetbrains.communicator.jabber.JabberUserFinder;
import jetbrains.communicator.util.IgnoreList;
import jetbrains.communicator.util.UIUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.ThreadFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.picocontainer.Disposable;
import org.picocontainer.MutablePicoContainer;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.Future;

import static jetbrains.communicator.core.users.UserEvent.Updated.*;
import static jetbrains.communicator.util.CommunicatorStrings.getMsg;

/**
 * @author Kir
 */
public class JabberTransport implements Transport, ConnectionListener, Disposable {
  @NonNls
  private static final Logger LOG = Logger.getInstance(JabberTransport.class);

  private static final int RESPONSE_TIMEOUT = 120*1000;
  @NonNls public static final String CODE = "Jabber";

  private final JabberUI myUI;
  private final JabberFacade myFacade;
  private final UserModel myUserModel;
  private final IDEtalkListener myUserModelListener = new MyUserModelListener();
  private final AsyncMessageDispatcher myDispatcher;

  private RosterListener myRosterListener;
  private PacketListener mySubscribeListener;
  private PacketListener myMessageListener;
  private final JabberUserFinder myUserFinder;
  private final IDEFacade myIdeFacade;

  private final String myThreadIdPrefix = StringUtils.randomString(5);
  private int myCurrentThreadId;
  private boolean myIgnoreUserEvents;
  private PresenceMode myPresenceMode;

  private final Map<User, UserPresence> myUser2Presence = new HashMap<>();
  private final Set<String> myIDEtalkUsers = new HashSet<>();
  private final Map<String, String> myUser2Thread = Collections.synchronizedMap(new HashMap<>());

  @NonNls
  private static final String RESPONSE = "response";
  private final IgnoreList myIgnoreList;

  // negative value disables reconnect
  private int myReconnectTimeout = Integer.parseInt(System.getProperty("ideTalk.reconnect", "30")) * 1000;
  private Future<?> myReconnectProcess;

  public JabberTransport(JabberUI UI, JabberFacade facade, UserModel userModel, AsyncMessageDispatcher messageDispatcher, JabberUserFinder userFinder) {
    Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

    //XMPPConnection.DEBUG_ENABLED = true;
    JDOMExtension.init();


    myUI = UI;
    myFacade = facade;
    myUserModel = userModel;
    myDispatcher = messageDispatcher;
    myUserFinder = userFinder;
    myIdeFacade = messageDispatcher.getIdeFacade();
    myIgnoreList = new IgnoreList(myIdeFacade);

    myFacade.addConnectionListener(this);
    getBroadcaster().addListener(myUserModelListener);
  }

  private EventBroadcaster getBroadcaster() {
    return myUserModel.getBroadcaster();
  }

  @Override
  public String getName() {
    return CODE;
  }

  @Override
  public void initializeProject(String projectName, MutablePicoContainer projectLevelContainer) {
    myUI.initPerProject(projectLevelContainer);
    myIdeFacade.runOnPooledThread(() -> myFacade.connect());
  }

  @Override
  public User[] findUsers(TalkProgressIndicator progressIndicator) {
    if (isOnline()) {
      return myUserFinder.findUsers(progressIndicator);
    }
    return new User[0];
  }

  @Override
  public Class<? extends NamedUserCommand> getSpecificFinderClass() {
    return FindByJabberIdCommand.class;
  }

  @Override
  public boolean isOnline() {
    return myFacade.isConnectedAndAuthenticated();
  }

  @Override
  public UserPresence getUserPresence(User user) {
    UserPresence presence = myUser2Presence.get(user);
    if (presence == null) {
      presence = new UserPresence(false);
      myUser2Presence.put(user, presence);
    }
    return presence;
  }

  private UserPresence _getUserPresence(User user) {
    Presence presence = _getPresence(user);
    if (presence != null && presence.getType() == Presence.Type.available) {
      Presence.Mode mode = presence.getMode();
      final PresenceMode presenceMode;
      //noinspection IfStatementWithTooManyBranches
      if (mode == Presence.Mode.away) {
        presenceMode = PresenceMode.AWAY;
      }
      else if (mode == Presence.Mode.dnd) {
        presenceMode = PresenceMode.DND;
      }
      else if (mode == Presence.Mode.xa) {
        presenceMode = PresenceMode.EXTENDED_AWAY;
      }
      else {
        presenceMode = PresenceMode.AVAILABLE;
      }

      return new UserPresence(presenceMode);
    }
    return new UserPresence(false);
  }

  @Override
  @NonNls
  public Icon getIcon(UserPresence userPresence) {
    return UIUtil.getIcon(userPresence, IdeTalkCoreIcons.IdeTalk.Jabber, IdeTalkCoreIcons.IdeTalk.Jabber_dnd);
  }

  @Override
  public boolean isSelf(User user) {
    return myFacade.isConnectedAndAuthenticated() && getSimpleId(myFacade.getConnection().getUser()).equals(user.getName());
  }

  @Override
  public String[] getProjects(User user) {
    return ArrayUtilRt.EMPTY_STRING_ARRAY;
  }

  @Override
  @Nullable
  public String getAddressString(User user) {
    return null;
  }

  @Override
  public synchronized void sendXmlMessage(User user, final XmlMessage xmlMessage) {
    if (!myUI.connectAndLogin(null)) {
      return;
    }

    final String threadId = getThreadId(user);
    final PacketCollector packetCollector = myFacade.getConnection().createPacketCollector(new ThreadFilter(threadId));
    doSendMessage(xmlMessage, user, threadId);

    if (xmlMessage.needsResponse()) {
      final Runnable responseWaiterRunnable = () -> {
        try {
          processResponse(xmlMessage, packetCollector);
        }
        finally {
          packetCollector.cancel();
        }
      };
      myIdeFacade.runOnPooledThread(responseWaiterRunnable);
    }
    else {
      packetCollector.cancel();
    }
  }

  String getThreadId(User user) {
    String id = myUser2Thread.get(user.getName());
    if (id == null) {
      id = myThreadIdPrefix + myCurrentThreadId ++;
      myUser2Thread.put(user.getName(), id);
    }
    return id;
  }

  @Override
  public void setOwnPresence(UserPresence userPresence) {
    if (isOnline() && !userPresence.isOnline()) {
      myFacade.disconnect();
    }
    else if (!isOnline() && userPresence.isOnline()) {
      myUI.connectAndLogin(null);
    }

    if (isOnline() && presenceModeChanged(userPresence.getPresenceMode())) {
      myFacade.setOnlinePresence(userPresence);
      myPresenceMode = userPresence.getPresenceMode();
    }
  }


  @Override
  public boolean hasIdeTalkClient(User user) {
    return myIDEtalkUsers.contains(user.getName());
  }

  private boolean presenceModeChanged(PresenceMode presenceMode) {
    return myPresenceMode == null || myPresenceMode != presenceMode;
  }

  private static void processResponse(XmlMessage xmlMessage, PacketCollector collector) {
    boolean gotResponse = false;

    while (!gotResponse) {

      Message response = (Message) collector.nextResult(RESPONSE_TIMEOUT);
      if (response == null) break;

      final Collection<PacketExtension> extensions = response.getExtensions();
      for (PacketExtension o : extensions) {
        if (o instanceof JDOMExtension) {
          JDOMExtension extension = (JDOMExtension) o;
          if (RESPONSE.equals(extension.getElement().getName())) {
            xmlMessage.processResponse(extension.getElement());
            gotResponse = true;
            break;
          }
        }
      }
    }
  }

  private void doSendMessage(XmlMessage xmlMessage, User user, String threadId) {
    Element element = new Element(xmlMessage.getTagName(), xmlMessage.getTagNamespace());
    xmlMessage.fillRequest(element);

    Message message = createBaseMessage(user, element.getText());
    message.setThread(threadId);
    message.addExtension(new JDOMExtension(element));
    myFacade.getConnection().sendPacket(message);
  }

  static Message createBaseMessage(User user, String message) {
    Message msg = new Message(user.getName(), Message.Type.CHAT);
    msg.setBody(message);
    return msg;
  }

  @Override
  public void connected(XMPPConnection connection) {
    LOG.info("Jabber connected");
    if (mySubscribeListener == null) {
      mySubscribeListener = new MySubscribeListener();
      connection.addPacketListener(mySubscribeListener, new PacketTypeFilter(Presence.class));
    }
    if (myMessageListener == null) {
      myMessageListener = new MyMessageListener();
      connection.addPacketListener(myMessageListener, new PacketTypeFilter(Message.class));
    }
  }

  @Override
  public void authenticated() {
    LOG.info("Jabber authenticated: " + myFacade.getConnection().getUser());
    if (myRosterListener == null) {
      myRosterListener = new MyRosterListener();
      getRoster().addRosterListener(myRosterListener);
    }
    myUserFinder.registerForProject(myFacade.getMyAccount().getJabberId());

    if (!hasJabberContacts()) {
      synchronizeRoster(false);
    }
  }

  private boolean hasJabberContacts() {
    User[] users = myUserModel.getAllUsers();
    for (User user : users) {
      if (user.getTransportCode().equals(getName())) return true;
    }
    return false;
  }

  @Override
  public void disconnected(boolean onError) {
    final XMPPConnection connection = myFacade.getConnection();

    LOG.info("Jabber disconnected: " + connection.getUser());
    connection.removePacketListener(mySubscribeListener);
    mySubscribeListener = null;
    connection.removePacketListener(myMessageListener);
    myMessageListener = null;

    final Roster roster = connection.getRoster();
    if (roster != null) {
      roster.removeRosterListener(myRosterListener);
    }
    myRosterListener = null;

    myIDEtalkUsers.clear();
    myUser2Presence.clear();
    myUser2Thread.clear();

    if (onError && reconnectEnabledAndNotStarted()) {
      LOG.warn(getMsg("jabber.server.was.disconnected", myReconnectTimeout / 1000));
      myReconnectProcess = myIdeFacade.runOnPooledThread(new MyReconnectRunnable());
    }
  }

  private boolean reconnectEnabledAndNotStarted() {
    return (myReconnectProcess == null || myReconnectProcess.isDone()) && myReconnectTimeout >= 0;
  }

  @Override
  public void dispose() {
    getBroadcaster().removeListener(myUserModelListener);
    myFacade.removeConnectionListener(this);
  }

  private void updateUserPresence(String jabberId) {
    LOG.debug("Presence changed for " + jabberId);
    final User user = myUserModel.findUser(getSimpleId(jabberId), getName());
    if (user != null) {
      updateIsIDEtalkClient(jabberId, user);

      final UserPresence presence = _getUserPresence(user);
      IDEtalkEvent event = createPresenceChangeEvent(user, presence);
      if (event != null) {
        getBroadcaster().doChange(event, () -> myUser2Presence.put(user, presence));
      }
    }
  }

  private void updateIsIDEtalkClient(String jabberId, User user) {
    if (StringUtil.toLowerCase(getResource(jabberId)).startsWith(StringUtil.toLowerCase(JabberFacade.IDETALK_RESOURCE))) {
      myIDEtalkUsers.add(user.getName());
    } else {
      myIDEtalkUsers.remove(user.getName());
    }
  }

  @Nullable
  private IDEtalkEvent createPresenceChangeEvent(User user, UserPresence newPresence) {
    UserPresence oldPresence = getUserPresence(user);
    if (!newPresence.equals(oldPresence)) {
      if (newPresence.isOnline() ^ oldPresence.isOnline()) {
        return newPresence.isOnline() ? new UserEvent.Online(user) : new UserEvent.Offline(user);
      }
      else {
        return new UserEvent.Updated(user, PRESENCE, oldPresence.getPresenceMode(), newPresence.getPresenceMode());
      }
    }
    return null;
  }

  private void updateJabberUsers(boolean removeUsersNotInRoster) {
    LOG.debug("Roster changed - update user model");
    Set<User> currentUsers = ContainerUtil.set(myUserModel.getAllUsers());
    for (RosterEntry rosterEntry : getRoster().getEntries()) {
      User user = addJabberUserToUserModelOrUpdateInfo(rosterEntry);
      currentUsers.remove(user);
    }

    if (removeUsersNotInRoster) {
      removeUsers(currentUsers);
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Roster synchronized: " +Arrays.asList(myUserModel.getAllUsers()));
    }
  }

  private void removeUsers(Set<User> currentUsers) {
    for (User user : currentUsers) {
      myUserModel.removeUser(user);
    }
  }

  private User addJabberUserToUserModelOrUpdateInfo(RosterEntry rosterEntry) {
//    System.out.println("rosterEntry.getName() = " + rosterEntry.getName());
//    System.out.println("rosterEntry.getUser() = " + rosterEntry.getUser());
    User user = myUserModel.createUser(getSimpleId(rosterEntry.getUser()), getName());
    String newGroup = getUserGroup(rosterEntry);
    if (newGroup != null) {
      user.setGroup(newGroup, myUserModel);
    }
    user.setDisplayName(rosterEntry.getName(), myUserModel);
    myUserModel.addUser(user);
    String jabberId = getCurrentJabberID(user, rosterEntry);
    updateIsIDEtalkClient(jabberId, user);
    return user;
  }

  private String getCurrentJabberID(User user, RosterEntry rosterEntry) {
    Presence presence = _getPresence(user);
    String jabberId = null;
    if (presence != null) {
      jabberId = presence.getFrom();
    }
    if (jabberId == null) jabberId = rosterEntry.getUser();
    if (jabberId == null) jabberId = rosterEntry.getName();
    return jabberId;
  }

  static String getResource(String userName) {
    int lastSlash = userName.indexOf('/');
    if (lastSlash != -1) {
      return userName.substring(lastSlash + 1);
    }
    return "";
  }

  static String getSimpleId(String userName) {
    String id = userName;
    int lastSlash = id.indexOf('/');
    if (lastSlash != -1) {
      id = id.substring(0, lastSlash);
    }
    return id;
  }

  @Nullable
  private static String getUserGroup(RosterEntry rosterEntry) {
    String group = null;
    for (RosterGroup rosterGroup : rosterEntry.getGroups()) {
      group = rosterGroup.getName();
    }
    return group;
  }

  private Roster getRoster() {
    final Roster roster = myFacade.getConnection().getRoster();
    assert roster != null;
    return roster;
  }

  public JabberFacade getFacade() {
    return myFacade;
  }

  boolean isUserInMyContactListAndActive(String userName) {
    User user = myUserModel.findUser(getSimpleId(userName), getName());
    return user != null && user.isOnline();
  }

  @Nullable
  private Presence _getPresence(User user) {
    if (!isOnline()) return null;
    return getRoster().getPresence(user.getName());
  }

  private User self() {
    return myUserModel.createUser(myFacade.getMyAccount().getJabberId(), getName());
  }

  public static JabberTransport getInstance() {
    return (JabberTransport) Pico.getInstance().getComponentInstanceOfType(JabberTransport.class);
  }

  public void synchronizeRoster(boolean removeUsersNotInRoster) {
    updateJabberUsers(removeUsersNotInRoster);
  }

  public void runIngnoringUserEvents(Runnable runnable) {
    try {
      myIgnoreUserEvents = true;
      runnable.run();
    } finally {
      myIgnoreUserEvents = false;
    }
  }

  /** -1 disables reconnect */
  public void setReconnectTimeout(int milliseconds) {
    myReconnectTimeout = milliseconds;
  }

  private class MyRosterListener implements RosterListener {
    @Override
    public void entriesAdded(Collection addresses) {
      updateJabberUsers(false);
    }

    @Override
    public void entriesUpdated(Collection addresses) {
      updateJabberUsers(false);
    }

    @Override
    public void entriesDeleted(Collection addresses) {
      updateJabberUsers(false);
    }

    @Override
    public void presenceChanged(final String string) {
      updateUserPresence(string);
    }
  }

  private class MyUserModelListener extends TransportUserListener {

    MyUserModelListener() {
      super(JabberTransport.this);
    }

    @Override
    protected void processBeforeChange(UserEvent event) {
      super.processBeforeChange(event);
      event.accept(new EventVisitor() {
        @Override
        public void visitUserAdded(UserEvent.Added event) {
          event.getUser().setCanAccessMyFiles(false, myUserModel);
        }
      });
    }

    @Override
    protected void processAfterChange(UserEvent event) {
      if (myIgnoreUserEvents) return;

      event.accept(new EventVisitor() {
        @Override public void visitUserRemoved(UserEvent.Removed event) {
          synchronizeWithJabberIfPossible(event);
        }

        @Override public void visitUserUpdated(UserEvent.Updated event) {
          if (GROUP.equals(event.getPropertyName()) ||
              DISPLAY_NAME.equals(event.getPropertyName())) {
            synchronizeWithJabberIfPossible(event);
          }
        }
      });
    }

    private void synchronizeWithJabberIfPossible(UserEvent event) {
      if (event.getUser().getTransportCode().equals(getName()) &&
          myFacade.isConnectedAndAuthenticated()) {
        myDispatcher.sendNow(self(), new JabberSyncUserMessage(event));
      }
    }
  }

  private class MySubscribeListener implements PacketListener {
    @Override
    public void processPacket(Packet packet) {
      final Presence presence = ((Presence) packet);
      if (presence.getType() != Presence.Type.subscribe) return;
      LOG.info("Subscribe request from " + presence.getFrom());

      if (myIgnoreList.isIgnored(presence.getFrom())) {
        LOG.info(presence.getFrom() + " in ignore list");
        return;
      }

      if (isUserInMyContactListAndActive(presence.getFrom()) || Pico.isUnitTest()) {
        acceptSubscription(presence, true);
        return;
      }

      UIUtil.invokeLater(() -> acceptSubscription(presence, myUI.shouldAcceptSubscriptionRequest(presence)));
    }

    private void acceptSubscription(final Presence presence, boolean subscribe) {
      if (!isOnline()) return;

      myFacade.changeSubscription(presence.getFrom(), subscribe);

      if (subscribe) {
        String from = getSimpleId(presence.getFrom());
        LOG.info("Add " + from + " to the roster");

        try {
          getRoster().createEntry(from, from, new String[]{UserModel.DEFAULT_GROUP});
        } catch (XMPPException e) {
          LOG.warn(e);
        }
      }

    }
  }

  private class MyMessageListener implements PacketListener {

    @Override
    public void processPacket(Packet packet) {
      try {
        doProcessPacket(packet);
      }
      catch(Throwable e) {
        LOG.error(e.getMessage(), e);
      }
    }

    private void doProcessPacket(Packet packet) {
      final Message message = ((Message) packet);
      if (message.getType() == Message.Type.ERROR) {
        UIUtil.invokeLater(() -> {
          String from = (message.getFrom() != null) ? getMsg("from.0.lf", message.getFrom()) : "";
          LOG.warn(getMsg("jabber.error.text", from,
              (message.getError() == null ? "N/A" : message.getError().toString())));
        });
        return;
      }

      if (myIgnoreList.isIgnored(packet.getFrom())) {
        return;
      }

      Element element = null;
      for (PacketExtension o : message.getExtensions()) {
        if (o instanceof JDOMExtension) {
          element = ((JDOMExtension) o).getElement();
        }
      }

      if (element != null && !RESPONSE.equals(element.getName())) {
        processAndSendResponse(element, message);
      }
      else if (element == null && message.getBody() != null) {
        // Some simple Jabber Message
        MessageEvent event = EventFactory.createMessageEvent(JabberTransport.this, getFrom(message), message.getBody());
        if (message.getThread() != null) {
          myUser2Thread.put(getFrom(message), message.getThread());
        }

        getBroadcaster().fireEvent(event);
      }
    }

    private void processAndSendResponse(Element element, Message message) {
      Element response = new Element(RESPONSE, Transport.NAMESPACE);
      XmlResponseProvider provider = XmlResponseProvider.getProvider(element, getBroadcaster());
      if (provider.processAndFillResponse(response, element, JabberTransport.this, getFrom(message))) {
        Message responseMessage = new Message(getFrom(message));
        responseMessage.addExtension(new JDOMExtension(response));
        responseMessage.setThread(message.getThread());
        myFacade.getConnection().sendPacket(responseMessage);
      }
    }

    private String getFrom(Message message) {
      return getSimpleId(message.getFrom());
    }
  }

  private class MyReconnectRunnable implements Runnable {
    @Override
    public void run() {
      try {
        Thread.sleep(myReconnectTimeout);

        if (myFacade.connect() != null && myFacade.getMyAccount().isLoginAllowed()) {
          myReconnectProcess = myIdeFacade.runOnPooledThread(this);
        }
      } catch (InterruptedException ignored) {
        // return
      }
    }
  }
}
