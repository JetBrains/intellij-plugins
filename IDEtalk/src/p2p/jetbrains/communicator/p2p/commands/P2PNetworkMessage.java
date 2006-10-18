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
package jetbrains.communicator.p2p.commands;

import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.p2p.NetworkUtil;
import jetbrains.communicator.p2p.P2PTransport;
import jetbrains.communicator.p2p.XmlRpcTargetImpl;
import jetbrains.communicator.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kir
 */
public class P2PNetworkMessage implements Message {
  private final String myCommandId;
  private final String myCommand;
  private final List<String> myCommandParameters = new ArrayList<String>();
  private Object myResponse;

  public P2PNetworkMessage(String commandId, String command, String[] commandParameters) {
    myCommandId = commandId;
    myCommand = command;

    for (String commandParameter : commandParameters) {
      myCommandParameters.add(commandParameter);
    }
  }

  public boolean send(User user) {
    int port = getPort(user);
    if (port < 0) {
      return false;
    }
    
    XmlRpcTargetImpl target = new XmlRpcTargetImpl(port, P2PTransport.getInstance().getAddress(user));
    List<String> parameters = new ArrayList<String>();
    parameters.add(StringUtil.toXMLSafeString(StringUtil.getMyUsername()));
    parameters.addAll(myCommandParameters);
    setResponse(NetworkUtil.sendMessage(target, myCommandId, myCommand,
        parameters.toArray(new Object[parameters.size()])));
    return null != myResponse;
  }

  void setResponse(Object response) {
    myResponse = response;
  }

  public Object getResponse() {
    return myResponse;
  }

  private int getPort(User user) {
    P2PTransport p2PTransport = P2PTransport.getInstance();
    if (p2PTransport == null) {
      return -1;
    }
    return p2PTransport.getPort(user);
  }

  public String[] getParameters() {
    return myCommandParameters.toArray(new String[myCommandParameters.size()]);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof P2PNetworkMessage)) return false;

    final P2PNetworkMessage p2PNetworkMessage = (P2PNetworkMessage) o;

    if (myCommand != null ? !myCommand.equals(p2PNetworkMessage.myCommand) : p2PNetworkMessage.myCommand != null) return false;
    if (myCommandId != null ? !myCommandId.equals(p2PNetworkMessage.myCommandId) : p2PNetworkMessage.myCommandId != null) return false;
    if (myCommandParameters != null ? !myCommandParameters.equals(p2PNetworkMessage.myCommandParameters) : p2PNetworkMessage.myCommandParameters != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (myCommandId != null ? myCommandId.hashCode() : 0);
    result = 29 * result + (myCommand != null ? myCommand.hashCode() : 0);
    result = 29 * result + (myCommandParameters != null ? myCommandParameters.hashCode() : 0);
    return result;
  }
}
