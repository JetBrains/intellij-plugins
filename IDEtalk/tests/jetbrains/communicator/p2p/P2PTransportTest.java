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

import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.dispatcher.AsyncMessageDispatcherImpl;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.impl.users.UserModelImpl;
import jetbrains.communicator.core.users.*;
import jetbrains.communicator.ide.NullProgressIndicator;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockTransport;
import jetbrains.communicator.mock.MockUser;
import jetbrains.communicator.p2p.commands.AddOnlineUserP2PCommand;
import jetbrains.communicator.util.CommunicatorStrings;
import jetbrains.communicator.util.WaitFor;
import org.picocontainer.MutablePicoContainer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

/**
 * @author Kir Maximov
 *
 */
public class P2PTransportTest extends BaseTestCase {
  private P2PTransport myTransport;
  private String myLog;
  private MockIDEFacade myIdeFacade;
  private UserModelImpl myUserModel;
  public static final String PROJECT_NAME = "project234";
  public static final int WAIT_USER_RESPONSES_TIMEOUT = 500;
  protected AsyncMessageDispatcherImpl myDispatcher;
  private MutablePicoContainer myProjectLevelContainer;

  public P2PTransportTest(String s) {
    super(s);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myUserModel = new UserModelImpl(getBroadcaster());
    disposeOnTearDown(myUserModel);
    myIdeFacade = new MockIDEFacade(getClass());
    myIdeFacade.setReturnedProjects(new String[]{PROJECT_NAME});

    myDispatcher = new AsyncMessageDispatcherImpl(getBroadcaster(), myIdeFacade);
    disposeOnTearDown(myDispatcher);
    myTransport = new P2PTransport(myDispatcher, myUserModel, WAIT_USER_RESPONSES_TIMEOUT) {
      @Override
      protected void sendUserAddedCallback(User user) {
        super.sendUserAddedCallback(user);
        myLog += "sendUserAddedCallback" + user;
      }
    };
    Pico.getInstance().registerComponentInstance(myTransport);

    myLog = "";
    myProjectLevelContainer = Pico.getInstance().makeChildContainer();
  }

  @Override
  protected void tearDown() throws Exception {
    if (myTransport != null) {
      myTransport.dispose();
      Pico.getInstance().unregisterComponentByInstance(myTransport);
    }

    CommunicatorStrings.setMyUsername(null);
    super.tearDown();
  }

  public void testFindUsers() {
    User[] users = myTransport.findUsers(new NullProgressIndicator());
    assertTrue("At least self should be found", users.length >= 1);
    User self = null;
    for (User user : users) {
      if (user.getName().equals(CommunicatorStrings.getMyUsername())) {
        self = user;
        break;
      }
    }
    assertNotNull("Self user not found in " + Arrays.asList(users), self);
    assertTrue("Self should be online", self.isOnline());
    InetAddress address = myTransport.getAddress(self);
    assertTrue("Self address is expected:" + address, NetworkUtil.isOwnAddress(address));
    assertEquals("Projects should be set", PROJECT_NAME, self.getProjects()[0]);
  }

  public void testSetOwnPresence() {

    registerResponseProviders(myUserModel, myIdeFacade);

    // Add self to contact list
    User self = myUserModel.createUser(CommunicatorStrings.getMyUsername(), myTransport.getName());
    myUserModel.addUser(self);

    // Wait for next cycle of user finding
    new WaitFor(1000) { @Override
                        protected boolean condition() { return !myTransport.getUserMonitorThread().isFinding(); } };

    // make self away
    UserPresence presence = new UserPresence(PresenceMode.AWAY);
    assert presence.isOnline();
    myTransport.setOwnPresence(presence);

    // make sure that away status will be updated
    myTransport.findUsers(new NullProgressIndicator());
    assert PresenceMode.AWAY == self.getPresence().getPresenceMode();

    // Now, test itself. We go online and want this status to be updated almost immediately.
    myEvents.clear();
    addEventListener();
    myTransport.setOwnPresence(new UserPresence(PresenceMode.AVAILABLE));

    final User self1 = self;
    new WaitFor(200) {
      @Override
      protected boolean condition() {
        return self1.getPresence().getPresenceMode() == PresenceMode.AVAILABLE;
      }
    };
    assertSame("Should provide correct presence mode", PresenceMode.AVAILABLE,
        myTransport.getUserPresence(self).getPresenceMode());
    UserEvent.Updated event = (UserEvent.Updated) checkEvent(true);
    assertNotNull("Expect go online event", event);
    assertEquals("presence", event.getPropertyName());
  }

  public void testInitialize_OurProject() throws Throwable {
    initializeProject(PROJECT_NAME);
    User[] users = myUserModel.getAllUsers();
    assertTrue("At least self should be found/added", users.length > 0);
    assertEquals("Project name should become group", PROJECT_NAME, users[0].getGroup());
    assertEquals("One group expected", 1, myUserModel.getGroups().length);
  }

  private User createSelf() throws UnknownHostException {
    return myTransport.createUser(CommunicatorStrings.getMyUsername(), new OnlineUserInfo(InetAddress.getLocalHost(), myTransport.getPort()));
  }

  public void testInitialize_ExistsInOtherGroup() throws Throwable {
    User self = createSelf();
    myUserModel.addUser(self);
    myUserModel.addGroup(PROJECT_NAME);
    self.setGroup("A group", myUserModel);

    initializeProject(PROJECT_NAME);
    User[] users = myUserModel.getAllUsers();
    assertEquals("Group should be kept", "A group", users[0].getGroup());
  }

  private void initializeProject(String projectName) throws InterruptedException {
    myTransport.initializeProject(projectName, myProjectLevelContainer);
    Thread.sleep(WAIT_USER_RESPONSES_TIMEOUT *5);
  }

  public void testInitialize_WrongProject() throws Throwable {
    initializeProject("Bad_project");
    User[] users = myUserModel.getAllUsers();
    assertEquals("No one from Bad_project should be found/added", 0, users.length);
    assertEquals("No groups should be added", 0, myUserModel.getGroups().length);
  }

  public void testCanAddUsers() {
    myUserModel.addGroup("Fabrique");

    assertFalse("No users", myTransport.canAddUsers("project", new HashSet<>()));
    assertTrue("Group already exists - can add new users", myTransport.canAddUsers("Fabrique", Arrays.asList(user("foo"), user("bar"))));

    assertTrue("Group not exists - new users to new project",
        myTransport.canAddUsers("IDEA", Arrays.asList(user("foo"), user("bar"))));
    User user = user("foo");
    myUserModel.addUser(user);
    user.setGroup(UserModel.DEFAULT_GROUP, myUserModel);

    assertFalse("Already has added user",
        myTransport.canAddUsers("IDEA", Arrays.asList(user("foo"), user("bar"))));
  }

  private User user(String s) {
    return new MockUser(s, null);
  }

  public void testIsOnline() throws Exception {

    User mockUser = myTransport.createUser("mock", new OnlineUserInfo(InetAddress.getLocalHost(), myTransport.getPort()));


    assertFalse(mockUser.isOnline());
    myTransport.setOnlineUsers(Arrays.asList(mockUser));

    assertTrue("should be online now", mockUser.isOnline());

    assertTrue(UserImpl.create("mock", P2PTransport.CODE).isOnline());
  }

  public void testGetAddress() {

    User mockUser = UserImpl.create("mock", P2PTransport.CODE);
    assertNull("Sanity check", myTransport.getAddress(mockUser));

    new AddOnlineUserP2PCommand(myTransport.getUserMonitorThread())
        .addOnlineUser("localhost", "mock", 3354, null, null);

    myTransport.flushCurrentUsers();
    assertEquals("Inet address should be set from available online user",
        "localhost", myTransport.getAddress(mockUser).getHostName());

    myTransport.setOnlineUsers(new HashSet<>());
    assertNull("Inet address should be reset", myTransport.getAddress(mockUser));
  }


  public void testGetProjects() {
    User mockUser = UserImpl.create("mock", P2PTransport.CODE);
    assertEquals("Sanity check", 0, myTransport.getProjects(mockUser).length);

    Vector<String> projects = new Vector<>(Arrays.asList("project1", "project2"));
    new AddOnlineUserP2PCommand(myTransport.getUserMonitorThread())
        .addOnlineUser("localhost", "mock", 0, projects, null);

    myTransport.flushCurrentUsers();
    assertEquals("Projects should be set from available online user",
        projects.toString(), Arrays.asList(myTransport.getProjects(mockUser)).toString());

    myTransport.setOnlineUsers(new HashSet<>());
    assertEquals("Projects should be reset", 0, myTransport.getProjects(mockUser).length);
  }

  public void testGetPort() {

    User mockUser = UserImpl.create("mock", P2PTransport.CODE);
    assertEquals("Sanity check", -1, myTransport.getPort(mockUser));

    new AddOnlineUserP2PCommand(myTransport.getUserMonitorThread())
        .addOnlineUser("localhost", "mock", 3354, null, null);

    myTransport.flushCurrentUsers();
    assertEquals("Port should be set from available online user",
        3354, myTransport.getPort(mockUser));

    myTransport.setOnlineUsers(new HashSet<>());
    assertEquals("Port should be reset", -1, myTransport.getPort(mockUser));
  }

  public void testGetUserPresence() {
    User mockUser = UserImpl.create("mock", P2PTransport.CODE);
    assertEquals("Sanity check", PresenceMode.UNAVAILABLE, myTransport.getUserPresence(mockUser).getPresenceMode());

    new AddOnlineUserP2PCommand(myTransport.getUserMonitorThread())
        .addOnlineUser("localhost", "mock", 3354, null, new UserPresence(PresenceMode.DND).toVector());

    myTransport.flushCurrentUsers();
    assertEquals("User status should be set from available online user",
        PresenceMode.DND, myTransport.getUserPresence(mockUser).getPresenceMode());

    myTransport.setOnlineUsers(new HashSet<>());
    assertEquals("Presence should be reset", PresenceMode.UNAVAILABLE, myTransport.getUserPresence(mockUser).getPresenceMode());
  }

  public void testOnUserAdd() {
    User mockUser = UserImpl.create("user", myTransport.getName());
    getBroadcaster().fireEvent(new UserEvent.Added(mockUser));

    assertEquals("Should send notification", "sendUserAddedCallback" + mockUser, myLog);
  }

  public void testOnUserAdd_AnotherTransport() {
    User mockUser = UserImpl.create("user", MockTransport.NAME);
    getBroadcaster().fireEvent(new UserEvent.Added(mockUser));

    assertEquals("Should not send notification for another transport:" + myLog, "", myLog);
  }

  public void testSetOnlineUsers_UpdateEvent() {
    myTransport.setOnlineUsers(new HashSet<>());

    User user = myTransport.createUser("someUser", new OnlineUserInfo(null, 0, null, new UserPresence(PresenceMode.AWAY)));
    myTransport.setOnlineUsers(Collections.singleton(user));

    addEventListener();

    user = myTransport.createUser("someUser", new OnlineUserInfo(null, 0, null, new UserPresence(PresenceMode.AWAY)));
    myTransport.setOnlineUsers(Collections.singleton(user));

    assertEquals("Same user data, no events expected: " + myEvents, 0, myEvents.size());

    user = myTransport.createUser("someUser", new OnlineUserInfo(null, 0, null, new UserPresence(PresenceMode.EXTENDED_AWAY)));
    myTransport.setOnlineUsers(Collections.singleton(user));

    UserEvent.Updated event = (UserEvent.Updated) checkEvent(true);
    assertEquals("Precense changed: " + myEvents, "presence", event.getPropertyName());
  }

  public void testSetOnlineUsers_OnlineEvent() throws Exception {
    addEventListener();

    // Set none users
    myTransport.setOnlineUsers(new HashSet<>());
    assertEquals("No online users, no change expected", 0, myEvents.size());

    // Got one online user
    User bill = myTransport.createUser("bill", new OnlineUserInfo(InetAddress.getLocalHost(), myTransport.getPort()));
    myEvents.clear();
    myTransport.setOnlineUsers(Arrays.asList(bill));

    assertEquals("Online status for bill should have changed:" + myEvents, 1, myEvents.size());
    checkEvent(bill, 0, true);

    myEvents.clear();

    User john = myTransport.createUser("john", new OnlineUserInfo(InetAddress.getLocalHost(), myTransport.getPort()));
    myTransport.setOnlineUsers(Arrays.asList(john));
    assertTrue(john.isOnline());
    assertTrue(john.getPresence().isOnline());
    assertFalse(bill.isOnline());

    assertEquals("Online status for bill and john should have changed: " + myEvents, 2, myEvents.size());

    checkEvent(bill, 0, false);
    checkEvent(john, 1, true);

    myEvents.clear();

    myTransport.setOnlineUsers(Arrays.asList(myTransport.createUser("john", new OnlineUserInfo(InetAddress.getLocalHost(), myTransport.getPort()))));

    assertEquals("Online status didn't change - no events expected: " + myEvents, 0, myEvents.size());
    assertEquals("Address should have changed", InetAddress.getLocalHost(), myTransport.getAddress(john));
  }


  private void checkEvent(User affectedUser, int eventId, boolean becomeOnline) {
    assertEquals(affectedUser, evt(eventId).getUser());
    assertEquals("online", evt(eventId).getPropertyName());
    assertEquals(!becomeOnline, ((Boolean) evt(eventId).getOldValue()).booleanValue());
    assertEquals(becomeOnline, ((Boolean) evt(eventId).getNewValue()).booleanValue());
  }

  private UserEvent.Updated evt(int i) {
    return (UserEvent.Updated) myEvents.get(i);
  }

}
