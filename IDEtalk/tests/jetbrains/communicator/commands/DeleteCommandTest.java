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

import com.intellij.util.ArrayUtil;
import jetbrains.communicator.BaseTestCase;
import jetbrains.communicator.core.impl.users.UserModelImpl;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.UserListComponent;
import jetbrains.communicator.mock.MockUser;
import org.jmock.Mock;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kir
 */
public class DeleteCommandTest extends BaseTestCase {
  private DeleteCommand myCommand;

  private Mock myUserListComponentMock;
  private Mock myIDEFacade;

  private UserModelImpl myUserModel;
  private static final String GROUP_NAME = "aGroup";

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUserListComponentMock = mock(UserListComponent.class);
    myIDEFacade = mock(IDEFacade.class);
    myUserModel = new UserModelImpl(getBroadcaster());
    disposeOnTearDown(myUserModel);
    myCommand = new DeleteCommand(myUserModel,
        (UserListComponent)myUserListComponentMock.proxy(), (IDEFacade) myIDEFacade.proxy()){
      @Override
      protected boolean isFocused() {
        return true;
      }
    };
  }

  public void testIsEnabled_NoSelection() throws Exception {
    myUserListComponentMock.stubs().method("getSelectedNodes").will(returnValue(ArrayUtil.EMPTY_OBJECT_ARRAY));
    assertFalse("No selection - action should be disabled", myCommand.isEnabled());
  }

  public void testIsEnabled_WithSelection() throws Exception {
    myUserListComponentMock.stubs().method("getSelectedNodes").will(returnValue(new Object[]{"grp"}));
    assertTrue("Action should be enabled", myCommand.isEnabled());
  }

  public void testDeleteGroup_Commit() throws Exception {
    _testDeleteGroup(true, 0);
  }

  public void testDeleteGroup_WithUsers() throws Exception {
    myUserModel.addUser(new MockUser("user", GROUP_NAME));

    myUserListComponentMock.stubs().method("getSelectedNodes").will(returnValue(new Object[]{GROUP_NAME}));

    assertTrue("Deleteion of group should be enabled - we delete groups with users", myCommand.isEnabled());

    myIDEFacade.expects(once()).method("askQuestion").will(returnValue(true));

    myCommand.execute();
    assertEquals(0, myUserModel.getAllUsers().length);
    assertEquals(0, myUserModel.getGroups().length);
  }

  public void testDeleteGroup_WithUsers_DeleteAll() throws Exception {
    myUserModel.addGroup(GROUP_NAME);
    MockUser user = new MockUser("user", GROUP_NAME);
    myUserModel.addUser(user);

    myUserListComponentMock.stubs().method("getSelectedNodes").
        will(returnValue(new Object[]{GROUP_NAME, user}));

    assertTrue("Want to delete group with users in it", myCommand.isEnabled());

    myIDEFacade.expects(once()).method("askQuestion").will(returnValue(true));

    myCommand.execute();
    assertEquals(0, myUserModel.getAllUsers().length);
    assertEquals(0, myUserModel.getGroups().length);
  }

  public void testDeleteGroup_CancelOperation() throws Exception {
    _testDeleteGroup(false, 1);
  }

  public void testDeleteUser() throws Exception {
    MockUser user = new MockUser();
    myUserModel.addUser(user);
    myUserListComponentMock.expects(once()).method("getSelectedNodes").will(returnValue(new Object[]{user}));
    myIDEFacade.expects(once()).method("askQuestion").will(returnValue(true));

    myCommand.execute();

    assertEquals("User should be deleted", 0, myUserModel.getAllUsers().length);
  }

  private void _testDeleteGroup(boolean userResponse, int remainingGroups) {
    myUserModel.addGroup(GROUP_NAME);
    myIDEFacade.expects(once()).method("askQuestion").will(returnValue(userResponse));
    myUserListComponentMock.expects(once()).method("getSelectedNodes").will(returnValue(new Object[]{GROUP_NAME}));

    myCommand.execute();

    assertEquals(remainingGroups, myUserModel.getGroups().length);
  }

  public void testMessages_NoUsersInGroups() throws Exception {
    assertMessage("user User 0", 1, 0);
    assertMessage("users User 0 and User 1", 2, 0);
    assertMessage("users User 0, User 1, and User 2", 3, 0);

    assertMessage("group \"group0\"", 0, 1);
    assertMessage("groups \"group0\" and \"group1\"", 0, 2);

    assertMessage("group \"group0\" and user User 0", 1, 1);
    assertMessage("groups \"group0\", \"group1\" \nand user User 0", 1, 2);
    assertMessage("group \"group0\" and \nusers User 0, User 1 \nfrom other groups", 2, 1);
    assertMessage("groups \"group0\", \"group1\" \nand \nusers User 0, User 1 \nfrom other groups", 2, 2);
  }

  public void testMessages_WithUsersInDeletedGroups() throws Exception {
    myUserModel.addUser(new MockUser("userName", "aGroup"));
    assertMessage("group \"aGroup\" with its 1 user", new Object[]{"aGroup"});
    assertMessage("groups \"aGroup\"(1 user) and \"group2\"", new Object[]{"aGroup", "group2"});

    myUserModel.addUser(new MockUser("userName1", "aGroup"));
    assertMessage("group \"aGroup\" with its 2 users", new Object[]{"aGroup"});
    assertMessage("groups \"aGroup\"(2 users) and \"group2\"", new Object[]{"aGroup", "group2"});

    assertMessage("group \"aGroup\"(2 users) and user Some Another User",
        new Object[]{"aGroup", new MockUser("Some Another User", null)});
  }

  public void testMessageIgnoresUserFromDeletedGroup() throws Exception {
    MockUser user = new MockUser("userName", "aGroup");
    myUserModel.addUser(user);
    assertMessage("group \"aGroup\" with its 1 user", new Object[]{"aGroup", user});
  }

  private void assertMessage(String msg, int users, int emptyGroups) throws UnknownHostException {
    List nodes = new ArrayList();
    for (int i = 0; i < users; i ++){
      MockUser u = new MockUser("user" + i, "");
      u.setDisplayName("User " + i, myUserModel);
      nodes.add(u);
    }
    for (int i = 0; i < emptyGroups; i ++){
      nodes.add("group" + i);
    }

    Object[] selectedNodes = nodes.toArray(new Object[nodes.size()]);
    assertMessage(msg, selectedNodes);
  }

  private void assertMessage(String msg, Object[] selectedNodes) {
    String result = myCommand.buildQuestion(selectedNodes);
    assertEquals("Wrong message", DeleteCommand.QUESTION_PREFIX + msg + "?", result);
  }

}
