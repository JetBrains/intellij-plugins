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
package jetbrains.communicator.core.impl.users;

import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.IDEtalkListener;
import jetbrains.communicator.core.TestFactory;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.transport.EventFactory;
import jetbrains.communicator.core.users.GroupEvent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.mock.MockTransport;
import jetbrains.communicator.mock.MockUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kir Maximov
 */
public class UserModelImplTest extends BaseTestCase {
  private UserModel myUserModel;
  private MyIDEtalkListener myListener;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUserModel = TestFactory.createUserListWithUsers(this);
    myListener = new MyIDEtalkListener();
    getBroadcaster().addListener(myListener);
  }

  @Override
  protected void tearDown() throws Exception {
    myListener.clear();
    getBroadcaster().removeListener(myListener);

    super.tearDown();
  }

  public void testAllUsers() {
    User[] all = myUserModel.getAllUsers();
    assertUser("group1", "aaa", all[0]);
    assertUser("group1", "ccc", all[1]);
    assertUser("group2", "zzz", all[2]);
    assertUser("group2", "aaaa", all[3]);
    assertUser("group2", "bbb", all[4]);

    assertSame(all, myUserModel.getAllUsers());
    all[0].setDisplayName("another name", myUserModel);

    assertNotSame(all, myUserModel.getAllUsers());
    all = myUserModel.getAllUsers();

    getBroadcaster().fireEvent(new UserEvent.Offline(all[0]));
    assertNotSame(all, myUserModel.getAllUsers());
    all = myUserModel.getAllUsers();

    myUserModel.addUser(myUserModel.createUser("a", MockTransport.NAME));
    assertNotSame(all, myUserModel.getAllUsers());
    all = myUserModel.getAllUsers();

    myUserModel.removeUser(all[0]);
    assertNotSame(all, myUserModel.getAllUsers());
  }


  public void testReordering() {
    User[] all = myUserModel.getAllUsers();
    all[0].setDisplayName("zzz", myUserModel);

    all = myUserModel.getAllUsers();
    assertUser("group1", "ccc", all[0]);
    assertUser("group1", "zzz", all[1]);
  }

  public void testGrouping() {
    String[] groups = myUserModel.getGroups();
    assertEquals("Wrong groups", Arrays.asList("group1", "group2"),
        Arrays.asList(groups));
  }

  public void testAddGroupExplicitly() {
    myUserModel.addGroup(null);
    myUserModel.addGroup("");
    myUserModel.addGroup(" group15  ");

    assertEquals("group15", ((GroupEvent.Added) myListener.getEvents()[0]).getGroup());
    myListener.assertLog("Group adding message expected", "BeforeAfter");

    // add again
    myUserModel.addGroup(" group15  ");
    myListener.assertLog("No events expected - already in model", "");

    String[] groups = myUserModel.getGroups();
    assertEquals("Wrong groups", Arrays.asList("group1", "group15", "group2"),
        Arrays.asList(groups));
  }

  public void testRemoveGroups() {
    assertFalse(myUserModel.removeGroup("group"));

    myUserModel.addGroup("added1");
    myUserModel.addGroup("added2");

    final MockUser addedUser = new MockUser("some addedUser", "added2");
    myUserModel.addUser(addedUser);

    assertTrue("Do not contain users, can be removed", myUserModel.removeGroup("added1"));
    assertTrue("Contains users, removed including users", myUserModel.removeGroup("group1"));

    assertEquals("Wrong groups after deletion", Arrays.asList("added2", "group2"),
        Arrays.asList(myUserModel.getGroups()));
  }

  public void testRemoveUserFromImplicitGroup() {
    User[] users = myUserModel.getUsers("group1");
    for (User user : users) {
      myUserModel.removeUser(user);
    }

    assertEquals("Implicit group should remain", Arrays.asList("group1", "group2"),
        Arrays.asList(myUserModel.getGroups()));
  }

  public void testRemoveGroups_Event() {
    myUserModel.addGroup("AGroup");
    myListener.clear();

    myUserModel.removeGroup("AGroup");

    assertEquals("AGroup", ((GroupEvent.Removed) myListener.getEvents()[0]).getGroup());
    myListener.assertLog("Group adding message expected", "BeforeAfter");

    myUserModel.removeGroup("AGroup");
    myListener.assertLog("Not in model - no event expected", "");
  }

  public void testGroupMembers() {
    assertEquals(0, myUserModel.getUsers("dfasdff").length);

    User[] group1 = myUserModel.getUsers("group1");
    assertEquals(2, group1.length);
    assertEquals("aaa", group1[0].getName());
    assertEquals("ccc", group1[1].getName());

    User[] group2 = myUserModel.getUsers("group2");
    assertEquals(3, group2.length);
    assertEquals("Online user should go first", "zzz", group2[0].getName());
    assertEquals("aaaa", group2[1].getName());
    assertEquals("bbb", group2[2].getName());
  }

  public void testRenameGroup() {
    myUserModel.renameGroup("group1", "new group");

    assertTrue(myListener.getEvents()[0] instanceof GroupEvent.Updated);

    myListener.assertLog("1 event expected", "BeforeAfter");
  }

  public void testRenameGroup_ExplicitlyAdded() {
    myUserModel.addGroup("new group");
    myUserModel.renameGroup("new group", "new group1");

    assertEquals("Wrong groups after rename",
        Arrays.asList("group1", "group2", "new group1"),
        Arrays.asList(myUserModel.getGroups()));
  }

  public void testStacktrace() {
    int hadUsers = myUserModel.getAllUsers().length;

    getBroadcaster().fireEvent(EventFactory.createStacktraceEvent(new MockTransport(), "aaaaaa", "statcktrace", "comment"));

    assertEquals("Another user should be added", hadUsers + 1, myUserModel.getAllUsers().length);

    User addedUser = myUserModel.getAllUsers()[0];
    assertEquals("aaaaaa", addedUser.getName());
  }

  public void testAddUser() {
    MockUser user = new MockUser("user", null);
    myUserModel.addUser(user);

    assertTrue(myUserModel.hasUser(user));
    myListener.assertLog("Expect messages", "BeforeAfter");

    myUserModel.addUser(user);
    myListener.assertLog("Expect no messages when already in model", "");
  }

  public void testRemoveUser() {
    MockUser user = new MockUser("user", null);
    myUserModel.addUser(user);
    myListener.clear();

    myUserModel.removeUser(user);
    myListener.assertLog("Expect messages", "BeforeAfter");

    myUserModel.removeUser(user);
    myListener.assertLog("Expect no messages when not in model", "");
  }

  private void assertUser(String group, String name, User user) {
    assertEquals(group, user.getGroup());
    assertEquals(name, user.getDisplayName());
  }

  public void testDeleteUserFromList() {
    for (int i = 5; i > 0; i --) {
      assertEquals(i, myUserModel.getAllUsers().length);
      myUserModel.removeUser(myUserModel.getAllUsers()[0]);
      assertEquals(i - 1, myUserModel.getAllUsers().length);
    }
  }

  private static class MyIDEtalkListener implements IDEtalkListener {
    private String myLog;
    private final List<IDEtalkEvent> myEvents = new ArrayList<>();

    MyIDEtalkListener() {
      clear();
    }

    public void clear() {
      myLog = "";
      myEvents.clear();
    }

    public void assertLog(String msg, String expected) {
      assertEquals("Wrong event sequence: " + msg, expected, myLog);
      clear();
    }

    public IDEtalkEvent[] getEvents() {
      return myEvents.toArray(new IDEtalkEvent[0]);
    }

    @Override
    public void beforeChange(IDEtalkEvent event) {
      myEvents.add(event);
      myLog += "Before";
    }

    @Override
    public void afterChange(IDEtalkEvent event) {
      myLog += "After";
    }
  }

}
