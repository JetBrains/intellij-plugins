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
import com.intellij.util.ui.tree.TreeUtil;
import jetbrains.communicator.OptionFlag;
import jetbrains.communicator.core.TestFactory;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.dispatcher.LocalMessageDispatcherImpl;
import jetbrains.communicator.core.transport.EventFactory;
import jetbrains.communicator.core.users.SettingsChanged;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockMessage;
import jetbrains.communicator.mock.MockTransport;

import javax.swing.*;

/**
 * @author kir
 */
public class UsersTreeModel_OnlyOfflineShownTest extends BaseTestCase {
  private UserModel myUserModel;
  private UsersTreeModel myUsersTreeModel;
  private JTree myTree;
  private LocalMessageDispatcherImpl myLocalMessageDispatcher;
  private MockIDEFacade myIDEFacade;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUserModel = TestFactory.createUserListWithUsers(this);
    myTree = new Tree();
    myIDEFacade = new MockIDEFacade(getClass());
    myLocalMessageDispatcher = new LocalMessageDispatcherImpl(getBroadcaster(), myIDEFacade, myUserModel);
    disposeOnTearDown(myLocalMessageDispatcher);
    myUsersTreeModel = new UsersTreeModel(myTree, myUserModel, myLocalMessageDispatcher);
    disposeOnTearDown(myUsersTreeModel);
    myTree.setModel(myUsersTreeModel);

    OptionFlag.OPTION_HIDE_OFFLINE_USERS.change(true);
    getBroadcaster().fireEvent(new SettingsChanged());
  }

  public void testHideOfflineUsers() {
    myTree.expandRow(2);
    myTree.expandRow(1);

    assertEquals("1 group with non-online user + 1 user online + root",  2 + 1, myTree.getRowCount());

    OptionFlag.OPTION_HIDE_OFFLINE_USERS.change(false);
    getBroadcaster().fireEvent(new SettingsChanged());
    myTree.expandRow(3);
    myTree.expandRow(2);
    myTree.expandRow(1);
    assertEquals(7 + 1, myTree.getRowCount());
  }

  public void testOfflineUserWithMessage() {
    myTree.expandRow(2);
    myTree.expandRow(1);

    myIDEFacade.setReturnedMessage(new MockMessage());
    getBroadcaster().fireEvent(EventFactory.createMessageEvent(new MockTransport(),
        myUserModel.getUsers("group1")[0].getName(), "text"));

    TreeUtil.expandAll(myTree);
    assertEquals("Should include user with pending local message",
       5, myTree.getRowCount());
  }
}
