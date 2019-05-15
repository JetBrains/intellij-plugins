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
package jetbrains.communicator.p2p;

import jetbrains.communicator.AbstractTransportTestCase;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.util.CommunicatorStrings;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Kir Maximov
 *
 */
public class P2PTransport_SelfOnline_Test extends AbstractTransportTestCase {
  private P2PTransport myTransport;
  private User mySelf;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    mySelf = createSelf();
    myTransport.setOnlineUsers(Collections.singleton(mySelf));
  }

  @Override
  protected Transport createTransport() {
    myTransport = new P2PTransport(myDispatcher, myUserModel);
    Pico.getInstance().registerComponentInstance(myTransport);
    return myTransport;
  }

  @Override
  protected User createSelf() throws UnknownHostException {
    return myTransport.createUser(CommunicatorStrings.getMyUsername(), new OnlineUserInfo(InetAddress.getLocalHost(), myTransport.getPort()));
  }

  @Override
  protected User createAnotherOnlineUser() throws UnknownHostException {
    mySelf = createSelf();
    User user = myTransport.createUser("kir123@localhost", new OnlineUserInfo(InetAddress.getLocalHost(), myTransport.getPort()));
    myTransport.setOnlineUsers(Arrays.asList(mySelf, user));
    return user;
  }


  @Override
  protected void tearDown() throws Exception {
    if (myTransport != null) {
      myTransport.dispose();
      Pico.getInstance().unregisterComponentByInstance(myTransport);
    }

    super.tearDown();
  }
}
