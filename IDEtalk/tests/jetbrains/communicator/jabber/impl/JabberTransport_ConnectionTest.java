/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package jetbrains.communicator.jabber.impl;

import icons.IdeTalkCoreIcons;
import jetbrains.communicator.AbstractTransportTestCase;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.transport.MessageEvent;
import jetbrains.communicator.core.transport.TextXmlMessage;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.users.PresenceMode;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.ProgressIndicator;
import jetbrains.communicator.jabber.JabberUI;
import jetbrains.communicator.jabber.JabberUserFinder;
import jetbrains.communicator.util.WaitFor;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jmock.Mock;
import org.jmock.core.Invocation;
import org.jmock.core.stub.ReturnStub;
import org.picocontainer.Disposable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Kir
 */
@SuppressWarnings({"SSBasedInspection"})
public class JabberTransport_ConnectionTest extends AbstractTransportTestCase {
  private JabberFacadeImpl myFacade;

  public static final String USER = "someuser";
  public static final String PASSWORD = "somePassword";
  public static final String LOCALHOST = "localhost";
  private String myUser;
  private JabberTransport myTransport;
  public static final String FRED = "fred@localhost";
  private MockUserFinder myUserFinder;
  public static final int TIMEOUT = 6000;
  private boolean myUsersSynchronized;

  public static Test suite() {
    TestSuite testSuite = new TestSuite();
    XMPPConnection ourConnection;
    try {
      ourConnection = new XMPPConnection(LOCALHOST);
    } catch (XMPPException e) {
      return testSuite;
    }
    ourConnection.close();

    testSuite.addTestSuite(JabberTransport_ConnectionTest.class);
    return testSuite;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    //XMPPConnection.DEBUG_ENABLED = true;

    assertEquals("No connection yet", "", myUserFinder.getLog());
    myUser = USER + System.currentTimeMillis();
    addEventListener();

    assertNull(createGoodAccount());

    Thread.sleep(120);
    new WaitFor(500) {
      @Override
      protected boolean condition() {
        return myEvents.size() > 0;
      }
    };

    myTransport.synchronizeRoster(true);
    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return myUserModel.getAllUsers().length == 0;
      }
    };
    myEvents.clear();
    myUsersSynchronized = false;

    mySelf = createSelf();
  }

  @Override
  protected Transport createTransport() {
    myFacade = new JabberFacadeImpl(myIdeFacade);
    myUserFinder = new MockUserFinder();

    Mock mockUI = createJabberUIMock();
    myTransport = new JabberTransport((JabberUI) mockUI.proxy(), myFacade, myUserModel, myDispatcher, myUserFinder) {
      @Override
      public void synchronizeRoster(boolean removeUsersNotInRoster) {
        super.synchronizeRoster(removeUsersNotInRoster);
        myUsersSynchronized = true;
      }
    };
    Pico.getInstance().registerComponentInstance(myTransport);

    disposeOnTearDown(myFacade);
    disposeOnTearDown(myTransport);

    return myTransport;
  }

  private Mock createJabberUIMock() {
    Mock mockUI = mock(JabberUI.class);
    mockUI.stubs().method("connectAndLogin").will(new ReturnStub(Boolean.TRUE){
      @Override
      public Object invoke(Invocation invocation) throws Throwable {
        myFacade.connect();
        return super.invoke(invocation);
      }
    });
    return mockUI;
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      myFacade.getConnection().getAccountManager().deleteAccount();
    } catch (Throwable e) {
      // Harmless:
      // e.printStackTrace();
    }

    super.tearDown();
  }

  private String createGoodAccount() {
    return myFacade.createAccountAndConnect(myUser, PASSWORD,
        LOCALHOST, myFacade.getMyAccount().getPort(), false);
  }

  public void testSendProjectIdOnConnect() {
    assertEquals("Should invoke registerForProject", "registerForProject" + myUser + "@" + LOCALHOST,
        myUserFinder.getLog());
  }

  public void testAddUser_NotIdeTalk() {
    addEventListener();
    addUserFred();

    IDEtalkEvent event = checkEvent(false);
    assertTrue(event.toString(), event instanceof UserEvent.Added);
    User user = ((UserEvent.Added) event).getUser();
    assertEquals("Jabber user expected", "Jabber", user.getTransportCode());
    assertEquals("Should add Jabber user", FRED, user.getName());
    assertEquals("Wrong group", "aGroup", user.getGroup());

    assertFalse(user.hasIDEtalkClient());
  }

  public void testAddUser_IdeTalk() throws Throwable {
    XMPPConnection conn = createLocalConnectionWithJabberUser("fred" + System.nanoTime(), JabberFacadeImpl.IDETALK_RESOURCE);

    addEventListener();
    final String fred = getUser(conn);
    addUser(fred);

    IDEtalkEvent event = checkEvent(false);
    assertTrue(event.toString(), event instanceof UserEvent.Added);
    User user = ((UserEvent.Added) event).getUser();
    assertEquals("Jabber user expected", "Jabber", user.getTransportCode());
    assertEquals("Should add Jabber user", fred, user.getName());
    assertEquals("Wrong group", "aGroup", user.getGroup());

    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return myFacade.getConnection().getRoster().getPresence(fred) != null;
      }
    };
    assertTrue("User with IDEtalk resource", user.hasIDEtalkClient());
  }

  private void addUserFred() {
    addUser(FRED);
  }

  private void addUser(String userId) {
    myFacade.addUsers("aGroup", Arrays.asList(userId));

    waitForEvent();
  }

  private void waitForEvent() {
    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return myEvents.size() > 0;
      }
    };
  }

/*
  public void testUserRemovedFromRoster() throws Throwable {
    addEventListener();

    myFacade.addUsers("aGroup", Arrays.asList(new String[]{"jonny@localhost"}));
    waitForEvent();
    myEvents.clear();

    addUserFred();
    myEvents.clear();

    Roster roster = myFacade.getConnection().getRoster();
    roster.removeEntry(roster.getEntry(FRED));

    waitForEvent();
    UserEvent.Removed removedEvent = ((UserEvent.Removed) checkEvent(true));
    assertEquals("Fred should be removed", FRED, removedEvent.getUser().getName());
    assertEquals("Jonny should remain in UserModel", 1, myUserModel.getAllUsers().length);
    assertEquals("jonny@localhost", myUserModel.getAllUsers()[0].getName());
  }
*/

  public void testUserInRosterChangedGroup() throws Throwable {
    addEventListener();
    addUserFred();
    myEvents.clear();

    Roster roster = myFacade.getConnection().getRoster();
    RosterEntry fredEntry = roster.getEntry(FRED);

    UserEvent.Updated updated;
    myEvents.clear();
    roster.getGroup("aGroup").removeEntry(fredEntry);
    RosterGroup group = roster.createGroup("aaasss");
    group.addEntry(fredEntry);
    assertTrue(group.contains(fredEntry.getUser()));

    waitForEvent();
    updated = ((UserEvent.Updated) checkEvent(true));
    assertEquals("group", updated.getPropertyName());
    assertEquals("aGroup", updated.getOldValue());
    assertEquals("aaasss", updated.getNewValue());
  }

  public void testUserInRosterChangedName() {
    addEventListener();
    addUserFred();
    myEvents.clear();

    Roster roster = myFacade.getConnection().getRoster();
    RosterEntry fredEntry = roster.getEntry(FRED);
    fredEntry.setName("Some New Fred Name");

    waitForEvent();
    UserEvent.Updated updated = ((UserEvent.Updated) checkEvent(true));
    assertEquals("displayName", updated.getPropertyName());
    assertEquals(FRED, updated.getOldValue());
    assertEquals("Some New Fred Name", updated.getNewValue());
  }

  public void testLocalChanges_UpdateRoster() {
    addEventListener();
    addUserFred();

    User user = myUserModel.getAllUsers()[0];
    final Roster roster = myFacade.getConnection().getRoster();

    // Change group:
    user.setGroup("someOtherGroup", myUserModel);
    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return roster.getGroupCount() == 1 && roster.getGroup("someOtherGroup") != null;
      }
    };

    assertEquals("One group expected", 1, roster.getGroupCount());
    RosterGroup group = roster.getGroup("someOtherGroup");
    assertNotNull(group);
    assertEquals(1, group.getEntryCount());
    assertNotNull(group.getEntry(FRED));

    // Change Display name:
    user.setDisplayName("Some new name", myUserModel);
    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return "Some new name".equals(roster.getEntry(FRED).getName());
      }
    };
    assertEquals("User name in Roster should have changed", "Some new name", roster.getEntry(FRED).getName());

    // Delete user:
    myUserModel.removeUser(user);
    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return roster.getEntryCount() == 0;
      }
    };
    assertEquals("User should be removed from Roster", 0, roster.getEntryCount());
  }

  public void testDisconnectFromJabber() throws Exception {
    String userName = "bob" + System.currentTimeMillis();
    XMPPConnection contact = createLocalConnectionWithJabberUser(userName, null);

    myTransport.getFacade().addUsers("General", Arrays.asList(getUser(contact)));
    new WaitFor(TIMEOUT) {

      @Override
      protected boolean condition() {
        return 1 == myUserModel.getAllUsers().length && myUserModel.getAllUsers()[0].isOnline();
      }
    };
    assertEquals(1, myUserModel.getAllUsers().length);

    assertTrue(myUserModel.getAllUsers()[0].isOnline());

    myTransport.getFacade().disconnect();
    assertFalse("Jabber user should become unavailable", myUserModel.getAllUsers()[0].isOnline());
  }

  public void testUserOnlineOffline() throws Throwable {
    addEventListener();

    String userName = "bob" + System.currentTimeMillis();
    XMPPConnection contact = createLocalConnectionWithJabberUser(userName, null);

    Roster roster = myTransport.getFacade().getConnection().getRoster();
    String jabberUser = getUser(contact);

    final User bob = UserImpl.create(jabberUser, myTransport.getName());

    myEvents.clear();
    roster.createEntry(jabberUser, "Bob", new String[0]);
    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return myEvents.size() == 2;
      }
    };
    assertTrue("Bob should be online", bob.isOnline());
    assertEquals(IdeTalkCoreIcons.IdeTalk.Jabber, myTransport.getIcon(bob.getPresence()));

    assertEquals("One user in UserModel expected:" + Arrays.asList(myUserModel.getAllUsers()),
        1, myUserModel.getAllUsers().length);
    UserEvent.Online online = ((UserEvent.Online) myEvents.get(1));
    assertEquals("Online event expected", bob, online.getUser());

    myEvents.clear();

    Presence presence = new Presence(Presence.Type.unavailable);
    contact.sendPacket(presence);

    new WaitFor(TIMEOUT){
      @Override
      protected boolean condition() {
        return  myEvents.size() == 1;
      }
    };
    assertFalse("Should become offline", bob.isOnline());
    assertEquals(IdeTalkCoreIcons.IdeTalk.Offline, myTransport.getIcon(myTransport.getUserPresence(bob)));
    assertEquals("Bob should go offline", bob, ((UserEvent.Offline) checkEvent(true)).getUser());
  }

  public void testIgnoreList() throws Exception {
    setIgnoreList("erts\r\nsomeId");

    final XMPPConnection conn = createLocalConnectionWithJabberUser("aaasomeId" + System.nanoTime(), null);
    conn.getRoster().createEntry(selfJabberId(), "name", new String[0]);

    new WaitFor(2000) {
      @Override
      protected boolean condition() {
        return conn.getRoster().getEntry(selfJabberId()).getType() == RosterPacket.ItemType.BOTH;
      }
    };
    assertEquals("Should not subscribe user from ignore list", RosterPacket.ItemType.NONE, conn.getRoster().getEntry(selfJabberId()).getType());
  }

  private void setIgnoreList(String contents) throws IOException {
    File configDir = myIdeFacade.getConfigDir();
    File file = new File(configDir, "jabber.ignore.txt");
    FileWriter fw = new FileWriter(file);
    fw.write(contents);
    fw.close();
  }

  public void testSubscriptionConfirmation() throws Throwable {

    // In this test user bob will try to add me to his contact list.
    // This action will require our confirmation

    addEventListener();

    // Create bob
    String userName = "bob" + System.currentTimeMillis();
    final XMPPConnection bob = createLocalConnectionWithJabberUser(userName, null);

    // Set our account to require manual subscription and disconnect:
    myFacade.getConnection().getRoster().setSubscriptionMode(Roster.SubscriptionMode.manual);
    myFacade.getMyAccount().setRememberPassword(true);
    myFacade.saveSettings();
    myFacade.getConnection().close();

    // Bob tries to add me to his contact list
    myEvents.clear();
    final String selfName = myUser + "@" + getUser(bob).substring(userName.length() + 1);
    bob.getRoster().createEntry(selfName, "me", new String[]{"DDD"});

    // Now we connect. Bob should get us in his presence - auto confirmation of the subscription expected.
    // See JabberTransport:mySubscribeListener
    myFacade.connect();
    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return  null != bob.getRoster().getPresence(selfName);
      }
    };
    assertTrue("Sanity check", myFacade.isConnectedAndAuthenticated());

    Presence presence = bob.getRoster().getPresence(selfName);
    assertNotNull(presence);
    assertEquals("Should return good presence", Presence.Type.available,
        presence.getType() );

    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return myEvents.size() > 1;
      }
    };

    final String bobUserName = getUser(bob);

    assertEquals("We should get bob in our contact list as well", 1, myUserModel.getAllUsers().length);
    assertEquals("We should get bob in our contact list as well", bobUserName, myUserModel.getAllUsers()[0].getName());

    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return getBobEntry(bobUserName) != null && getBobEntry(bobUserName).getType() == RosterPacket.ItemType.BOTH;
      }
    };

    assertEquals("Should add bob as a contact too", RosterPacket.ItemType.BOTH, getBobEntry(bobUserName).getType());
  }

  public void testSyncronizeWithRosterWhileWaitingForSubscription() {
    User bob = myUserModel.createUser("bob@localhost", JabberTransport.CODE);
    myUserModel.addUser(bob);

    myTransport.synchronizeRoster(false);

    assertNotNull("Bob should not be deleted",
        myUserModel.findUser(bob.getName(), bob.getTransportCode()));
  }

  private RosterEntry getBobEntry(String bobUserName) {
    return myFacade.getConnection().getRoster().getEntry(bobUserName);
  }

  public void testIsSelf() {
    assertFalse(myTransport.isSelf(UserImpl.create("some@localhost", myTransport.getName())));
    assertTrue(myTransport.isSelf(UserImpl.create(selfJabberId(), myTransport.getName())));
  }

  public void testIsInMyContactList() throws Exception {
    String userName = "bob" + System.currentTimeMillis();
    XMPPConnection bob = createLocalConnectionWithJabberUser(userName, null);

    userName = getUser(bob);

    assertFalse("Not in my contact list", myTransport.isUserInMyContactListAndActive(userName));

    myFacade.addUsers("General", Collections.singletonList(userName));
    final String userName1 = userName;
    new WaitFor(TIMEOUT){
      @Override
      protected boolean condition() {
        return myTransport.isUserInMyContactListAndActive(userName1);
      }
    };

    assertTrue("In my contact list and active",
        myTransport.isUserInMyContactListAndActive(userName));
    assertTrue("In my contact list and active",
        myTransport.isUserInMyContactListAndActive(userName + "/somePath"));

    bob.close();
    assertFalse("In my contact list and not active",
        myTransport.isUserInMyContactListAndActive(userName));
  }

  private static String getUser(XMPPConnection bob) {
    return JabberTransport.getSimpleId(bob.getUser());
  }

  public void testSendMessage2NonExistingUser() {
    myFacade.addUsers("grp", Arrays.asList("some@fake.user"));
    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return myUserModel.getAllUsers().length > 0;
      }
    };

    myUserModel.getAllUsers()[0].sendMessage("some text", getBroadcaster());
    myEvents.clear();
    new WaitFor(TIMEOUT){
      @Override
      protected boolean condition() {
        return myEvents.size() > 0;
      }
    };
    assertEquals("No events expected:" + myEvents, 0, myEvents.size());
  }

  public void testSendMessageWhenOffline() {
    myFacade.disconnect();

    // This should work fine. Connection should be reestablished
    addEventListener();
    mySelf.sendMessage("message", getBroadcaster());
    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return myEvents.size() > 0;
      }
    };
    assertTrue("Should reestablish connection", myFacade.isConnectedAndAuthenticated());
    checkEvent(true);
  }

  public void testSendMessage_IgnoreList() throws Throwable {

    XMPPConnection conn = createLocalConnectionWithJabberUser("tom" + System.nanoTime(), null);
    conn.getRoster().createEntry(selfJabberId(), "name", new String[0]);
    addEventListener();
    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return myEvents.size() > 1;
      }
    };
    myEvents.clear();

    setIgnoreList(getUser(conn));

    conn.createChat(selfJabberId()).sendMessage("hello");
    new WaitFor(2000) {
      @Override
      protected boolean condition() {
        return myEvents.size() > 0;
      }
    };
    assertEquals("Should ignore message (ignore list exists):" + myEvents, 0, myEvents.size());
  }

  public void testSimpleJabberMessage() {
    Message message = new Message(mySelf.getName(), Message.Type.NORMAL);
    String body = "some \u0442\u0435\u043a\u0441\u0442 <>#$%^";
    message.setThread("someThreadId");
    message.setBody(body);

    addEventListener();
    myFacade.getConnection().sendPacket(message);

    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return myEvents.size() > 1;
      }
    };

    IDEtalkEvent event = myEvents.get(1);
    assertTrue("Expect message Event", event instanceof MessageEvent);
    assertEquals("Expect message text", body, ((MessageEvent) event).getMessage());
    assertEquals("Should remember threadId for incoming messages", "someThreadId", myTransport.getThreadId(mySelf));
  }

  public void testSendMessageWithNullBody_ResposesHaveNoBody() throws Exception {
    //Thread.sleep(1000);
    myEvents.clear();
    Message baseMessage = JabberTransport.createBaseMessage(mySelf, null);
    myTransport.getFacade().getConnection().sendPacket(baseMessage);
    Thread.sleep(100);
    assertEquals("No events expected: " + myEvents, 0, myEvents.size());
  }

  public void testSendCustomMessage_WithoutProvider() {

    addEventListener();

    myTransport.sendXmlMessage(mySelf, new TextXmlMessage("some text"){
      @Override
      public String getTagNamespace() {
        return "some namespace";
      }
    });

    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return myEvents.size() > 1;
      }
    };

    IDEtalkEvent event = myEvents.get(1);
    assertTrue("Expect message Event for unknown provider", event instanceof MessageEvent);
    assertEquals("Expect message text", "some text", ((MessageEvent) event).getMessage());
  }

  public void testTimeIsSetInSimpleMessage() {
    Message baseMessage = JabberTransport.createBaseMessage(mySelf, "some text");
    myTransport.getFacade().getConnection().sendPacket(baseMessage);

    new WaitFor(TIMEOUT){
      @Override
      protected boolean condition() {
        return myEvents.size() > 0;
      }
    };

    long diff = ((MessageEvent) myEvents.get(1)).getWhen().getTime() - System.currentTimeMillis();
    assertTrue("Time should be set for simple Jabber messages: " + diff, Math.abs(diff) < 150);
  }

  public void testSyncronizeRosterOnConnect() {
    myTransport.authenticated();
    assertTrue("No Jabber users in UserModel - should sync with roster", myUsersSynchronized);
  }

  public void testSyncronizeRosterOnConnect_HasJabberContacts() {
    myUserModel.addUser(UserImpl.create("fff@ddd.cc", JabberTransport.CODE));
    myTransport.authenticated();
    assertFalse("Already has Jabber users in UserModel - no sync with roster", myUsersSynchronized);
  }

  public void testSetOwnPresence_Status() throws Exception {
    String bobName = "bob" + System.currentTimeMillis();
    final XMPPConnection bob = createLocalConnectionWithJabberUser(bobName, null);
    final String user = selfJabberId();
    bob.getRoster().createEntry(user, user, new String[0]);
    myFacade.addUsers("grp", Arrays.asList(getUser(bob)));

    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return null != bob.getRoster().getPresence(user);
      }
    };
    assert null != bob.getRoster().getPresence(user);


    // First, change own presence and check it's changed from bob's point of view
    UserPresence presence = new UserPresence(PresenceMode.AWAY);
    assert presence.isOnline();

    myTransport.setOwnPresence(presence);
    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return Presence.Mode.away == bob.getRoster().getPresence(user).getMode();
      }
    };
    Presence p = bob.getRoster().getPresence(user);
    assertSame("Own presence should be away", Presence.Mode.away, p.getMode());
    assertEquals("", p.getStatus());

    // Now, change bob's presence
    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return myEvents.size() > 2;
      }
    };
    myEvents.clear();
    bob.sendPacket(new Presence(Presence.Type.available, "", 0, Presence.Mode.dnd));
    final User bobUser = myUserModel.findUser(getUser(bob), myTransport.getName());

    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return PresenceMode.DND == bobUser.getPresence().getPresenceMode();
      }
    };

    assertSame("Should take bob's presence", PresenceMode.DND, bobUser.getPresence().getPresenceMode());
    UserEvent.Updated event = (UserEvent.Updated) checkEvent(true);
    assertEquals(bobUser, event.getUser());
    assertEquals("presence", event.getPropertyName());
    assertEquals(PresenceMode.DND, event.getNewValue());
    assertEquals(PresenceMode.AVAILABLE, event.getOldValue());

    // Now, change bob's presence + set time
    bob.sendPacket(new Presence(Presence.Type.available, "some status", 0, Presence.Mode.xa));

    new WaitFor(TIMEOUT) {
      @Override
      protected boolean condition() {
        return PresenceMode.EXTENDED_AWAY == bobUser.getPresence().getPresenceMode();
      }
    };

    assertSame("Should take bob's presence", PresenceMode.EXTENDED_AWAY, bobUser.getPresence().getPresenceMode());
  }

  public void testShouldNotReconnectOnSimpleDisconnect() throws Throwable {

    myTransport.setReconnectTimeout(200);
    myTransport.disconnected(false);
    myFacade.disconnect();

    Thread.sleep(300);
    assertFalse("No reconnect expected", myFacade.isConnectedAndAuthenticated());
  }

  public void testShouldReconnectOnErrorDisconnect() throws Throwable {
    myTransport.setReconnectTimeout(200);
    myTransport.disconnected(true);  // emulate connection failure
    myFacade.disconnect();

    Thread.sleep(300);
    assertTrue("Reconnect expected", myFacade.isConnectedAndAuthenticated());
  }


  @Override
  protected User createSelf() {
    if (myFacade.getConnection() != null) {
      return UserImpl.create(selfJabberId(), myTransport.getName());
    }
    return null;
  }

  private String selfJabberId() {
    return getUser(myFacade.getConnection());
  }

  @Override
  protected User createAnotherOnlineUser() throws Exception {
    String userName = "AnotherUser" + System.currentTimeMillis();
    final XMPPConnection bob = createLocalConnectionWithJabberUser(userName, null);
    return myUserModel.createUser(getUser(bob), myTransport.getName());
  }

  private XMPPConnection createLocalConnectionWithJabberUser(String userName, String resource) throws XMPPException {
    final XMPPConnection conn = new XMPPConnection("localhost");
    conn.getAccountManager().createAccount(userName, "123456");
    if (resource == null) {
      resource = "test";
    }
    conn.login(userName, "123456", resource);
    conn.getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);
    Presence presence = new Presence(Presence.Type.available);
    conn.sendPacket(presence);
    disposeOnTearDown(new Disposable(){
      @Override
      public void dispose() {
        try {
          if (conn.isConnected()) {
            conn.getAccountManager().deleteAccount();
          }
        }
        catch (XMPPException e) {
          throw new RuntimeException(e);
        }
        finally{
          Thread thread = new Thread("jabber conn test") {
            @Override
            public void run() {
              conn.close();
            }
          };
          thread.start();
          try {
            thread.join();
          }
          catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      }
    });
    return conn;
  }

  private static class MockUserFinder implements JabberUserFinder {
    private String myLog = "";

    @Override
    public User[] findUsers(ProgressIndicator progressIndicator) {
      myLog += "findUsers";
      return new User[0];
    }

    @Override
    public void registerForProject(String jabberUserId) {
      myLog += "registerForProject" + jabberUserId;
    }

    public String getLog() {
      String log = myLog;
      myLog = "";
      return log;
    }
  }
}
