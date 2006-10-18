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
package jetbrains.communicator.jabber;

import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.MutablePicoContainer;

import java.awt.*;
import java.util.List;

/**
 * @author Kir
 */
public interface JabberUI {

  boolean connectAndLogin(String message);

  void initPerProject(MutablePicoContainer projectLevelContainer);
  void login(Component parentComponent);

  /** @return information about users to be added in form:
  * [groupName]:[space or comma-separated JabberIDs]
  * <br> Can return null if no data entered */
  String getFindByIdData(List<String> availableGroups);

  boolean shouldAcceptSubscriptionRequest(Presence requestFrom);

}
