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

import jetbrains.communicator.core.*;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.*;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.UIUtil;
import org.picocontainer.Disposable;

import java.util.*;

/**
 * @author Kir Maximov
 */
public class UserModelImpl implements UserModel, Disposable {

  protected final Collection<User> myUsers = new HashSet<User>();
  protected final Collection<String> myGroups = new HashSet<String>();

  protected final transient EventBroadcaster myBroadcaster;
  private transient MyListener myEventListener;

  private final transient Object myCachedUsersLock = new Object();
  protected final transient Object myUsersGroupsLock = new Object();

  private transient User[] myCachedUsers;

  public UserModelImpl(EventBroadcaster eventBroadcaster) {
    myBroadcaster = eventBroadcaster;
    myEventListener = new MyListener();

    myBroadcaster.addListener(myEventListener);
  }

  public void dispose() {
    myBroadcaster.removeListener(myEventListener);
  }

  public User createUser(String userName, String transportCode) {
    return UserImpl.create(userName, transportCode);
  }

  public void addUser(final User user) {
    if (_getUsers().contains(user)) return;
    if (user.isSelf() && !Pico.isUnitTest()) return;

    myBroadcaster.doChange(new UserEvent.Added(user), new Runnable() {
      public void run() {
        synchronized (myUsersGroupsLock) {
          myUsers.add(user);
          myGroups.add(user.getGroup());
        }
      }
    });
  }

  public void removeUser(final User user) {
    if (!_getUsers().contains(user)) return;

    myBroadcaster.doChange(new UserEvent.Removed(user), new Runnable() {
      public void run() {
        synchronized (myUsersGroupsLock) {
          myUsers.remove(user);
        }
      }
    });
  }

  public String[] getGroups() {
    Set<String> result = new TreeSet<String>();
    synchronized(myUsersGroupsLock) {
      result.addAll(myGroups);
    }

    String lastGroup = null;
    for (User user : getAllUsers()) {
      if (!user.getGroup().equals(lastGroup)) {
        lastGroup = user.getGroup();
        result.add(lastGroup);
      }
    }
    return result.toArray(new String[result.size()]);
  }

  public User[] getUsers(String groupName) {
    List<User> result = new ArrayList<User>();
    for (User user : getAllUsers()) {
      if (user.getGroup().equals(groupName)) {
        result.add(user);
      }
    }
    return result.toArray(new User[result.size()]);
  }

  public User[] getAllUsers() {
    synchronized (myCachedUsersLock) {
      if (myCachedUsers == null) {
        Collection<User> _users = _getUsers();
        myCachedUsers = _users.toArray(new User[_users.size()]);

        Arrays.sort(myCachedUsers, new Comparator<User>() {
          public int compare(User u1, User u2) {

            if (u1.getGroup().equals(u2.getGroup())) {

              return UIUtil.compareUsers(u1, u2);

            }

            return u1.getGroup().compareTo(u2.getGroup());
          }
        });
      }
      return myCachedUsers;
    }
  }

  public boolean hasUser(User user) {
    synchronized(myUsersGroupsLock) {
      return _getUsers().contains(user);
    }
  }

  public String getGroup(User user) {
    for (User user1 : _getUsers()) {
      if (user1.equals(user)) return user1.getGroup();
    }
    return null;
  }

  public void addGroup(String groupName) {
    if (StringUtil.isNotEmpty(groupName)) {
      final String trimmedName = groupName.trim();

      if (Arrays.asList(getGroups()).contains(trimmedName)) return;
      
      myBroadcaster.doChange(new GroupEvent.Added(trimmedName), new Runnable() {
        public void run() {
          synchronized(myUsersGroupsLock) {
            myGroups.add(trimmedName);
          }
        }
      });
    }
  }

  public boolean removeGroup(final String groupName) {

    boolean result = false;
    User[] users = getUsers(groupName);
    for (User user : users) {
      removeUser(user);
      result = true;
    }

    synchronized(myUsersGroupsLock) {
      if (!myGroups.contains(groupName)) {
        return result;
      }
    }

    myBroadcaster.doChange(new GroupEvent.Removed(groupName), new Runnable() {
      public void run() {
        synchronized (myUsersGroupsLock) {
          myGroups.remove(groupName);
        }
      }
    });
    return true;
  }

  public EventBroadcaster getBroadcaster() {
    return myBroadcaster;
  }

  public User findUser(String userName, String transportCode) {
    for (User user : _getUsers()) {
      if (user.getName().equals(userName) && user.getTransportCode().equals(transportCode)) {
        return user;
      }
    }
    return null;
  }

  public boolean forEach(Object[] nodes, UserAction userAction, boolean considerOnlyOnlineUsers) {
    final Set<User> users = new LinkedHashSet<User>();
    for (Object node : nodes) {
      if (node instanceof User) {
        User user = (User) node;
        if (!considerOnlyOnlineUsers || user.isOnline()) {
          users.add(user);
        }
      }
      else if (node instanceof String) {
        forEach(getUsers(node.toString()), new UserAction() {
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

  public String renameGroup(final String oldGroup, String newGroup) {
    final User[] users = getUsers(oldGroup);
    final String newName = StringUtil.fixGroup(newGroup);

    myBroadcaster.doChange(new GroupEvent.Updated(oldGroup, newName), new Runnable() {
      public void run() {
        synchronized(myUsersGroupsLock) {
          myGroups.remove(oldGroup);
          for (final User user : users) {
            user.setGroup(newName, null);
          }
          myGroups.add(newName);
        }
      }
    });

    return newName;
  }

  private Collection<User> _getUsers() {
    synchronized(myUsersGroupsLock) {
      return new HashSet<User>(myUsers);
    }
  }

  private class MyListener extends IDEtalkAdapter {
    public void afterChange(IDEtalkEvent event) {
      event.accept(new EventVisitor(){
        @SuppressWarnings({"RefusedBequest"})
        public void visitTransportEvent(TransportEvent event) {
          addUser(event.createUser(UserModelImpl.this));
        }

        public void visitUserEvent(UserEvent event) {
          super.visitUserEvent(event);
          synchronized(myCachedUsersLock) {
            myCachedUsers = null;
          }
        }
      });
    }
  }
}
