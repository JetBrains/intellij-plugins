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

package jetbrains.communicator.core.users;

import jetbrains.communicator.core.EventBroadcaster;

/**
 * @author Kir Maximov
 */
public interface UserModel {
  String DEFAULT_GROUP = "General";
  String AUTO_GROUP = "<Auto>";

  /** create user object but do not add it to contact list */
  User createUser(String userName, String transportCode);

  /** add user to contact list*/
  void addUser(User user);

  /** remove user from contact list*/
  void removeUser(User user);

  String[] getGroups();
  
  /**@return actually set name */
  String renameGroup(String oldGroup, String newGroup);

  User[] getUsers(String groupName);
  User[] getAllUsers();
  boolean hasUser(User user);
  String getGroup(User user);

  /** If group name is empty, no group is created */
  void addGroup(String groupName);
  /** groups are removed with users within */
  boolean removeGroup(String groupName);

  EventBroadcaster getBroadcaster();

  User findUser(String userName, String transportCode);

  boolean forEach(Object[] nodes, UserAction userAction, boolean considerOnlyOnlineUsers);
}
