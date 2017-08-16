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
package jetbrains.communicator.idea.messagesWindow;

import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.dispatcher.LocalMessageDispatcherImpl;
import jetbrains.communicator.core.impl.users.UserModelImpl;
import jetbrains.communicator.core.transport.EventFactory;
import jetbrains.communicator.core.transport.MessageEvent;
import jetbrains.communicator.idea.IDEtalkMessagesWindow;
import jetbrains.communicator.idea.config.IdeaFlags;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockTransport;
import org.jmock.Mock;

/**
 * @author Kir
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class EventsProcessorTest extends BaseTestCase {
  private UserModelImpl myUserModel;
  private Mock myMock;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUserModel = new UserModelImpl(getBroadcaster());
    disposeOnTearDown(myUserModel);

    myMock = mock(IDEtalkMessagesWindow.class);

    LocalMessageDispatcherImpl localMessageDispatcher = new LocalMessageDispatcherImpl(getBroadcaster(), new MockIDEFacade(getClass()), myUserModel);
    disposeOnTearDown(localMessageDispatcher);

    EventsProcessor processor = new EventsProcessor((IDEtalkMessagesWindow) myMock.proxy(), myUserModel, localMessageDispatcher, null);
    disposeOnTearDown(processor);
  }

  public void testIncomingMessage_No_Tab_WithExpand() {
    MessageEvent event = EventFactory.createMessageEvent(new MockTransport(), "bob", "text");

    myMock.expects(once()).method("expandToolWindow");
    myMock.expects(once()).method("newMessageAvailable").with(eq(myUserModel.createUser("bob", MockTransport.NAME)), eq(event));
    myMock.expects(once()).method("getWindow").will(returnValue(null));

    getBroadcaster().fireEvent(event);
  }

  public void testIncomingMessage_No_Tab_WithoutExpand() {
    IdeaFlags.EXPAND_ON_MESSAGE.change(false);

    MessageEvent event = EventFactory.createMessageEvent(new MockTransport(), "bob", "text");
    myMock.expects(once()).method("newMessageAvailable").with(eq(myUserModel.createUser("bob", MockTransport.NAME)), eq(event));
    myMock.expects(once()).method("getWindow").will(returnValue(null));

    getBroadcaster().fireEvent(event);
  }
}
