// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.users;

import jetbrains.communicator.core.EventBroadcaster;
import org.jetbrains.annotations.NotNull;

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

  User @NotNull [] getAllUsers();

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
