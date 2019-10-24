// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.jabber;

import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.MutablePicoContainer;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Kir
 */
public interface JabberUI {

  boolean connectAndLogin(String message);
  void connectAndLoginAsync(String message, AtomicBoolean connected);

  void initPerProject(MutablePicoContainer projectLevelContainer);
  void login(Component parentComponent);

  /** @return information about users to be added in form:
  * [groupName]:[space or comma-separated JabberIDs]
  * <br> Can return null if no data entered */
  String getFindByIdData(List<String> availableGroups);

  boolean shouldAcceptSubscriptionRequest(Presence requestFrom);

}
