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

import com.intellij.util.ArrayUtil;
import jetbrains.communicator.core.*;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.*;
import jetbrains.communicator.util.CommunicatorStrings;
import jetbrains.communicator.util.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.Disposable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Kir Maximov
 */
public class UserModelImpl implements UserModel, Disposable {
  protected final Collection<User> myUsers = new HashSet<>();
  protected final Collection<String> myGroups = new HashSet<>();

  protected final transient EventBroadcaster myBroadcaster;
  private final transient MyListener myEventListener;

  protected final transient Object myUsersGroupsLock = new Object();

  private final transient AtomicReference<User[]> myCachedUsers = new AtomicReference<>();

  public UserModelImpl(EventBroadcaster eventBroadcaster) {
    myBroadcaster = eventBroadcaster;
    myEventListener = new MyListener();

    myBroadcaster.addListener(myEventListener);
  }

  @Override
  public void dispose() {
    myBroadcaster.removeListener(myEventListener);
  }

  @Override
  public User createUser(String userName, String transportCode) {
    return UserImpl.create(userName, transportCode);
  }

  @Override
  public void addUser(final User user) {
    synchronized (myUsersGroupsLock) {
      if (myUsers.contains(user)) {
        return;
      }
    }
    if (user.isSelf() && !Pico.isUnitTest()) {
      return;
    }

    myBroadcaster.doChange(new UserEvent.Added(user), () -> {
      synchronized (myUsersGroupsLock) {
        myUsers.add(user);
        myGroups.add(user.getGroup());
      }
    });
  }

  @Override
  public void removeUser(final User user) {
    synchronized (myUsersGroupsLock) {
      if (!myUsers.contains(user)) {
        return;
      }
    }

    myBroadcaster.doChange(new UserEvent.Removed(user), () -> {
      synchronized (myUsersGroupsLock) {
        myUsers.remove(user);
      }
    });
  }

  @Override
  public String[] getGroups() {
    Set<String> result = new TreeSet<>();
    synchronized (myUsersGroupsLock) {
      result.addAll(myGroups);
    }

    String lastGroup = null;
    for (User user : getAllUsers()) {
      if (!user.getGroup().equals(lastGroup)) {
        lastGroup = user.getGroup();
        result.add(lastGroup);
      }
    }
    return ArrayUtil.toStringArray(result);
  }

  @Override
  public User[] getUsers(String groupName) {
    List<User> result = new ArrayList<>();
    for (User user : getAllUsers()) {
      if (user.getGroup().equals(groupName)) {
        result.add(user);
      }
    }
    return result.toArray(new User[0]);
  }

  @NotNull
  @Override
  public User[] getAllUsers() {
    User[] usersList = myCachedUsers.get();
    if (usersList != null) {
      return usersList;
    }

    usersList = getUsersList();
    Arrays.sort(usersList, (u1, u2) -> {
      if (u1.getGroup().equals(u2.getGroup())) {
        return UIUtil.compareUsers(u1, u2);
      }
      return u1.getGroup().compareTo(u2.getGroup());
    });

    return myCachedUsers.compareAndSet(null, usersList) ? usersList : getAllUsers();
  }

  @Override
  public boolean hasUser(User user) {
    synchronized (myUsersGroupsLock) {
      return myUsers.contains(user);
    }
  }

  @Override
  public String getGroup(User user) {
    for (User user1 : getUsersList()) {
      if (user1.equals(user)) {
        return user1.getGroup();
      }
    }
    return null;
  }

  @Override
  public void addGroup(String groupName) {
    if (!com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces(groupName)) {
      final String trimmedName = groupName.trim();

      if (Arrays.asList(getGroups()).contains(trimmedName)) return;

      myBroadcaster.doChange(new GroupEvent.Added(trimmedName), () -> {
        synchronized (myUsersGroupsLock) {
          myGroups.add(trimmedName);
        }
      });
    }
  }

  @Override
  public boolean removeGroup(final String groupName) {
    boolean result = false;
    User[] users = getUsers(groupName);
    for (User user : users) {
      removeUser(user);
      result = true;
    }

    synchronized (myUsersGroupsLock) {
      if (!myGroups.contains(groupName)) {
        return result;
      }
    }

    myBroadcaster.doChange(new GroupEvent.Removed(groupName), () -> {
      synchronized (myUsersGroupsLock) {
        myGroups.remove(groupName);
      }
    });
    return true;
  }

  @Override
  public EventBroadcaster getBroadcaster() {
    return myBroadcaster;
  }

  @Override
  public User findUser(String userName, String transportCode) {
    for (User user : getUsersList()) {
      if (user.getName().equals(userName) && user.getTransportCode().equals(transportCode)) {
        return user;
      }
    }
    return null;
  }

  @Override
  public boolean forEach(Object[] nodes, UserAction userAction, boolean considerOnlyOnlineUsers) {
    final Set<User> users = new LinkedHashSet<>();
    for (Object node : nodes) {
      if (node instanceof User) {
        User user = (User)node;
        if (!considerOnlyOnlineUsers || user.isOnline()) {
          users.add(user);
        }
      }
      else if (node instanceof String) {
        forEach(getUsers(node.toString()), new UserAction() {
          @Override
          public boolean executeAndContinue(User user) {
            users.add(user);
            return true;
          }
        }, considerOnlyOnlineUsers);
      }
      else {
        throw new IllegalArgumentException("Invalid nodes: " + Arrays.asList(nodes));
      }
    }

    for (User user : users) {
      if (!userAction.executeAndContinue(user)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String renameGroup(final String oldGroup, String newGroup) {
    final User[] users = getUsers(oldGroup);
    final String newName = CommunicatorStrings.fixGroup(newGroup);

    myBroadcaster.doChange(new GroupEvent.Updated(oldGroup, newName), () -> {
      synchronized (myUsersGroupsLock) {
        myGroups.remove(oldGroup);
        for (final User user : users) {
          user.setGroup(newName, null);
        }
        myGroups.add(newName);
      }
    });

    return newName;
  }

  private User[] getUsersList() {
    synchronized (myUsersGroupsLock) {
      return myUsers.toArray(new User[0]);
    }
  }

  private class MyListener extends IDEtalkAdapter {
    @Override
    public void afterChange(IDEtalkEvent event) {
      event.accept(new EventVisitor() {
        @Override
        public void visitTransportEvent(TransportEvent event) {
          addUser(event.createUser(UserModelImpl.this));
        }

        @Override
        public void visitUserEvent(UserEvent event) {
          super.visitUserEvent(event);
          myCachedUsers.set(null);
        }
      });
    }
  }
}
