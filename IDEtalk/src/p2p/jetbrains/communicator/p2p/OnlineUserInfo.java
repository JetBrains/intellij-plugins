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
package jetbrains.communicator.p2p;

import jetbrains.communicator.core.users.UserPresence;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Kir
 */
public final class OnlineUserInfo {
  private final InetAddress myAddress;
  private final int myPort;
  private final Collection<String> myProjects;
  private UserPresence myPresence;

  // For tests only:
  public OnlineUserInfo(InetAddress address, int port) {
    this(address, port, new HashSet<>(), new UserPresence(true));
  }

  public OnlineUserInfo(InetAddress address, int port, Collection<String> projects, UserPresence presence) {
    myAddress = address;
    myPort = port;
    myPresence = presence;
    myProjects = projects;
  }

  public void setUserPresence(UserPresence userPresence) {
    myPresence = userPresence;
  }

  public InetAddress getAddress() {
    return myAddress;
  }

  public int getPort() {
    return myPort;
  }

  public Collection<String> getProjects() {
    return myProjects;
  }

  public UserPresence getPresence() {
    return myPresence;
  }

  public void setPresence(UserPresence presence) {
    myPresence = presence;
  }

  public String toString() {
    return "OnlineUserInfo[" + myPresence.toString() + ", " + myProjects + "]";
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final OnlineUserInfo that = (OnlineUserInfo) o;

    if (myPort != that.myPort) return false;
    if (myAddress != null ? !myAddress.equals(that.myAddress) : that.myAddress != null) return false;
    if (myPresence != null ? !myPresence.equals(that.myPresence) : that.myPresence != null) return false;
    if (myProjects != null ? !myProjects.equals(that.myProjects) : that.myProjects != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (myAddress != null ? myAddress.hashCode() : 0);
    result = 29 * result + myPort;
    result = 29 * result + (myProjects != null ? myProjects.hashCode() : 0);
    result = 29 * result + (myPresence != null ? myPresence.hashCode() : 0);
    return result;
  }
}
