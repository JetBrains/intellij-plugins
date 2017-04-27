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

import jetbrains.communicator.core.users.User;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.*;

/**
 * @author Kir
 */
class FoundUsersModel extends DefaultTreeModel {
  public static final String NO_PROJECT_NODE = "<no project>";

  FoundUsersModel(List<User> foundUsers) {
    super(new RootNode(convertToMap(foundUsers)));
  }

  private static Map<String,Collection<User>> convertToMap(List<User> foundUsers) {
    Map<String,Collection<User>> resultMap = new TreeMap<>();
    for (User user : foundUsers) {
      String[] projects = user.getProjects();

      if (projects == null || projects.length == 0) projects = new String[]{NO_PROJECT_NODE};

      for (String project : projects) {
        getUsers(resultMap, project).add(user);
      }
    }
    return resultMap;
  }

  private static Collection<User> getUsers(Map<String,Collection<User>> resultMap, String project) {
    Collection<User> users = (resultMap.get(project));
    if (users == null) {
      users = new TreeSet<>((Comparator)(o1, o2) -> ((User)o1).getName().compareToIgnoreCase(((User)o2).getName()));
      resultMap.put(project, users);
    }
    return users;
  }

  private static class RootNode extends DefaultMutableTreeNode {

    RootNode(Map<String,Collection<User>> users) {
      for (String project : users.keySet()) {
        add(new ProjectNode(project, users.get(project)));
      }
    }
  }

  private static class ProjectNode extends DefaultMutableTreeNode {
    ProjectNode(String projectName, Collection<User> usersInProject) {
      setUserObject(projectName);
      for (User user : usersInProject) {
        add(new UserNode(user));
      }
    }
  }

  private static class UserNode extends DefaultMutableTreeNode {
    UserNode(User user) {
      setUserObject(user);
    }

    public String toString() {
      return ((User) getUserObject()).getName();
    }
  }
}
