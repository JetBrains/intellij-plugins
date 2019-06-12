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

import jetbrains.communicator.util.CommunicatorStrings;

import java.util.List;
import java.util.Vector;

/**
 * @author Kir
 */
public final class UserPresence {
  private final PresenceMode myPresenceMode;

  public UserPresence(boolean online) {
    this(online ? PresenceMode.AVAILABLE : PresenceMode.UNAVAILABLE);
  }

  public UserPresence(PresenceMode presenceMode) {
    assert presenceMode != null;
    myPresenceMode = presenceMode;
  }

  public boolean isOnline() {
    return myPresenceMode != PresenceMode.UNAVAILABLE;
  }

  public PresenceMode getPresenceMode() {
    return myPresenceMode;
  }

  public String getDisplayText() {
    switch(myPresenceMode) {
      case UNAVAILABLE: return "";
      case AVAILABLE: return CommunicatorStrings.getMsg("user.presence.online");
      case DND: return CommunicatorStrings.getMsg("user.presence.dnd");
      case AWAY:
        return CommunicatorStrings.getMsg("user.presence.away");
      case EXTENDED_AWAY:
        return CommunicatorStrings.getMsg("user.presence.extended_away");
    }
    return "";
  }

  public static UserPresence fromVector(List userPresence) {
    if (userPresence == null) return new UserPresence(false);

    return new UserPresence(PresenceMode.valueOf(userPresence.get(0).toString()));
  }

  public Vector toVector() {
    Vector result = new Vector();
    result.add(myPresenceMode.toString());
    result.add("0"); // Compatibility issue
    return result;
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public String toString() {
    return "UserPresence[" + myPresenceMode;
  }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final UserPresence that = (UserPresence) o;

    if (myPresenceMode != that.myPresenceMode)
      return false;

    return true;
  }

  public int hashCode() {
    return (myPresenceMode != null ? myPresenceMode.hashCode() : 0);
  }
}
