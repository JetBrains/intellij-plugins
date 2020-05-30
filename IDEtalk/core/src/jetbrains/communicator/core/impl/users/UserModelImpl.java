// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl.users;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtilRt;
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
    Set<String> result;
    synchronized (myUsersGroupsLock) {
      result = new TreeSet<>(myGroups);
    }

    String lastGroup = null;
    for (User user : getAllUsers()) {
      if (!user.getGroup().equals(lastGroup)) {
        lastGroup = user.getGroup();
        result.add(lastGroup);
      }
    }
    return ArrayUtilRt.toStringArray(result);
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

  @Override
  public User @NotNull [] getAllUsers() {
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
    if (!StringUtil.isEmptyOrSpaces(groupName)) {
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
