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
import jetbrains.communicator.util.StringUtil;

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

  public void accept(EventVisitor visitor) {
    visitor.visitGroupEvent(this);
  }

  public String toString() {
    return StringUtil.toString(getClass(), myGroup);
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
