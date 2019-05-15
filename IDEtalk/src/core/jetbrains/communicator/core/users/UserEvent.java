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

import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.util.CommunicatorStrings;
import org.jetbrains.annotations.NonNls;

/**
 * @author Kir Maximov
 */
public abstract class UserEvent implements IDEtalkEvent {
  private final User myUser;

  public UserEvent(User user) {
    assert user != null;
    myUser = user;
  }

  public User getUser() {
    return myUser;
  }

  @Override
  public void accept(EventVisitor visitor) {
    visitor.visitUserEvent(this);
  }

  public String toString() {
    return CommunicatorStrings.toString(getClass(), myUser);
  }

  public static class Added extends UserEvent {
    public Added(User user) {
      super(user);
    }

    @Override
    public void accept(EventVisitor visitor) {
      visitor.visitUserAdded(this);
    }
  }

  public static class Removed extends UserEvent {
    public Removed(User user) {
      super(user);
    }

    @Override
    public void accept(EventVisitor visitor) {
      visitor.visitUserRemoved(this);
    }
  }

  public static class Updated extends UserEvent {
    private final String myPropertyName;
    private final Object myOldValue;
    private final Object myNewValue;
    @NonNls
    public static final String GROUP = "group";
    @NonNls
    public static final String DISPLAY_NAME = "displayName";
    @NonNls
    public static final String PRESENCE = "presence";

    public Updated(User user, String propertyName, Object oldValue, Object newValue) {
      super(user);
      myPropertyName = propertyName;
      myOldValue = oldValue;
      myNewValue = newValue;
    }

    public String getPropertyName() {
      return myPropertyName;
    }

    public Object getOldValue() {
      return myOldValue;
    }

    public Object getNewValue() {
      return myNewValue;
    }

    @Override
    public void accept(EventVisitor visitor) {
      visitor.visitUserUpdated(this);
    }

    public String toString() {
      return CommunicatorStrings.toString(getClass(), new Object[]{
        getUser(), myPropertyName, myOldValue, myNewValue
      });
    }
  }

  public static class Online extends Updated {
    public Online(User user) {
      super(user, "online", Boolean.FALSE, Boolean.TRUE);
    }

    @Override
    public void accept(EventVisitor visitor) {
      visitor.visitUserOnline(this);
    }
  }

  public static class Offline extends Updated {
    public Offline(User user) {
      super(user, "online", Boolean.TRUE, Boolean.FALSE);
    }
  }

}
