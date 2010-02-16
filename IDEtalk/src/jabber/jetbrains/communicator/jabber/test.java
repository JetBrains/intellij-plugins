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
package jetbrains.communicator.jabber;

import com.intellij.util.ArrayUtil;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.util.StringUtil;
import junit.framework.Assert;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smackx.packet.VCard;

import java.util.Collection;

/**
 * @author Kir
 */
public class test {

  private static void testSetGroup() throws XMPPException, InterruptedException {
    XMPPConnection c = new XMPPConnection("localhost");

    try {
      try {
        c.getAccountManager().createAccount("bob", "ddd");
        c.getAccountManager().createAccount("alice", "ddd");
      }
      catch(Exception e){}

      c.login("bob", "ddd");

      Roster bobRoster = c.getRoster();

      bobRoster.createEntry("alice@localhost", "Alice", ArrayUtil.EMPTY_STRING_ARRAY);
      RosterGroup someGroup = bobRoster.createGroup("someGroup");
      RosterEntry entry = bobRoster.getEntry("alice@localhost");
      Thread.sleep(500);
      Assert.assertNotNull(entry);

      someGroup.addEntry(entry);
      Assert.assertTrue(someGroup.contains(entry));

      c.close();
      c = new XMPPConnection("localhost");
      c.login("bob", "ddd");
      Assert.assertNotNull(c.getRoster().getGroup("someGroup"));
      Assert.assertEquals(1, c.getRoster().getGroup("someGroup").getEntryCount());


    } finally {
      c.getAccountManager().deleteAccount();
    }
  }

  public static void testVCard() throws Exception {
    XMPPConnection.DEBUG_ENABLED = true;
    XMPPConnection c = new XMPPConnection("localhost");
    try {
      c.getAccountManager().createAccount("bob", "ddd");
    } catch (Exception e) {
      e.printStackTrace();
    }

    c.login("bob", "ddd");

    VCard vCard = new VCard();
    vCard.load(c);

    vCard.setAddressFieldHome("STREET", "123");
    vCard.setPhoneWork("FAX", "1234");

    vCard.save(c);


    vCard = new VCard();
    vCard.load(c);

    Assert.assertEquals("123", vCard.getAddressFieldHome("STREET"));
    Assert.assertEquals("1234", vCard.getPhoneWork("FAX"));
  }

  public static void main(String[] args) throws Throwable, InterruptedException {
    //testVCard();
    //testSetGroup();
    //testAutoSubscribe();

    testManyAccounts();
  }

  private static void testManyAccounts() throws Exception {
    for(int i = 0; i < 20; i ++) {
      XMPPConnection connection = new XMPPConnection("kir-pc", 5222, "localhost");
      final String username = "user" + i;
      final String password = "pwd";

      try {
        connection.getAccountManager().createAccount(username, password);
        connection.login(username, password);
        connection.getAccountManager().deleteAccount();
      } catch (Exception e) {
        System.out.println("i = " + i);
        throw e;
      }
      finally {
        connection.close();
      }
    }
  }

  private static void testAutoSubscribe() throws XMPPException, InterruptedException {
    Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

    XMPPConnection.DEBUG_ENABLED = true;
    XMPPConnection bob = new XMPPConnection("localhost");
    final XMPPConnection alice = new XMPPConnection("localhost");

    if (bob.getAccountManager().supportsAccountCreation()) {
      try {
        bob.getAccountManager().createAccount("bob", "ddd");
        bob.getAccountManager().createAccount("alice", "ddd");
      } catch (XMPPException e) {
      }
    }

    bob.login("bob", "ddd");
    alice.login("alice", "ddd");

    final Roster bobRoster = bob.getRoster();

    bobRoster.createEntry("bob@localhost", "bob", ArrayUtil.EMPTY_STRING_ARRAY);
    assert 1 == bobRoster.getEntryCount();
    Presence presence = bobRoster.getPresence("bob@localhost");
    System.out.println("presence = " + presence);

    Thread.sleep(10000);

    final Roster aliceRoster = alice.getRoster();

    cleanupRoster(bobRoster);
    cleanupRoster(aliceRoster);

    addAliceAutosubscriber(alice, bobRoster);

    bobRoster.createEntry("alice@localhost", "Alice", ArrayUtil.EMPTY_STRING_ARRAY);

    Thread.sleep(400);

    testPresenceChanges(bobRoster, alice);


    Chat chat = bob.createChat("bob@localhost");

    Message message = chat.createMessage();
    DefaultPacketExtension packetExtension =
        new DefaultPacketExtension("ideTalk", Transport.NAMESPACE);
    String s = StringUtil.toXMLSafeString("value@");
    packetExtension.setValue("code", s);
    message.addExtension(packetExtension);

    message.setBody("Test body");

    chat.sendMessage(message);

    Thread.sleep(200);
    Message msg1 = chat.nextMessage();
    System.out.println("" + msg1.getBody());
    PacketExtension extension = msg1.getExtension("ideTalk", Transport.NAMESPACE);
    System.out.println("" + extension);
    System.out.println("" + ((DefaultPacketExtension) extension).getValue("code"));


    bob.close();
    alice.close();
  }

  private static void testPresenceChanges(final Roster bobRoster, final XMPPConnection alice) {
    bobRoster.addRosterListener(new RosterListener() {
      public void entriesAdded(Collection addresses) {
      }

      public void entriesUpdated(Collection addresses) {
      }

      public void entriesDeleted(Collection addresses) {
      }

      public void presenceChanged(String user) {
        Presence presence = bobRoster.getPresence(user);
        System.out.println("New presence for " + user + ": " + presence);
      }
    });

    Presence presence = new Presence(Presence.Type.available, "Gone for lunch", -1, Presence.Mode.xa);
    alice.sendPacket(presence);

    presence = new Presence(Presence.Type.available, "Very busy", -1, Presence.Mode.dnd);
    alice.sendPacket(presence);
  }

  private static void addAliceAutosubscriber(final XMPPConnection alice, final Roster bobRoster) {
    alice.addPacketListener( new PacketListener() {
    public void processPacket(Packet packet) {
      Presence presence = ((Presence) packet);
      Presence.Type type = presence.getType();
      System.out.println("type = " + type);
      System.out.println("from = " + presence.getFrom());
      System.out.println("to = " + presence.getTo());
      System.out.println("XML = " + presence.toXML());

      Presence presence1 = bobRoster.getPresence("alice@localhost");
      System.out.println("Alice presence1 = " + presence1);

      confirmSubscription(alice, presence);

    }
    } , new PacketTypeFilter(Presence.class));
  }

  private static void confirmSubscription(final XMPPConnection connection, Presence request) {
    Presence reply = new Presence(Presence.Type.subscribed);
    reply.setFrom(connection.getUser());
    reply.setTo(request.getFrom());
    connection.sendPacket(reply);
  }

  private static void cleanupRoster(final Roster bobRoster) throws XMPPException {
    for (RosterEntry entry : bobRoster.getEntries()) {
      bobRoster.removeEntry(entry);
    }
  }
}
