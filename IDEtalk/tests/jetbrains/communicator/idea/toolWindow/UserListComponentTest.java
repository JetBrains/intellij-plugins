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

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.dispatcher.LocalMessageDispatcherImpl;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.impl.users.UserModelImpl;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockTransport;
import jetbrains.communicator.mock.MockUser;
import jetbrains.communicator.util.TreeUtils;

import javax.swing.*;

/**
 * @author Kir Maximov
 */
public class UserListComponentTest extends BaseTestCase {
  private UserListComponentImpl myUserListComponent;
  private UserModelImpl myUserModel;
  private MockIDEFacade myIdeFacade;
  private EventBroadcaster myBroadcaster;
  private LocalMessageDispatcherImpl myLocalMessageDispatcher;

  @Override
  protected void setUp() throws Exception {
    super.setUp();


    myBroadcaster = getBroadcaster();
    myIdeFacade = new MockIDEFacade(getClass());
    myUserModel = new UserModelImpl(myBroadcaster);
    disposeOnTearDown(myUserModel);
    myLocalMessageDispatcher = new LocalMessageDispatcherImpl(myBroadcaster, myIdeFacade, myUserModel);
    disposeOnTearDown(myLocalMessageDispatcher);
    myUserListComponent = new UserListComponentImpl(myUserModel, myIdeFacade, myLocalMessageDispatcher);
    disposeOnTearDown(myUserListComponent);

    getTree().updateUI(); // Emulate the UI change
  }

  public void testGetSelectedNodes() {
    myUserModel.addGroup("a group");
    MockUser user = new MockUser("user", "group");
    myUserModel.addUser(user);

    JTree jTree = getTree();
    jTree.expandRow(1);

    jTree.setSelectionInterval(0, 2);

    Object[] selectedNodes = myUserListComponent.getSelectedNodes();
    assertEquals("a group", selectedNodes[0]);
    assertEquals("group", selectedNodes[1]);
    assertEquals(user, selectedNodes[2]);
  }

  public void testGetSelectedNodes_WhenEditing() {
    myUserModel.addGroup("a group");

    JTree jTree = getTree();
    jTree.setSelectionRow(0);
    jTree.startEditingAtPath(jTree.getPathForRow(0));

    Object[] selectedNodes = myUserListComponent.getSelectedNodes();
    assertEquals("No nodes should be returned while editing to disable actions", 0, selectedNodes.length);
  }

  private JTree getTree() {
    return myUserListComponent.getTree();
  }

  public void testExpandGroupNodeOnUserAdd() {
    myUserModel.addGroup("a group");
    myUserModel.addUser(new MockUser("user", "a group"));

    assertEquals("User group should be expanded", 2, getTree().getRowCount());
  }

  public void testExpandGroupMessageReceived() {
    MockTransport transport = new MockTransport();
    User user = UserImpl.create("nick", transport.getName());
    user.setGroup("some non-default group", myUserModel);
    myUserModel.addUser(user);
    TreeUtils.collapseAll(getTree());
    assertEquals("Sanity check", 1, getTree().getRowCount());

    myBroadcaster.fireEvent(new TransportEvent(transport, "nick"){});
    assertEquals("User group should be expanded to show incoming message icon",
        2, getTree().getRowCount());
  }

  public void testSaveLoadTreeState() {
    myUserModel.addUser(new MockUser("user1", "group"));
    myUserModel.addUser(new MockUser("user2", "group"));
    myUserModel.addGroup("daaa");
    myUserModel.addUser(new MockUser("user3", "zzzz"));
    TreeUtils.collapseAll(getTree());
    getTree().expandRow(1);
    assertEquals("Sanity check", 5, getTree().getRowCount());

    myUserListComponent.saveState();

    myUserListComponent = new UserListComponentImpl(myUserModel, myIdeFacade, myLocalMessageDispatcher);
    disposeOnTearDown(myUserListComponent);
    assertEquals("Tree state should be restored", 5, getTree().getRowCount());
  }
}
