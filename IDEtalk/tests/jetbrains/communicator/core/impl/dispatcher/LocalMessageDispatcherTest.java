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

import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.impl.users.UserModelImpl;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.SendMessageEvent;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockMessage;
import jetbrains.communicator.mock.MockTransport;
import jetbrains.communicator.util.WatchDog;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Date;

/**
 * @author kir
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class LocalMessageDispatcherTest extends BaseTestCase {
  private LocalMessageDispatcherImpl myDispatcher;
  private User myUser;
  private MockIDEFacade myIdeFacade;
  private UserModelImpl myUserModel;
  private static final long SAVE_WAIT_TIMEOUT = MessageHistory.SAVE_TIMEOUT + 200;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUserModel = new UserModelImpl(getBroadcaster());
    disposeOnTearDown(myUserModel);
    myIdeFacade = new MockIDEFacade(getClass());
    myDispatcher = new LocalMessageDispatcherImpl(getBroadcaster(), myIdeFacade, myUserModel);

    myUser = UserImpl.create("user", MockTransport.NAME);
  }

  @Override
  protected void tearDown() throws Exception {
    myDispatcher.clearAll();
    myDispatcher.dispose();
    super.tearDown();
  }

  public void testIconBlinkingStatus() {
    assertFalse(hasMessagesWhichRequireIconBlinking());

    MockMessage message = new MockMessage();
    myDispatcher.addPendingMessage(myUser, message);

    assertEquals(1, myDispatcher.getPendingMessages(myUser).length);
    assertTrue("Should blink to inform user about urgent event",
        hasMessagesWhichRequireIconBlinking());

    myDispatcher.sendNow(myUser, message);
    assertFalse("No more blinking expected - message was dispatched",
        hasMessagesWhichRequireIconBlinking());
  }

  private boolean hasMessagesWhichRequireIconBlinking() {
    return myDispatcher.getBlinkingIcon() != null;
  }

  public void testAddPendingMessage() {
    MockMessage message = new MockMessage();
    myDispatcher.addPendingMessage(myUser, message);

    assertEquals(1, myDispatcher.getPendingMessages(myUser).length);
    assertSame(message, myDispatcher.getPendingMessages(myUser)[0]);

    // add again
    myDispatcher.addPendingMessage(myUser, message);
    assertEquals("Should be added once", 1, myDispatcher.getPendingMessages(myUser).length);

    myDispatcher.addPendingMessage(myUser, null);
    assertEquals("Should have no problems with nulls", 1, myDispatcher.getPendingMessages(myUser).length);
  }

  public void testTransportEvent() {

    MockTransport mockTransport = new MockTransport();

    MockMessage localMessage = new MockMessage();
    MockMessage localMessage1 = new MockMessage();
    myIdeFacade.setReturnedMessage(localMessage);
    getBroadcaster().fireEvent(new TransportEvent(mockTransport, "user"){});

    myIdeFacade.setReturnedMessage(localMessage1);
    getBroadcaster().fireEvent(new TransportEvent(mockTransport, "user"){});

    Message[] pendingMessages = myDispatcher.getPendingMessages(myUser);
    assertEquals("Expect a local localMessages as a result to TransportEvent",
        2, pendingMessages.length);
    assertSame(localMessage, pendingMessages[0]);
    assertSame(localMessage1, pendingMessages[1]);
    assertEquals("", localMessage.getLog());
  }

  public void testSendMessageEvent() {
    MockTransport mockTransport = new MockTransport();

    MockMessage localMessage = new MockMessage();
    MockMessage localMessage1 = new MockMessage();
    myIdeFacade.setReturnedMessage(localMessage);
    getBroadcaster().fireEvent(new SendMessageEvent("text", myUser));

    myIdeFacade.setReturnedMessage(localMessage1);
    getBroadcaster().fireEvent(new SendMessageEvent("text", myUser));

    LocalMessage[] history = myDispatcher.getHistory(myUser, null);
    assertEquals("Expect localMessages as a result to OwnMessageEvent",
        2, history.length);
    assertSame(localMessage, history[0]);
    assertSame(localMessage1, history[1]);
  }

  public void testPersistency() {
    myDispatcher.addPendingMessage(myUser, new MockMessage());
    assertEquals("Should have no problems with nulls", 1, myDispatcher.getPendingMessages(myUser).length);

    // test persistency
    LocalMessageDispatcherImpl localMessageDispatcher = createLocalMessageDispatcher();
    assertEquals(1, localMessageDispatcher.getPendingMessages(myUser).length);

    localMessageDispatcher.sendNow(myUser,
        localMessageDispatcher.getPendingMessages(myUser)[0]);

    localMessageDispatcher =
        new LocalMessageDispatcherImpl(getBroadcaster(), myIdeFacade, myUserModel);
    disposeOnTearDown(localMessageDispatcher);
    assertEquals(0, localMessageDispatcher.getPendingMessages(myUser).length);
  }

  public void testHistory() {
    assertEquals(0, myDispatcher.getHistory(myUser, null).length);

    MockMessage message = new MockMessage();
    myDispatcher.addPendingMessage(myUser, message);

    assertEquals(1, myDispatcher.getPendingMessages(myUser).length);
    assertEquals(0, myDispatcher.getHistory(myUser, null).length);

    myDispatcher.sendNow(myUser, message);

    assertEquals(0, myDispatcher.getPendingMessages(myUser).length);
    assertEquals(1, myDispatcher.getHistory(myUser, null).length);

    MockMessage message2 = new MockMessage();
    myDispatcher.sendNow(myUser, message2);

    LocalMessage[] history = myDispatcher.getHistory(myUser, null);
    assertEquals(2, history.length);
    assertSame(message, history[0]);
    assertSame(message2, history[1]);
  }

  public void testHistoryWithDate() throws Exception {
    MockMessage message = new MockMessage();
    myDispatcher.sendNow(myUser, message);

    Thread.sleep(10);
    assertEquals(1, myDispatcher.getHistory(myUser, a_moment_ago()).length);
    assertEquals("Do not expect old history", 0, myDispatcher.getHistory(myUser, new Date()).length);
  }

  private Date a_moment_ago() {
    return new Date(System.currentTimeMillis() - 5100);
  }

  public void testClearHistory() {
    assertTrue(myDispatcher.isHistoryEmpty());

    MockMessage message = new MockMessage();
    myDispatcher.sendNow(myUser, message);
    myDispatcher.clearHistory();

    assertEquals(0, myDispatcher.getHistory(myUser, null).length);
  }

  public void testHistoryPersistence() throws Exception {
    // Test Saving history message:
    MockMessage message = new MockMessage(new Date(), "some text \u0420\u041f \u0422\u0425\u0423\u0423\u041b\u0419");
    myDispatcher.sendNow(myUser, message);

    Thread.sleep(SAVE_WAIT_TIMEOUT);

    LocalMessageDispatcherImpl localMessageDispatcher = createLocalMessageDispatcher();
    assertEquals(1, localMessageDispatcher.getHistory(myUser, null).length);
    assertEquals("some text \u0420\u041f \u0422\u0425\u0423\u0423\u041b\u0419",
                 ((MockMessage) localMessageDispatcher.getHistory(myUser, null)[0]).getMessage());
  }

  public void testHistoryPersistence_SortLoadedHistory() throws Exception {
    myDispatcher.sendNow(myUser, new MockMessage(yesterday(), "someText"));

    MockMessage message = new MockMessage();
    myDispatcher.sendNow(myUser, message);

    Thread.sleep(SAVE_WAIT_TIMEOUT);

    LocalMessageDispatcherImpl localMessageDispatcher = createLocalMessageDispatcher();
    localMessageDispatcher.sendNow(myUser, message);

    Thread.sleep(SAVE_WAIT_TIMEOUT);
    assertEquals(2, localMessageDispatcher.getHistory(myUser, a_moment_ago()).length);
    LocalMessage[] messages = localMessageDispatcher.getHistory(myUser, null);

    assertEquals(3, messages.length);
    assertEquals("someText", ((MockMessage) messages[0]).getMessage());
  }

  protected LocalMessageDispatcherImpl createLocalMessageDispatcher() {
    LocalMessageDispatcherImpl localMessageDispatcher =
        new LocalMessageDispatcherImpl(getBroadcaster(), myIdeFacade, myUserModel);
    disposeOnTearDown(localMessageDispatcher);
    return localMessageDispatcher;
  }

  public void testHistoryInSeveralFiles() throws Exception {
    myDispatcher.sendNow(myUser, new MockMessage(new Date()));
    myDispatcher.sendNow(myUser, new MockMessage(yesterday()));

    Thread.sleep(SAVE_WAIT_TIMEOUT);
    assertEquals(2, new File(myIdeFacade.getCacheDir(), "history").listFiles().length);

    LocalMessageDispatcherImpl localMessageDispatcher = createLocalMessageDispatcher();
    LocalMessage[] messages = localMessageDispatcher.getHistory(myUser, a_moment_ago());
    assertEquals(1, messages.length);

    messages = localMessageDispatcher.getHistory(myUser, new Date(System.currentTimeMillis() - 1000 * 3601 * 24));
    assertEquals(2, messages.length);
  }

  private Date yesterday() {
    return new Date(System.currentTimeMillis() - 1000 * 3600 * 24);
  }

  public void testPerformance() throws Exception {
    Logger logger = Logger.getLogger("jetbrains.communicator");
    Level oldLevel = logger.getLevel();
    try {
      logger.setLevel(Level.WARN);

      for (int i = 0; i < 1000; i ++) {
        Date date = new Date(System.currentTimeMillis() + i * 1000L * 3600L);
        myDispatcher.sendNow(myUser, new MockMessage(date));
      }
      Thread.sleep(SAVE_WAIT_TIMEOUT*2);


      WatchDog watchDog = new WatchDog("Load history");
      LocalMessageDispatcherImpl localMessageDispatcher = createLocalMessageDispatcher();
      LocalMessage[] messages = localMessageDispatcher.getHistory(myUser, yesterday());
      assertEquals(1000, messages.length);

      long diff = watchDog.diff();
      watchDog.watchAndReset("done");
      assertTrue("Too long getting history:" + diff, diff < 1500);

      messages = localMessageDispatcher.getHistory(myUser, null);
      assertEquals(1000, messages.length);

      diff = watchDog.diff();
      watchDog.watchAndReset("again done");
      assertTrue("Too long getting history second time:" + diff, diff < 100);

    } finally {
      logger.setLevel(oldLevel);
    }
  }

  public void testClearHistory_Persistence() throws Exception {
    myDispatcher.sendNow(myUser, new MockMessage(new Date()));
    Thread.sleep(SAVE_WAIT_TIMEOUT);

    assertFalse(myDispatcher.isHistoryEmpty());

    myDispatcher.clearHistory();
    Thread.sleep(SAVE_WAIT_TIMEOUT);
    assertEquals("History should be deleted", 0, new File(myIdeFacade.getCacheDir(), "history").listFiles().length);

    myDispatcher.sendNow(myUser, new MockMessage(new Date()));
    myDispatcher.clearHistory();
    Thread.sleep(SAVE_WAIT_TIMEOUT);
    assertEquals("History should be deleted", 0, new File(myIdeFacade.getCacheDir(), "history").listFiles().length);

    assertEquals(0, myDispatcher.getHistory(myUser, null).length);
    assertTrue(myDispatcher.isHistoryEmpty());
  }
}
