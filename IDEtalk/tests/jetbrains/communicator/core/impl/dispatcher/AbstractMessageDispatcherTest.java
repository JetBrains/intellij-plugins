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
package jetbrains.communicator.core.impl.dispatcher;

import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockMessage;
import jetbrains.communicator.mock.MockUser;
import jetbrains.communicator.p2p.commands.P2PNetworkMessage;

/**
 * @author kir
 */
public class AbstractMessageDispatcherTest extends BaseTestCase {
  private AbstractMessageDispatcher myDispatcher;
  private MockUser myUser;
  private MockIDEFacade myIdeFacade;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myIdeFacade = new MockIDEFacade(getClass());
    myDispatcher = createDispatcher();

    myUser = new MockUser("user", null);
    myUser.setOnline(true);
  }



  private AbstractMessageDispatcher createDispatcher() {
    return new AbstractMessageDispatcher(getBroadcaster(), myIdeFacade.getCacheDir()) {

      @Override
      protected String getEventsFileName() {
        return "test.xml";
      }
    };
  }

  @Override
  protected void tearDown() throws Exception {
    myDispatcher.clearAll();
    myDispatcher.dispose();
    super.tearDown();
  }

  public void testSendMessageToOnlineUser() {

    MockMessage message = new MockMessage(true);
    myDispatcher.performDispatch(myUser, message);
    assertEquals("should send to online user",
        "sent to "+myUser+":success", message.getLog());
  }

  public void testMessagesQueue() {

    assertEquals("No pending messages", 0, myDispatcher.getPendingMessages(myUser).length);

    myDispatcher.performDispatch(myUser, new MockMessage());
    assertEquals("Sent successfully, queue is still empty", 0, myDispatcher.getPendingMessages(myUser).length);

    // queue failed message
    MockMessage failedMessage = new MockMessage(false);
    myDispatcher.performDispatch(myUser, failedMessage);

    assertEquals("Failed message should be queued",
        1, myDispatcher.getPendingMessages(myUser).length);
    assertSame(failedMessage, myDispatcher.getPendingMessages(myUser)[0]);
  }

  public void testSendSameMessageTwice() {
    MockMessage failedMessage = new MockMessage(false);
    myDispatcher.performDispatch(myUser, failedMessage);
    myDispatcher.performDispatch(myUser, failedMessage);
    assertEquals("Only one message should be queued", 1,
        myDispatcher.getPendingMessages(myUser).length);
  }

  public void testUserDeleted() {
    MockMessage failedMessage = new MockMessage(false);
    myDispatcher.performDispatch(myUser, failedMessage);

    getBroadcaster().fireEvent(new UserEvent.Removed(myUser));
    assertEquals("Queue should be cleared when user is deleted", 0,
        myDispatcher.getPendingMessages(myUser).length);
  }

  public void testSuccessfulSendingOfPendingEvent() {
    myUser.setOnline(true);
    MockMessage failedMessage = new MockMessage(false);
    myDispatcher.performDispatch(myUser, failedMessage);

    failedMessage.setSendSuccessful(true);
    myDispatcher.performDispatch(myUser, failedMessage);

    assertEquals("If sent successful, remove from pending queue", 0,
        myDispatcher.getPendingMessages(myUser).length);
  }

  public void testRemoveMessageFromQueue() {
    MockMessage message1 = new MockMessage();
    MockMessage message2 = new MockMessage();
    MockMessage message3 = new MockMessage();
    myDispatcher.addPendingMessage(myUser, message1);
    myDispatcher.addPendingMessage(myUser, message2);
    myDispatcher.addPendingMessage(myUser, message3);

    myDispatcher.removePendingMessage(myUser, 0);
    assertSame(message2, myDispatcher.getPendingMessages(myUser)[0]);

    myDispatcher.removePendingMessage(myUser, 1);
    assertSame(message2, myDispatcher.getPendingMessages(myUser)[0]);

    assertEquals(1, myDispatcher.getPendingMessages(myUser).length);
  }

  public void testPersistency() {
    Message failedMessage = new P2PNetworkMessage("foo", "foo2", new String[]{"test"});
    myDispatcher.performDispatch(myUser, failedMessage);
    myDispatcher.save();

    AbstractMessageDispatcher dispatcher = createDispatcher();
    assertEquals("queue should persist", 1, dispatcher.getPendingMessages(myUser).length);

    Message message = dispatcher.getPendingMessages(myUser)[0];
    assertTrue("queue should persist", message instanceof P2PNetworkMessage);
    assertEquals("test", ((P2PNetworkMessage) message).getParameters()[0]);

    dispatcher.dispose();
  }

}