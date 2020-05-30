// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.transport;

import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.TalkProgressIndicator;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.picocontainer.MutablePicoContainer;

import javax.swing.*;

/**
 * @author Kir Maximov
 *
 * Register implementation of this interface upon application start:
 *
 * Pico.getInstance().registerComponentInstance(myTransport);
 */
public interface Transport {

  String NAMESPACE = "http://idetalk.com/namespace";

  /** Method is called once upon project opening.
   * @param projectName
   * @param projectLevelContainer   */
  void initializeProject(String projectName, MutablePicoContainer projectLevelContainer);

  String getName();

  /** This call should search all users this transport can find. */
  User[] findUsers(TalkProgressIndicator progressIndicator);

  /** Can return null if no specific user finder exist in the current transport.
   * Specific finder is used as an option in FindUsers command. For Jabber, it can be
   * "Find by JabberID". Class should be a NamedUserCommand class.*/
  Class<? extends NamedUserCommand> getSpecificFinderClass();

  UserPresence getUserPresence(User user);
  /** @return true if user is self for given transport */
  boolean isSelf(User user);

  @NonNls
  Icon getIcon(UserPresence userPresence);

  /** Get lists of project opened by user. Simple implementation can use
   * User:getProjectsData call */
  String[] getProjects(User user);
  /** Should return user's address in human-readable form or null if not available */
  @Nullable
  String getAddressString(User user);

  void sendXmlMessage(User user, XmlMessage message);

  /** Transport is active and can be used for communication */
  boolean isOnline();

  /** Set own presence for the transport, like away, not available etc.*/
  void setOwnPresence(UserPresence userPresence);

  /** return true if user has IDEtalk client installed */
  boolean hasIdeTalkClient(User user);
}
