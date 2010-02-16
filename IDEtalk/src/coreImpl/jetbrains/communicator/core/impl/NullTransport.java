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
package jetbrains.communicator.core.impl;

import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.transport.XmlMessage;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.ProgressIndicator;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Kir
 */
public class NullTransport implements Transport {
  public void initializeProject(String projectName, MutablePicoContainer projectLevelContainer) {
  }

  public Class<? extends NamedUserCommand> getSpecificFinderClass() {
    return null;
  }

  public String getName() {
    return "NULL";
  }

  public boolean isSelf(User user) {
    return false;
  }

  public String getIconPath(UserPresence userPresence) {
    return null;
  }

  public String[] getProjects(User user) {
    //noinspection SSBasedInspection
    return new String[0];
  }

  public String getAddressString(User user) {
    return null;
  }

  public User[] findUsers(ProgressIndicator progressIndicator) {
    return new User[0];
  }

  public void sendXmlMessage(User user, XmlMessage message) {
  }

  public void setOwnPresence(UserPresence userPresence) {
  }

  public boolean isOnline() {
    return false;
  }

  public UserPresence getUserPresence(User user) {
    return new UserPresence(false);
  }

  public boolean hasIDEtalkClient(User user) {
    return false;
  }
}
