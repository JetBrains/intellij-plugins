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
package jetbrains.communicator.idea;

import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.transport.EventFactory;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.SendCodePointerEvent;
import jetbrains.communicator.ide.SendMessageEvent;
import jetbrains.communicator.idea.codePointer.IncomingCodePointerMessage;
import jetbrains.communicator.idea.sendMessage.IncomingLocalMessage;
import jetbrains.communicator.idea.sendMessage.IncomingStacktraceMessage;
import jetbrains.communicator.mock.MockTransport;

import java.util.Date;

/**
 * @author kir
 */
public class IDEAFacadeTest extends BaseTestCase {
  private IDEAFacade myFacade;
  private MockTransport myTransport;
  private User myUser;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myTransport = new MockTransport();

    myFacade = new IDEAFacade();
    myUser = UserImpl.create("user", myTransport.getName());
  }

  public void testCreateLocalMessage_OutgoingMessage() {
    LocalMessage outgoingEvent = myFacade.createLocalMessageForOutgoingEvent(new SendMessageEvent("message", myUser));
    assertTrue(outgoingEvent instanceof OutgoingLocalMessage);
  }

  public void testCreateLocalMessage_OutgoingCodePointer() {
    SendCodePointerEvent event = new SendCodePointerEvent("message", VFile.create("a path"), new CodePointer(3,4), myUser);
    LocalMessage outgoingEvent = myFacade.createLocalMessageForOutgoingEvent(event);
    assertTrue(outgoingEvent instanceof OutgoingCodePointerLocalMessage);
  }

  public void testCreateLocalMessage_IncomingMessage() {
    TransportEvent event =
        EventFactory.createMessageEvent(myTransport, "user", "comment123");
    Date when = new Date();
    event.setWhen(when.getTime());
    LocalMessage localMessage = myFacade.createLocalMessageForIncomingEvent(event);

    assertTrue("instance of Stacktracelocalmessage expected:" + localMessage.getClass().getName() ,
        localMessage instanceof IncomingLocalMessage);
    assertEquals("comment123", ((BaseLocalMessage) localMessage).getComment());
    assertEquals(when, localMessage.getWhen());
  }

  public void testCreateLocalMessage_Stacktrace() {
    TransportEvent event =
        EventFactory.createStacktraceEvent(myTransport, "user", "stacktrace", "comment123");
    LocalMessage localMessage = myFacade.createLocalMessageForIncomingEvent(event);

    assertTrue("instance of Stacktracelocalmessage expected",
        localMessage instanceof IncomingStacktraceMessage);
    assertEquals("comment123", ((BaseLocalMessage) localMessage).getComment());
  }

  public void testCreateLocalMessage_CodeInterval() {
    TransportEvent event =
        EventFactory.createCodePointerEvent(myTransport, "user22", VFile.create("a path"), 0, 0, 1, 2, "some comment1");
    LocalMessage localMessage = myFacade.createLocalMessageForIncomingEvent(event);

    assertTrue("instance of IncomingCodePointerMessage expected:" + localMessage,
        localMessage instanceof IncomingCodePointerMessage);
    assertEquals("some comment1", ((BaseLocalMessage) localMessage).getComment());
  }
}
