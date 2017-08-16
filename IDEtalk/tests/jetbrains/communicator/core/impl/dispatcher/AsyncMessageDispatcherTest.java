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

import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockMessage;
import jetbrains.communicator.mock.MockUser;
import jetbrains.communicator.util.WaitFor;
import org.apache.log4j.Logger;

/**
 * @author kir
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class AsyncMessageDispatcherTest extends BaseTestCase {
  private static final Logger LOG = Logger.getLogger(AsyncMessageDispatcherTest.class);

  private AsyncMessageDispatcherImpl myDispatcher;
  private MockUser myUser;
  private MockIDEFacade myIdeFacade;
  private String[] myLog;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myIdeFacade = new MockIDEFacade(getClass());
    myUser = new MockUser("user", null);
    myUser.setOnline(true);

    myDispatcher = new AsyncMessageDispatcherImpl(getBroadcaster(), myIdeFacade);

    new WaitFor(1000) {
      @Override
      protected boolean condition() {
        return myDispatcher.isRunning();
      }
    };

    myLog = new String[]{""};
  }

  @Override
  protected void tearDown() throws Exception {
    myDispatcher.clearAll();
    myDispatcher.dispose();

    assertFalse(myDispatcher.isRunning());
    super.tearDown();
  }

  public void testDispose() {
    
  }

  public void testAsyncDispatch_Success() {

    NotifyableMessage mockMessage = new NotifyableMessage(true, myLog);

    myDispatcher.sendLater(new MockUser(), mockMessage);
    myLog[0] += "Returned";
    mockMessage.waitUntilDispatchingStarted();

    triggerMessageProcessing(mockMessage);

    assertEquals("Should return from method call and than process the message",
        "ReturnedProcessed", myLog[0]);
    assertEquals("Messages should be delivered", 0,
        myDispatcher.getUsersWithMessages().length);
  }

  private void triggerMessageProcessing(final NotifyableMessage mockMessage) {
    mockMessage.triggerProcess();
    new WaitFor(10000) {
      @Override
      protected boolean condition() {
        return !myDispatcher.isMessageDispatchInProgress();
      }
    };
  }

  public void testAsyncDispatch_Failure() {

    NotifyableMessage mockMessage = new NotifyableMessage(false, myLog);

    myDispatcher.sendLater(new MockUser(), mockMessage);
    myLog[0] += "Returned";
    mockMessage.waitUntilDispatchingStarted();

    triggerMessageProcessing(mockMessage);

    assertEquals("Should return from method call and than process the message",
        "ReturnedProcessed", myLog[0]);
    assertEquals("Messages should not be delivered", 1,
        myDispatcher.getUsersWithMessages().length);
  }

  public void testAddMessagesWhileDispatching() {

    NotifyableMessage mockMessage = new NotifyableMessage(true, myLog);
    MockUser user1 = new MockUser("user1", null);
    MockUser user2 = new MockUser("user2", null);
    user1.setOnline(true);
    user2.setOnline(true);

    myDispatcher.sendLater(user1, mockMessage);
    mockMessage.waitUntilDispatchingStarted();
    myDispatcher.sendLater(user2, new MockMessage(true));

    assertEquals("Messages not delivered yet", 2,
        myDispatcher.getUsersWithMessages().length);

    triggerMessageProcessing(mockMessage);

    new WaitFor(200) {
      @Override
      protected boolean condition() {
        return myDispatcher.getUsersWithMessages().length == 0;
      }
    };

    assertEquals("All messages should be delivered", 0, myDispatcher.getUsersWithMessages().length);
  }

  private static class NotifyableMessage extends MockMessage {
    private boolean myDispatchingStarted;
    private boolean myProcessed;
    private final String[] myLog1;

    NotifyableMessage(boolean successfulDelivery, String[] log) {
      super(successfulDelivery);
      myLog1 = log;
    }

    public void waitUntilDispatchingStarted() {
      new WaitFor(10000) {
        @Override
        protected boolean condition() {
          return myDispatchingStarted;
        }
      };
    }

    @Override
    public synchronized boolean send(User user) {
      try {
        LOG.debug("AsyncMessageDispatcherTest$NotifyableMessage.StartProcessing");
        myDispatchingStarted = true;
        wait();
        myLog1[0] += "Processed";
        LOG.debug("AsyncMessageDispatcherTest$NotifyableMessage.Processed");
        myProcessed = true;

      } catch (InterruptedException e) {
        fail("Unexpected interrupt");
      }
      return super.send(user);
    }

    public synchronized boolean isProcessed() {
      return myProcessed;
    }

    public synchronized void triggerProcess() {
      LOG.debug("Before trigger");
      notifyAll();
      LOG.debug("After trigger");
    }

    public String toString() {
      return "TestMessage@" + System.identityHashCode(this);
    }
  }
}
