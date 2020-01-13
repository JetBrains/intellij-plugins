// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.core.impl;

import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.transport.XmlMessage;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.TalkProgressIndicator;
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
  public User[] findUsers(TalkProgressIndicator progressIndicator) {
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
