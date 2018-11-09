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

import javax.swing.*;

/**
 * @author Kir
 */
public class NullTransport implements Transport {
  @Override
  public void initializeProject(String projectName, MutablePicoContainer projectLevelContainer) {
  }

  @Override
  public Class<? extends NamedUserCommand> getSpecificFinderClass() {
    return null;
  }

  @Override
  public String getName() {
    return "NULL";
  }

  @Override
  public boolean isSelf(User user) {
    return false;
  }

  @Override
  public Icon getIcon(UserPresence userPresence) {
    return null;
  }

  @Override
  public String[] getProjects(User user) {
    //noinspection SSBasedInspection
    return new String[0];
  }

  @Override
  public String getAddressString(User user) {
    return null;
  }

  @Override
  public User[] findUsers(ProgressIndicator progressIndicator) {
    return new User[0];
  }

  @Override
  public void sendXmlMessage(User user, XmlMessage message) {
  }

  @Override
  public void setOwnPresence(UserPresence userPresence) {
  }

  @Override
  public boolean isOnline() {
    return false;
  }

  @Override
  public UserPresence getUserPresence(User user) {
    return new UserPresence(false);
  }

  @Override
  public boolean hasIdeTalkClient(User user) {
    return false;
  }
}
