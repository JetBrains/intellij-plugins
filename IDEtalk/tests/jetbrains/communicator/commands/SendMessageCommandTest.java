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
package jetbrains.communicator.commands;

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.impl.users.UserModelImpl;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockTransport;
import jetbrains.communicator.mock.MockUser;
import jetbrains.communicator.mock.MockUserListComponent;
import org.jmock.Mock;

/**
 * @author kir
 */
public class SendMessageCommandTest extends BaseTestCase {
  private SendMessageCommand myCommand;

  private String myLog = "";
  private UserModelImpl myUserModel;
  private MockUserListComponent myMockUserListComponent;
  private Mock myFacadeMock;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myMockUserListComponent = new MockUserListComponent();
    myFacadeMock = mock(IDEFacade.class);
    myUserModel = new UserModelImpl(getBroadcaster());
    disposeOnTearDown(myUserModel);
    myCommand = new SendMessageCommand(myUserModel, myMockUserListComponent, (IDEFacade) myFacadeMock.proxy());
  }

  public void testInvokeDialog() {
    MockUser user1 = new MockUser("user1", null);
    MockUser user2 = new MockUser("user2", null);
    MockUser user3 = new MockUser("user3", null);
    myUserModel.addUser(user1);
    myUserModel.addUser(user2);
    myUserModel.addUser(user3);
    myMockUserListComponent.setSelectedNodes(new Object[]{"a group", user2, "grp2", user1});

    myCommand.setMessage("a text");
    myFacadeMock.expects(once()).method("invokeSendMessage").with(
        eq(myUserModel.getAllUsers()),
        eq(new User[]{user2, user1}),
        eq("a text"),
        ANYTHING);

    myCommand.execute();
  }

  public void testInvokeDialogWithExplicitUser() {
    MockUser user1 = new MockUser();
    myUserModel.addUser(user1);
    myCommand.setUser(user1);

    myFacadeMock.expects(once()).method("invokeSendMessage").with(
        eq(myUserModel.getAllUsers()),
        eq(new User[]{user1}),
        eq(""),
        ANYTHING);

    myCommand.execute();
  }

  public void testSendMessage() {
    addEventListener();

    final User user = UserImpl.create("user", MockTransport.NAME);
    doTest("message1", new User[]{user});

    verifySendMessageLocalEvent(user, "message1");
  }

  private MockUser createUser() {
    final MockUser user = new MockUser("user", null) {
      @Override
      public void sendMessage(String comment, EventBroadcaster eventBroadcaster) {
        myLog += toString() + ' ' + comment;
      }
    };
    return user;
  }

  public void testSendNothingWhenNoMessage() {
    final User user = createUser();
    doTest("   \n", new User[]{user});
    assertEquals("Nothing to send", "", myLog);
  }

  public void testSendNothingWhenNoUsers() {
    addEventListener();
    doTest("message", new User[0]);
    assertEquals("Nothing to send", 0, myEvents.size());
  }

  private void doTest(final String message, final User[] users) {

    MockIDEFacade ideFacade = new MockIDEFacade() {
      @Override
      public void invokeSendMessage(User[] availableUsers, User[] defaultRecipients, String message1, SendMessageInvoker runOnOK) {
        runOnOK.doSendMessage(users, message);
      }
    };
    myCommand = new SendMessageCommand(myUserModel, myMockUserListComponent, ideFacade);

    myCommand.execute();
  }

}
