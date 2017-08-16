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
import jetbrains.communicator.mock.MockUser;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kir
 */
public class FoundUsersModelTest extends BaseTestCase {
  public void testNoUsers() {
    FoundUsersModel model = createModel(new ArrayList<>());
    assertNull("Root node - no parent", getRoot(model).getParent());
    assertEquals("No children expected", 0, getRoot(model).getChildCount());
  }

  public void testUserInProject() {
    MockUser bob = new MockUser("BobName", null);
    bob.setProjects(new String[]{"bobProject"});

    FoundUsersModel model = createModel(Arrays.asList(new User[]{bob}));
    assertEquals("One project expected", 1, getRoot(model).getChildCount());
    TreeNode projectNode = getRoot(model).getChildAt(0);
    assertEquals("Invalid project node", "bobProject", projectNode.toString());

    assertEquals("One user in group expected", 1, projectNode.getChildCount());
    assertEquals("Invalid user node", bob.getName(), projectNode.getChildAt(0).toString());
  }

  public void test2Users2Projects() {
    MockUser bob = new MockUser("bob", null);
    MockUser alice = new MockUser("alice", null);

    bob.setProjects(new String[]{"bobProject"});
    alice.setProjects(new String[]{"aliceProject"});
    FoundUsersModel model = createModel(Arrays.asList(new User[]{bob, alice}));
    assertEquals("Two projects expected", 2, getRoot(model).getChildCount());
    assertEquals("One user per project expected", 1, getRoot(model).getChildAt(0).getChildCount());
    assertEquals("One user per project expected", 1, getRoot(model).getChildAt(1).getChildCount());
  }

  public void testUserWithoutProject() {
    MockUser bob = new MockUser("BobName", null);

    FoundUsersModel model = createModel(Arrays.asList(new User[]{bob}));
    assertEquals("One (unnamed) project expected", 1, getRoot(model).getChildCount());
    TreeNode projectNode = getRoot(model).getChildAt(0);
    assertEquals("Invalid project node", "<no project>", projectNode.toString());

    assertEquals("One user in group expected", 1, projectNode.getChildCount());
    assertEquals("Invalid user node", bob.getName(), projectNode.getChildAt(0).toString());
  }

  public void testUserIn2Groups() {
    MockUser bob = new MockUser("BobName", null);
    bob.setProjects(new String[]{"bobProject", "anotherBobProject"});

    FoundUsersModel model = createModel(Arrays.asList(new User[]{bob}));
    assertEquals("2 projects expected", 2, getRoot(model).getChildCount());
    TreeNode projectNode1 = getRoot(model).getChildAt(0);
    assertEquals("Invalid project node", "anotherBobProject", projectNode1.toString());
    TreeNode projectNode2 = getRoot(model).getChildAt(1);
    assertEquals("Invalid project node", "bobProject", projectNode2.toString());

    assertEquals("One user in group expected", 1, projectNode1.getChildCount());
    assertEquals("One user in group expected", 1, projectNode2.getChildCount());
  }

  public void test2UsersInSameGroup() {
    MockUser bob = new MockUser("bob", null);
    MockUser alice = new MockUser("alice", null);

    bob.setProjects(new String[]{"Project"});
    alice.setProjects(new String[]{"Project"});
    FoundUsersModel model = createModel(Arrays.asList(new User[]{bob, alice}));
    assertEquals("One project expected", 1, getRoot(model).getChildCount());
    assertEquals("Two users expected", 2, getRoot(model).getChildAt(0).getChildCount());

    assertEquals("Wrong sorting of users", "alice", getRoot(model).getChildAt(0).getChildAt(0).toString());
    assertEquals("Wrong sorting of users", "bob", getRoot(model).getChildAt(0).getChildAt(1).toString());
  }
  
  private FoundUsersModel createModel(List<User> users) {
    return new FoundUsersModel(users);
  }

  private TreeNode getRoot(FoundUsersModel model) {
    return ((TreeNode) model.getRoot());
  }
}
