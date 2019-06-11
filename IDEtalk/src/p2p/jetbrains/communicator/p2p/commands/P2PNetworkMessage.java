// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.p2p.commands;

import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.p2p.NetworkUtil;
import jetbrains.communicator.p2p.P2PTransport;
import jetbrains.communicator.p2p.XmlRpcTargetImpl;
import jetbrains.communicator.util.CommunicatorStrings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Kir
 */
public class P2PNetworkMessage implements Message {
  private final String myCommandId;
  private final String myCommand;
  private final List<String> myCommandParameters = new ArrayList<>();
  private Object myResponse;

  public P2PNetworkMessage(String commandId, String command, String[] commandParameters) {
    myCommandId = commandId;
    myCommand = command;

    Collections.addAll(myCommandParameters, commandParameters);
  }

  @Override
  public boolean send(User user) {
    int port = getPort(user);
    if (port < 0) {
      return false;
    }

    XmlRpcTargetImpl target = new XmlRpcTargetImpl(port, P2PTransport.getInstance().getAddress(user));
    List<String> parameters = new ArrayList<>();
    parameters.add(CommunicatorStrings.toXMLSafeString(CommunicatorStrings.getMyUsername()));
    parameters.addAll(myCommandParameters);
    setResponse(NetworkUtil.sendMessage(target, myCommandId, myCommand, ArrayUtil.toObjectArray(parameters)));
    return null != myResponse;
  }

  void setResponse(Object response) {
    myResponse = response;
  }

  public Object getResponse() {
    return myResponse;
  }

  private static int getPort(User user) {
    P2PTransport p2PTransport = P2PTransport.getInstance();
    if (p2PTransport == null) {
      return -1;
    }
    return p2PTransport.getPort(user);
  }

  public String[] getParameters() {
    return ArrayUtilRt.toStringArray(myCommandParameters);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof P2PNetworkMessage)) return false;

    final P2PNetworkMessage p2PNetworkMessage = (P2PNetworkMessage) o;

    if (myCommand != null ? !myCommand.equals(p2PNetworkMessage.myCommand) : p2PNetworkMessage.myCommand != null) return false;
    if (myCommandId != null ? !myCommandId.equals(p2PNetworkMessage.myCommandId) : p2PNetworkMessage.myCommandId != null) return false;
    if (!myCommandParameters.equals(p2PNetworkMessage.myCommandParameters)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (myCommandId != null ? myCommandId.hashCode() : 0);
    result = 29 * result + (myCommand != null ? myCommand.hashCode() : 0);
    result = 29 * result + (myCommandParameters.hashCode());
    return result;
  }
}
