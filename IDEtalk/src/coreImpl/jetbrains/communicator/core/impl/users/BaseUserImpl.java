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

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.util.StringUtil;

/**
 * @author Kir
 */
public abstract class BaseUserImpl implements User {
  public static final String CAN_ACCESS_MY_FILES = "canAccessMyFiles";
  public static final String GROUP = "group";
  public static final String DISPLAY_NAME = "displayName";

  private final String myName;
  private String myGroup;
  private String myDisplayName;
  private boolean myCanAccessMyFiles;

  protected BaseUserImpl(String name, String group) {
    assert name != null;

    myName = name;
    myDisplayName = name;
    myGroup = StringUtil.fixGroup(group);
  }

  public String getName() {
    return myName;
  }

  public String getDisplayName() {
    return myDisplayName;
  }

  public void setDisplayName(final String name, UserModel userModel) {
    if (StringUtil.isEmpty(name)) return;

    if (userModel != null) {
      User inModel = userModel.findUser(getName(), getTransportCode());
      if (inModel != null) {
        ((BaseUserImpl) inModel).setDisplayNameWithEvent(name, userModel.getBroadcaster());
      }
    }
    myDisplayName = name;
  }

  public String getGroup() {
    return myGroup;
  }

  public void setGroup(final String group, UserModel userModel) {
    final String fixedGroup = StringUtil.fixGroup(group);

    if (userModel != null) {
      User inModel = userModel.findUser(getName(), getTransportCode());
      if (inModel != null) {
        ((BaseUserImpl) inModel).setGroupWithEvent(fixedGroup, userModel.getBroadcaster());
      }
    }
    myGroup = fixedGroup;
  }

  public boolean canAccessMyFiles() {
    return myCanAccessMyFiles;
  }

  public void setCanAccessMyFiles(final boolean canAccessMyFiles, UserModel userModel) {
    if (userModel != null) {
      User inModel = userModel.findUser(getName(), getTransportCode());
      if (inModel != null) {
        ((BaseUserImpl) inModel).setCanAccessWithEvent(canAccessMyFiles, userModel.getBroadcaster());
      }
    }

    myCanAccessMyFiles = canAccessMyFiles;
  }

  private void setCanAccessWithEvent(final boolean canAccessMyFiles, EventBroadcaster broadcaster) {
    if (canAccessMyFiles == canAccessMyFiles()) return;
    broadcaster.doChange(new UserEvent.Updated(this, CAN_ACCESS_MY_FILES,
        canAccessMyFiles(), canAccessMyFiles), new Runnable() {
      public void run() {
        myCanAccessMyFiles = canAccessMyFiles;
      }
    });
  }

  private void setGroupWithEvent(final String group, EventBroadcaster broadcaster) {
    if (myGroup.equals(group)) return;

    broadcaster.doChange(new UserEvent.Updated(this, GROUP, myGroup, group), new Runnable() {
      public void run() {
        myGroup = group;
      }
    });
  }

  private void setDisplayNameWithEvent(final String name, EventBroadcaster broadcaster) {
    if (myDisplayName.equals(name)) return;

    broadcaster.doChange(new UserEvent.Updated(this, DISPLAY_NAME, myDisplayName, name), new Runnable() {
      public void run() {
        myDisplayName = name;
      }
    });

  }

  public String toString() {
    return '[' + StringUtil.getShortName(getClass()) + ' ' + myName + ' ' + myGroup + ']';
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BaseUserImpl)) return false;

    final BaseUserImpl baseUser = (BaseUserImpl) o;

    return !(myName != null ? !myName.equals(baseUser.myName) : baseUser.myName != null);

  }

  public int hashCode() {
    return (myName != null ? myName.hashCode() : 0);
  }

}
