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

import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.p2p.P2PCommand;

/**
 * @author Kir
 */
public abstract class BaseP2PCommand implements P2PCommand {
  private final String myId;
  protected final EventBroadcaster myEventBroadcaster;
  protected final Transport myTransport;

  public BaseP2PCommand(String id, EventBroadcaster eventBroadcaster, Transport transport) {
    myId = id;
    myEventBroadcaster = eventBroadcaster;
    myTransport = transport;
  }

  @Override
  public final String getXmlRpcId() {
    return myId;
  }
}