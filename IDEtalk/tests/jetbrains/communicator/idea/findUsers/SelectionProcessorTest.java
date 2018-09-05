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
package jetbrains.communicator.idea.findUsers;

import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.mock.MockUser;
import jetbrains.communicator.util.KirTree;
import jetbrains.communicator.util.TreeUtils;

import javax.swing.*;
import java.util.Arrays;
import java.util.Set;

/**
 * @author Kir
 */
public class SelectionProcessorTest extends BaseTestCase {
  private KirTree myTree;
  private JComboBox myGroupSelector;
  private SelectionProcessor mySelectionProcessor;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myTree = new KirTree();
    setUsers(new User[]{user("foo", null)});
    myGroupSelector = new JComboBox();
    mySelectionProcessor = new SelectionProcessor(myTree, myGroupSelector,
        new String[]{"developers", "bosses"});
  }

  public void testGroupSelectorInitialization() {
    assertEquals("Should add 'auto' item", UserModel.AUTO_GROUP,
        myGroupSelector.getModel().getElementAt(0));
    assertEquals("bosses", myGroupSelector.getModel().getElementAt(1));
    assertEquals("developers", myGroupSelector.getModel().getElementAt(2));
    assertEquals(3, myGroupSelector.getModel().getSize());
  }

  public void testGetSelectedUsers_UserSelected() {
    setUsers(new User[]{user("bob", "Fabrique")});
    myTree.setSelectionRow(1);
    assertSelectedUsers(new String[]{"bob"});
  }

  public void testGetSelectedUsers_GroupSelected() {
    setUsers(new User[]{user("bob", "Fabrique"), user("alice", "Fabrique")});
    myTree.setSelectionRow(0);
    assertSelectedUsers(new String[]{"bob", "alice"});
  }

  public void testGetSelectedUsers_UsersAndGroupSelected() {
    setUsers(new User[]{user("bob", "Fabrique"), user("alice", "Idea")});
    myTree.setSelectionRow(0);
    myTree.addSelectionRow(3);
    assertSelectedUsers(new String[]{"bob", "alice"});
  }

  public void testDefaultUserSelection() {
    assertEquals("Default selection expected", 0, myTree.getSelectionRows()[0]);
  }

  public void testSelectUserWhenHisGroupSelected() {
    setUsers(new User[]{user("bob", "Idea")});
    myTree.setSelectionRow(0);
    myTree.addSelectionRow(1);
    assertEquals("Only user should be selected: " + selection(), 1, myTree.getSelectionRows()[0]);
  }

  private String selection() {
    return Arrays.asList(myTree.getSelectionPaths()).toString();
  }

  public void testSelectGroupWhenUserSelected() {
    setUsers(new User[]{user("bob", "Idea")});
    myTree.setSelectionRow(1);
    myTree.addSelectionRow(0);
    assertEquals("Only group should be selected: " + selection(), 0, myTree.getSelectionRows()[0]);
  }

  public void testSelectGroupAndUser_AtOnce() {
    setUsers(new User[]{user("bob", "Idea")});
    myTree.setSelectionInterval(0, 100);
    assertEquals("Only group should be selected: " + selection(), 0, myTree.getSelectionRows()[0]);
  }

  public void testSelectUser_FromUnnamedProject() {
    setUsers(new User[]{user("bob", null)});
    myTree.setSelectionRow(1);

    assertEquals("Expect general group name", UserModel.DEFAULT_GROUP, myGroupSelector.getSelectedItem());
  }

  public void testSelectUser_FromNamedProject() {
    setUsers(new User[]{user("bob", "Irida")});
    myTree.setSelectionRow(1);

    assertEquals("Expect project group name", "Irida", myGroupSelector.getSelectedItem());
  }

  public void testSelectUsers_FromDifferentProjects() {
    setUsers(new User[]{user("bob", "Irida"), user("anton", "IDEtalk")});
    myTree.setSelectionRows(new int[]{1,2});

    assertEquals("Expect <Auto> group name", UserModel.AUTO_GROUP, myGroupSelector.getSelectedItem());
  }

  private void assertSelectedUsers(String[] userNames) {

    Set<User> selectedUsers = mySelectionProcessor.getSelectedUsers();

    assertEquals("Wrong number of selected users: " + selectedUsers,
        userNames.length, selectedUsers.size());
    for (User user : selectedUsers) {
      assertTrue("Unknown user:" + user, Arrays.asList(userNames).contains(user.getName()));
    }
  }

  private void setUsers(User[] users) {
    myTree.setModel(new FoundUsersModel(Arrays.asList(users)));
    TreeUtils.expandAll(myTree);
  }

  private static User user(String userName, String project) {
    MockUser user = new MockUser(userName, null);
    if (project != null) {
      user.setProjects(new String[]{project});
    }
    return user;
  }


}
