/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package jetbrains.communicator.p2p.commands;

import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.p2p.NetworkUtil;
import jetbrains.communicator.p2p.P2PCommand;
import jetbrains.communicator.p2p.UserMonitorThread;
import jetbrains.communicator.p2p.XmlRpcTargetImpl;
import jetbrains.communicator.util.CommunicatorStrings;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Vector;

/**
 * notification about existence of a user in network
 */
public class AddOnlineUserP2PCommand implements P2PCommand {
  private static final String ID = "AddOnlineUser";

  private final UserMonitorThread myUserMonitorThread;

  public AddOnlineUserP2PCommand(@NotNull UserMonitorThread userMonitorThread) {
    myUserMonitorThread = userMonitorThread;
  }

  @Override
  public String getXmlRpcId() {
    return ID;
  }

  public boolean addOnlineUser(String remoteAddress, String remoteUsername, int remotePort, Vector<String> projects, Vector userPresence) {
    myUserMonitorThread.addOnlineUser(remoteAddress, CommunicatorStrings.fromXMLSafeString(remoteUsername),
                                      remotePort, projects, UserPresence.fromVector(userPresence));
    return true;
  }

  public static void addSelfTo(int port, InetAddress remoteAddress, InetAddress selfAddress, int myPort, Collection<String> projects, UserPresence presence) {
    XmlRpcTargetImpl target = new XmlRpcTargetImpl(port, remoteAddress);
    NetworkUtil.sendMessage(target, ID, "addOnlineUser",
                            selfAddress.getHostAddress(),
                            CommunicatorStrings.toXMLSafeString(CommunicatorStrings.getMyUsername()),
                            myPort,
                            new Vector<>(projects),
                            presence.toVector()
    );
  }
}
