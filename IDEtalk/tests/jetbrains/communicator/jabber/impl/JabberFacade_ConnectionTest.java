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

import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.jabber.ConnectionListener;
import jetbrains.communicator.jabber.JabberFacade;
import jetbrains.communicator.jabber.VCardInfo;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.util.WaitFor;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;

/**
 * @author Kir
 */
public class JabberFacade_ConnectionTest extends BaseTestCase {
  private JabberFacadeImpl myFacade;
  private MockIDEFacade myIDEFacade;

  public static final String USER = "someuser";
  public static final String PASSWORD = "somePassword";
  public static final String LOCALHOST = "localhost";
  private String myUser;

  public static Test suite() {
    TestSuite testSuite = new TestSuite();
    XMPPConnection ourConnection;
    try {
      ourConnection = new XMPPConnection(LOCALHOST);
    } catch (XMPPException e) {
      return testSuite;
    }
    ourConnection.close();

    testSuite.addTestSuite(JabberFacade_ConnectionTest.class);
    return testSuite;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myIDEFacade = new MockIDEFacade(getClass());
    myFacade = new JabberFacadeImpl(myIDEFacade);

    myUser = USER + System.currentTimeMillis();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      myFacade.getConnection().getAccountManager().deleteAccount();
      myFacade.disconnect();
    } catch (Throwable e) {
      // Harmless:
      // e.printStackTrace();
    }

    super.tearDown();
  }

  public void testConnectAndCreateUser() {

    assertFalse("No connection yet", myFacade.isConnectedAndAuthenticated());
    String message = createGoodAccount(myUser, myFacade);
    assertNull("No error expected: " + message, message);

    assertTrue("Have connection", myFacade.isConnectedAndAuthenticated());

    assertTrue("Facade connection should be logged in", myFacade.getConnection().isAuthenticated());
    String user = myFacade.getConnection().getUser();
    assertTrue("Wrong user is logged in: " + user,
        user.startsWith(myUser) && user.endsWith(JabberFacade.IDETALK_RESOURCE));
  }

  public void testPasswordWithAmpersand() {
    String connect = myFacade.createAccountAndConnect(myUser, "&&AA", LOCALHOST, myFacade.getMyAccount().getPort(), false);
    assertNull(connect);
  }

  private String createGoodAccount(String user, JabberFacadeImpl facade) {
    return facade.createAccountAndConnect(user, PASSWORD,
        LOCALHOST, facade.getMyAccount().getPort(), false);
  }

  public void testConnect() {

    assertNotNull("Expect error - no such user yet",
        myFacade.connect(myUser, PASSWORD, LOCALHOST, myFacade.getMyAccount().getPort(), false));

    createGoodAccount(myUser, myFacade);
    myFacade.disconnect();

    assertFalse("sanity check", myFacade.isConnectedAndAuthenticated());
    String message = myFacade.connect(myUser, PASSWORD, LOCALHOST, myFacade.getMyAccount().getPort(), false);
    assertNull("Should connect - no error expected: " + message, message);
    assertTrue("Should be connected now", myFacade.isConnectedAndAuthenticated());

    assertEquals("Save after success", myUser, myFacade.getMyAccount().getUsername());
    assertEquals("Save after success", PASSWORD, myFacade.getMyAccount().getPassword());
    assertEquals("Save after success", LOCALHOST, myFacade.getMyAccount().getServer());


    assertNotNull("Expect error",
        myFacade.connect("ddd", "aaa", LOCALHOST, myFacade.getMyAccount().getPort(), false));
  }

  public void testConnect_SSL() {
    if (isPortBusy(5223)) {
      createGoodAccount(myUser, myFacade);
      myFacade.disconnect();

      assertNull(myFacade.connect(myUser, PASSWORD, LOCALHOST, 5223, true));
      assertTrue(myFacade.isConnectedAndAuthenticated());

      assertTrue(myFacade.getConnection().isSecureConnection());
      assertFalse(myFacade.getConnection().isUsingTLS());
    }
  }

  private static boolean isPortBusy(int port) {
    ServerSocket socket = null;
    try {
      socket = new ServerSocket(port);
    }
    catch (IOException ignored) {
      return true;
    }
    finally {
      close(socket);
    }
    return false;
  }

  private static void close(ServerSocket socket) {
    if (socket != null) {
      try {
        socket.close();
      }
      catch (IOException e) {
        LOG.info(e.getLocalizedMessage());
      }
    }
  }

  public void testConnectListener() {
    final boolean[] connected = new boolean[1];
    final boolean[] disconnected = new boolean[1];
    final boolean[] authenticated = new boolean[1];
    myFacade.addConnectionListener(new ConnectionListener() {
      @Override
      public void connected(XMPPConnection connection) {
        connected[0] = true;
      }

      @Override
      public void disconnected(boolean onError) {
        disconnected[0] = true;
      }

      @Override
      public void authenticated() {
        authenticated[0] = true;
      }
    });

    assertFalse("sanity check", connected[0] || disconnected[0] || authenticated[0]);

    createGoodAccount(myUser, myFacade);
    assertTrue("should call listener", connected[0] && authenticated[0]);
    assertFalse(disconnected[0]);

    myFacade.disconnect();
    assertTrue(disconnected[0]);
  }

  public void testFailConnect() {
    String message = myFacade.createAccountAndConnect(myUser, PASSWORD,
        "777", myFacade.getMyAccount().getPort(), false);

    assertNotNull("No error expected", message);
    assertFalse("Connection failed", myFacade.isConnectedAndAuthenticated());
  }

  public void testSetVCardInfo() throws Throwable {
    createGoodAccount(myUser, myFacade);
    VCardInfo vCard = myFacade.getVCard(null);
    assertEquals("Empty VCard expected", "", vCard.getFirstname() + vCard.getLastname() );

    myFacade.setVCardInfo("nick", "Kirill", "Maximov");

    // Recreate facade and connection:
    myFacade = new JabberFacadeImpl(myIDEFacade);
    myFacade.connect(myUser, PASSWORD, LOCALHOST, myFacade.getMyAccount().getPort(), false);

    vCard = myFacade.getVCard(null);
    assertEquals("Should obtain VCard for self - nickname", "nick", vCard.getNickName());
    assertEquals("Should obtain VCard for self - firsname", "Kirill", vCard.getFirstname());
    assertEquals("Should obtain VCard for self - lastname", "Maximov", vCard.getLastname());
  }

  public void testAddUsers() {
    createGoodAccount(myUser, myFacade);
    final String bob = "bob@jabber.org";
    String self = myFacade.getConnection().getUser();
    myFacade.addUsers("someGroup", Arrays.asList(self, bob));

    final Roster roster = myFacade.getConnection().getRoster();
    new WaitFor(500) {
      @Override
      protected boolean condition() {
        return roster.contains(bob);
      }
    };

    assertTrue("User should be added to the Roster", roster.contains(bob));
    assertFalse("Self should not be added to the Roster", roster.contains(self));

    new WaitFor(200) {
      @Override
      protected boolean condition() {
        return roster.getEntry(bob).getGroups().size() > 0;
      }
    };
    assertEquals("Bad Group", "someGroup", roster.getEntry(bob).getGroups().iterator().next().getName());
  }

  public void testPresenceAfterConnection() {
    createGoodAccount(myUser, myFacade);
    final String me = myFacade.getConnection().getUser();

    myFacade.getConnection().getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);
    final JabberFacadeImpl another = new JabberFacadeImpl(myIDEFacade);
    createGoodAccount("ddd" + System.currentTimeMillis(), another);

    another.addUsers("group", Arrays.asList(me));
    new WaitFor(3000) {
      @Override
      protected boolean condition() {
        return another.getConnection().getRoster().getPresence(me) != null;
      }
    };

    Presence presence = another.getConnection().getRoster().getPresence(me);
    assertNotNull(presence);
    assertEquals(Presence.Type.available, presence.getType());
    assertEquals(Presence.Mode.available, presence.getMode());
  }
}
