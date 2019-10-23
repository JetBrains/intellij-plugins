// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
