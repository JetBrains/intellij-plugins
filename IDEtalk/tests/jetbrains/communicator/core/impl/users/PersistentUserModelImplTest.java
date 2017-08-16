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

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.ConcurrencyUtil;
import jetbrains.communicator.core.TestFactory;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.mock.MockIDEFacade;
import jetbrains.communicator.mock.MockTransport;
import jetbrains.communicator.mock.MockUser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author kir
 */
public class PersistentUserModelImplTest extends BaseTestCase {
  private PersistentUserModelImpl myUserModel;
  private File myTempDir;
  private boolean mySaved;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myTempDir = TestFactory.createDir(getName());
    myUserModel = createModelInstance();
    assert !mySaved;
  }

  private PersistentUserModelImpl createModelInstance() {
    PersistentUserModelImpl persistentUserModel = new PersistentUserModelImpl(getBroadcaster(), new MockIDEFacade(myTempDir));
    disposeOnTearDown(persistentUserModel);
    return persistentUserModel;
  }

  public void testFileTemplate() throws Exception {
    myUserModel.saveAll();

    File file = new File(myTempDir, PersistentUserModelImpl.FILE_NAME);
    String fileText = FileUtil.loadFile(file);
    assertEquals("Invalid file template",
        "<users>\n" +
        "  <myUsers class=\"set\"/>\n" +
        "  <myGroups class=\"set\"/>\n" +
        "</users>",
        fileText);
  }

  public void testPersistUser() {
    myUserModel.addUser(new MockUser("user", "group"));

    PersistentUserModelImpl modelInstance = createModelInstance();
    User[] users = modelInstance.getAllUsers();
    assertEquals(1, users.length);
    assertEquals("user", users[0].getName());
    assertEquals("group", users[0].getGroup());
  }

  public void testAddAndChangeUser() {
    myUserModel.addUser(new MockUser("user", "group"));
    checkSaved(true);
    User user = myUserModel.getAllUsers()[0];
    user.setDisplayName("dds dds", myUserModel);
    checkSaved(true);
    user.setGroup("newGroup", myUserModel);
    checkSaved(true);
    user.setCanAccessMyFiles(true, myUserModel);
    checkSaved(true);
    getBroadcaster().fireEvent(new UserEvent.Updated(user, "presence", null, null));
    checkSaved(false);

    PersistentUserModelImpl modelInstance = createModelInstance();
    User[] users = modelInstance.getAllUsers();
    assertEquals(1, users.length);
    assertEquals("dds dds", users[0].getDisplayName());
    assertEquals("newGroup", users[0].getGroup());
  }

  private void checkSaved(boolean expectedSaved) {
    assertEquals(expectedSaved, myUserModel.testSaved());
    mySaved = false;
  }

  public void testPersistGroup() {
    myUserModel.addGroup("a group");

    PersistentUserModelImpl modelInstance = createModelInstance();
    String[] groups = modelInstance.getGroups();
    assertEquals(1, groups.length);
    assertEquals("a group", groups[0]);
  }

  public void testConcurrentModifications() {
    Runnable createUserCommand = () -> {
      User user = myUserModel.createUser("bob" + System.nanoTime(), MockTransport.NAME);
      myUserModel.addUser(user);
    };
    Runnable createGroupCommand = () -> myUserModel.addGroup("group" + System.nanoTime());

    Runnable removeUserCommand = () -> {
      User[] allUsers = myUserModel.getAllUsers();
      if (allUsers.length > 0) {
        myUserModel.removeUser(allUsers[0]);
      }
    };
    Runnable removeGroupCommand = () -> {
      String[] groups = myUserModel.getGroups();
      if (groups.length > 0) {
        myUserModel.removeGroup(groups[0]);
      }
    };

    Collection<Thread> threads = new ArrayList<>();
    for(int i = 0; i < 20; i ++) {
      threads.add(new Thread(createCycle(createGroupCommand), "create group"));
      threads.add(new Thread(createCycle(createUserCommand), "create user"));
      threads.add(new Thread(createCycle(removeUserCommand), "remove user"));
      threads.add(new Thread(createCycle(removeGroupCommand), "remove group"));
    }
    for (Thread thread : threads) {
      thread.start();
    }
    ConcurrencyUtil.joinAll(threads);

    System.out.println("myUserModel.getAllUsers().length = " + myUserModel.getAllUsers().length);
    System.out.println("myUserModel.getGroups().length = " + myUserModel.getGroups().length);
  }

  private Runnable createCycle(final Runnable r) {
    return () -> {
      for (int i = 0; i < 10; i ++) {
        try {
          r.run();
        } catch (RuntimeException e) {
          ourShouldFail = true;
          throw e;
        }
      }
    };
  }

}
