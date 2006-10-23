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

package jetbrains.communicator.core.transport;

import jetbrains.communicator.core.commands.NamedUserCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.ProgressIndicator;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.picocontainer.MutablePicoContainer;

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
  User[] findUsers(ProgressIndicator progressIndicator);

  /** Can return null if no specific user finder exist in the current transport.
   * Specific finder is used as an option in FindUsers command. For Jabber, it can be
   * "Find by JabberID". Class should be a NamedUserCommand class.*/
  Class<? extends NamedUserCommand> getSpecificFinderClass();

  UserPresence getUserPresence(User user);
  /** @return true if user is self for given transport */
  boolean isSelf(User user);

  @NonNls
  /** @return path to the user icon corresponding to the user presence. The icon should be available as a jar resource */
  String getIconPath(UserPresence userPresence);

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
  boolean hasIDEtalkClient(User user);
}
