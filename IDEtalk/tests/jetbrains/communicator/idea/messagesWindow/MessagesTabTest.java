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

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.impl.ContentImpl;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.dispatcher.LocalMessageDispatcherImpl;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.mock.MockIDEFacade;
import org.jmock.Mock;

import javax.swing.*;

/**
 * @author Kir
 */
public class MessagesTabTest extends BaseTestCase {
  private User myUser;
  private MessagesTab myMessagesTab;
  private Mock myUserMock;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUserMock = mock(User.class);
    myUser = (User) myUserMock.proxy();
    final Mock consoleStub = mock(ConsoleView.class);
    consoleStub.stubs().method(ANYTHING);
    consoleStub.stubs().method("getComponent").will(returnValue(new JLabel()));
    LocalMessageDispatcherImpl localMessageDispatcher = new LocalMessageDispatcherImpl(getBroadcaster(), new MockIDEFacade(getClass()), null);
    disposeOnTearDown(localMessageDispatcher);

    myMessagesTab = new MessagesTab(null, myUser,
        localMessageDispatcher, true) {

      @Override
      protected ConsoleView createConsoleView(Project project) {
        return (ConsoleView) consoleStub.proxy();
      }
    };

    disposeOnTearDown(myMessagesTab);
  }

  public void testSendMessage() {

    expectSendMessage();
    myMessagesTab.getSendButton().doClick();
    assertEquals("Should clear send message area", "", myMessagesTab.getInput());

  }

  private void expectSendMessage() {
    myUserMock.expects(once()).method("sendMessage").with(eq("some text"), eq(getBroadcaster()));

    myMessagesTab.append("some text");
  }

  public void testEnableDisableButtons() {
    assertFalse(myMessagesTab.getSendButton().isEnabled());

    myMessagesTab.append(" ");
    assertFalse(myMessagesTab.getSendButton().isEnabled());

    myMessagesTab.append("a");
    assertTrue(myMessagesTab.getSendButton().isEnabled());
  }

  public void testContent() {
    Content content = new ContentImpl(new JLabel(), "text", true);
    myMessagesTab.attachTo(content);
    assertSame(myMessagesTab, MessagesTab.getTab(content));
    assertSame(content, myMessagesTab.getContent());
  }
}
