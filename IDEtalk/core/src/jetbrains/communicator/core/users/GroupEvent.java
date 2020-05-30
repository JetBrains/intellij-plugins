// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.users;

import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.util.CommunicatorStrings;

/**
 * @author Kir Maximov
 */
public abstract class GroupEvent implements IDEtalkEvent {
  private final String myGroup;

  protected GroupEvent(String group) {
    myGroup = group;
  }

  public String getGroup() {
    return myGroup;
  }

  @Override
  public void accept(EventVisitor visitor) {
    visitor.visitGroupEvent(this);
  }

  public String toString() {
    return CommunicatorStrings.toString(getClass(), myGroup);
  }

  public static class Added extends GroupEvent {
    public Added(String group) {
      super(group);
    }
  }

  public static class Removed extends GroupEvent {
    public Removed(String group) {
      super(group);
    }
  }

  public static class Updated extends GroupEvent {
    private final String myOldGroup;
    private final String myNewGroup;

    public Updated(String oldGroup, String newName) {
      super(null);
      myOldGroup = oldGroup;
      myNewGroup = newName;
    }

    public String getOldGroup() {
      return myOldGroup;
    }

    public String getNewGroup() {
      return myNewGroup;
    }
  }
}
