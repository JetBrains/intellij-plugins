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

import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.mock.MockTransport;

import java.io.Serializable;

/**
 * @author Kir Maximov
 */
public class UserTest extends BaseTestCase {
  private User myUser;
  private UserModel myUserModel;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUser = UserImpl.create("someUser", MockTransport.NAME);
    myUserModel = createUserModel();
  }

  public void testGroup() {
    assertGroup(UserModel.DEFAULT_GROUP, null);
    assertGroup(UserModel.DEFAULT_GROUP, "");
    assertGroup(UserModel.DEFAULT_GROUP, "  ");
    assertGroup("foo", " foo  ");
  }

  public void testNoTransport() {
    User user = UserImpl.create("user", "ddd");
    assertFalse(user.isOnline());
  }

  public void testIsOnline() {

    assertFalse(myUser.isOnline());

    final MockTransport mockTransport = new MockTransport() {
      @Override
      public UserPresence getUserPresence(User user) {
        return new UserPresence(true);
      }
    };
    Pico.getInstance().registerComponentInstance(mockTransport);

    myUser = UserImpl.create("someUser", MockTransport.NAME);

    assertTrue("Corresponding transport is created - should be online now", myUser.isOnline());
  }

  private void assertGroup(String expectedGroup, String group) {
    myUser.setGroup(group, null);
    assertEquals(expectedGroup, myUser.getGroup());
  }

  public void testSetGroupWithUserModel() {
    assertEquals(UserModel.DEFAULT_GROUP, myUser.getGroup());

    addEventListener();
    myUser.setGroup("anotherGroup", myUserModel);
    assertEquals("No events expected when user is not in model", 0, myEvents.size());
    assertEquals("Group should be changed", "anotherGroup", myUser.getGroup());

    myUserModel.addUser(myUser);
    myEvents.clear();
    User userCopy = UserImpl.create("someUser", MockTransport.NAME);
    userCopy.setGroup("alexandria", myUserModel);

    assertEquals("Group should be changed", "alexandria", userCopy.getGroup());
    assertEquals("Group should be changed for user in Model", "alexandria", myUser.getGroup());
    verifyUpdateEvent("group", "anotherGroup", "alexandria");
  }

  public void testSetDisplayNameWithUserModel() {
    addEventListener();
    myUser.setDisplayName("anotherName", myUserModel);
    assertEquals("No events expected when user is not in model", 0, myEvents.size());
    assertEquals("Display name should be changed", "anotherName", myUser.getDisplayName());

    myUserModel.addUser(myUser);
    myEvents.clear();
    User userCopy = UserImpl.create("someUser", MockTransport.NAME);
    userCopy.setDisplayName("jonny", myUserModel);

    assertEquals("Display name should be changed", "jonny", userCopy.getDisplayName());
    assertEquals("Display name should be changed for user in Model", "jonny", myUser.getDisplayName());
    verifyUpdateEvent("displayName", "anotherName", "jonny");
  }

  private UserModelImpl createUserModel() {
    UserModelImpl userModel = new UserModelImpl(getBroadcaster());
    disposeOnTearDown(userModel);
    return userModel;
  }

  public void testSetEmptyDisplayName() {
    myUserModel.addUser(myUser);

    addEventListener();

    myUser.setDisplayName("  ", myUserModel);
    assertEquals("Empty name - no events", 0, myEvents.size());
    assertEquals("Empty name - not changed", "someUser", myUser.getDisplayName());

    myUser.setDisplayName(null, myUserModel);
    assertEquals("Empty name - no events", 0, myEvents.size());
    assertEquals("Empty name - not changed", "someUser", myUser.getDisplayName());
  }

  public void testSetCanAccess_AnotherUser() {
    User sameUser = UserImpl.create(myUser.getName(), myUser.getTransportCode());

    sameUser.setCanAccessMyFiles(true, myUserModel);
    assertFalse("No change expected", myUser.canAccessMyFiles());

    myUserModel.addUser(myUser);

    sameUser.setCanAccessMyFiles(true, myUserModel);
    assertTrue("Change expected for user in model", myUser.canAccessMyFiles());
  }

  public void testSetCanAccess() {

    assertFalse("Should not be able to access my files",
        myUser.canAccessMyFiles());

    addEventListener();
    myUser.setCanAccessMyFiles(false, myUserModel);
    assertEquals("No change - no events expected", 0, myEvents.size());

    myUser.setCanAccessMyFiles(true, myUserModel);
    assertTrue(myUser.canAccessMyFiles());
    assertEquals("Not in model - no events", 0, myEvents.size());

    myUserModel.addUser(myUser);
    myEvents.clear();

    myUser.setCanAccessMyFiles(false, myUserModel);
    verifyUpdateEvent("canAccessMyFiles", Boolean.TRUE, Boolean.FALSE);
  }

  public void testEquals() {
    assertEquals(myUser, UserImpl.create(myUser.getName(), myUser.getTransportCode()));
  }

  private void verifyUpdateEvent(String property, Serializable oldValue, Serializable newValue) {
    UserEvent.Updated updated = ((UserEvent.Updated) checkEvent(true));
    assertEquals(property, updated.getPropertyName());
    assertEquals(oldValue, updated.getOldValue());
    assertEquals(newValue, updated.getNewValue());
  }
}
