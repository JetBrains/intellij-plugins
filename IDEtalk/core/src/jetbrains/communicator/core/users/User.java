// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.core.users;

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.transport.XmlMessage;
import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;

import javax.swing.*;

/**
 * @author Kir Maximov
 */
public interface User {

  String getName();
  String getDisplayName();
  Icon getIcon();
  String getGroup();

  /** Sets this user's displayName. If userModel is not null and user exists in userModel,
   * displayName is set also for the user in the userModel and
   * event about group change is sent.
   * @see UserEvent.Updated */
  void setDisplayName(String name, UserModel userModel);
  /** Sets this user's group. If userModel is not null and user exists in userModel,
   * group is set also for the user in the userModel and
   * event about group change is sent.
   * @see UserEvent.Updated */
  void setGroup(String group, UserModel userModel);

  /** If returns true, this user can view my opened files (method getProjectsData)
   * and get content of my files (method getVFile). Default value - true. */
  boolean canAccessMyFiles();
  void setCanAccessMyFiles(boolean enableAccess, UserModel userModel);

  String getTransportCode();
  UserPresence getPresence();
  boolean isOnline();
  boolean isSelf();

  String[] getProjects();
  ProjectsData getProjectsData(IDEFacade ideFacade);
  String getVFile(VFile vFile, IDEFacade ideFacade);

  void sendMessage(String message, EventBroadcaster eventBroadcaster);
  void sendCodeIntervalPointer(VFile file, CodePointer pointer, String comment, EventBroadcaster eventBroadcaster);
  void sendXmlMessage(XmlMessage message);

  /** return true if user has IDEtalk client installed*/
  boolean hasIDEtalkClient();
}
