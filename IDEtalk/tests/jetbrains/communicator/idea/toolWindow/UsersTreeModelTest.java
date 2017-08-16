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
package jetbrains.communicator.idea.toolWindow;

import com.intellij.ui.treeStructure.Tree;
import jetbrains.communicator.core.TestFactory;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.dispatcher.LocalMessageDispatcherImpl;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockUser;
import jetbrains.communicator.util.TreeUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * @author kir
 */
public class UsersTreeModelTest extends BaseTestCase {
  private UserModel myUserModel;
  private UsersTreeModel myUsersTreeModel;
  private JTree myTree;
  private LocalMessageDispatcherImpl myLocalMessageDispatcher;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUserModel = TestFactory.createUserListWithUsers(this);
    myTree = new Tree();
    myLocalMessageDispatcher = new LocalMessageDispatcherImpl(getBroadcaster(), new MockIDEFacade(getClass()), myUserModel);
    disposeOnTearDown(myLocalMessageDispatcher);
    myUsersTreeModel = new UsersTreeModel(myTree, myUserModel, myLocalMessageDispatcher);
    disposeOnTearDown(myUsersTreeModel);
    myTree.setModel(myUsersTreeModel);
  }

  public void testUserDeleted() {
    myUserModel.removeGroup("group2");
    assertEquals(0, myTree.getSelectionRows()[0]);

    myUserModel.removeGroup("group1");
    assertEquals(0, myTree.getSelectionRows()[0]);
  }

  public void testUserNameChanged() {

    final User user = myUserModel.getAllUsers()[0];
    assertEquals("aaa", user.getName());

    TreePath path = new TreePath(new DefaultMutableTreeNode(user));
    myUsersTreeModel.valueForPathChanged(path, "new name");

    assertEquals("new name", user.getDisplayName());
  }

  public void testGroupNameChanged() {
    TreePath path = new TreePath(new DefaultMutableTreeNode("group1"));

    myUsersTreeModel.valueForPathChanged(path, "new name");

    assertEquals(0, myUserModel.getUsers("group1").length);
    assertEquals("user groups should be changed from group1 to 'new name'",
        2, myUserModel.getUsers("new name").length);
    assertEquals("new name",
        ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject());
  }

  public void testGroupNameChanged_EmptyGroup() {
    TreePath path = new TreePath(new DefaultMutableTreeNode("group1"));

    myUsersTreeModel.valueForPathChanged(path, "");

    assertEquals(0, myUserModel.getUsers("group1").length);
    assertEquals("user groups should be changed from group1 to 'General'",
        2, myUserModel.getUsers(UserModel.DEFAULT_GROUP).length);
    assertEquals(UserModel.DEFAULT_GROUP,
        ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject());
  }

  public void testRebuildTreeOnUserNameChange() {
    myTree.expandRow(2);
    myTree.expandRow(1);

    assertEquals("All expanded, root node is hidden", 8, myTree.getRowCount());

    // rename user aaa
    myUserModel.getUsers("group1")[0].setDisplayName("xxxx", myUserModel);

    assertEquals("Nodes expansion should be preserved", 8, myTree.getRowCount());

    assertEquals("Tree rebuild expected", "ccc", getUserForRow(myTree, 2).getDisplayName());
    assertEquals("Tree rebuild expected", "xxxx", getUserForRow(myTree, 3).getDisplayName());
  }

  public void testRebuildTreeOnOnlineStatusChange() {
    myTree.expandRow(2);
    myTree.expandRow(1);

    assertEquals("All expanded, root node is hidden", 8, myTree.getRowCount());

    // rename user aaa
    MockUser mockUser = ((MockUser) myUserModel.getUsers("group1")[1]);
    mockUser.setOnline(true);
    getBroadcaster().fireEvent(new UserEvent.Online(mockUser));

    assertEquals("Nodes expansion should be preserved", 8, myTree.getRowCount());

    assertEquals("Online user should go to the top", "ccc", getUserForRow(myTree, 2).getDisplayName());
  }

  public void testRebuildTreeOnGroupChange() {
    myTree.expandRow(2);
    myTree.expandRow(1);

    assertEquals("All expanded, root node is hidden", 8, myTree.getRowCount());

    myUserModel.renameGroup("group1", "new name");

    assertEquals("Nodes expansion should be preserved", 8, myTree.getRowCount());

    assertEquals("Tree rebuild expected, group2 should be the first in the list",
        "zzz", getUserForRow(myTree, 2).getDisplayName());
  }

  public void testExplicitGroup() {
    myUserModel.addGroup("aaaaaaaa");

    assertEquals("aaaaaaaa", TreeUtils.getUserObject(myTree.getPathForRow(1)));
    myUserModel.renameGroup("aaaaaaaa", "aaaaaaaa1");

    assertEquals("aaaaaaaa1", TreeUtils.getUserObject(myTree.getPathForRow(1)));
  }

  private User getUserForRow(final JTree tree, int row) {
    return ((User) ((DefaultMutableTreeNode) tree.getPathForRow(row).getLastPathComponent()).getUserObject());
  }

}
