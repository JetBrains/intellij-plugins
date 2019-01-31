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

import com.intellij.util.TimeoutUtil;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.users.PresenceMode;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.NullProgressIndicator;
import jetbrains.communicator.ide.ProgressIndicator;
import jetbrains.communicator.util.WaitFor;
import org.jmock.Mock;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author Kir Maximov
 */
public class UserMonitorThread_WaitingForNextSearch_Test extends BaseTestCase {
  private Mock myUserMonitorClientMock;
  private UserMonitorThread myUserMonitorThread;
  private MulticastPingThread myMulticastThread;
  private static final int PORT = 12234;
  private static final int WAIT_USER_RESPONSES_TIMEOUT = 100;
  private static final int SECS_BETWEEN_SCANS = 1;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUserMonitorClientMock = mock(UserMonitorClient.class);

    final boolean[] started = new boolean[1];
    myMulticastThread = new MulticastPingThread(InetAddress.getByName("localhost"), null, (UserMonitorClient) myUserMonitorClientMock.proxy()) {
      @Override
      public void run() {
        myStarted = true;
        TimeoutUtil.sleep(10000);
      }

      @Override
      public void sendMulticastPingRequest() {
        started[0] = true;
      }
    };

    myUserMonitorClientMock.stubs().method("getPort").will(returnValue(PORT));


    System.setProperty(UserMonitorThread.SCAN_TIMEOUT_PROPERTY, "" + SECS_BETWEEN_SCANS); // seconds

    myUserMonitorThread = new UserMonitorThread(new MulticastPingThread[]{myMulticastThread},
        (UserMonitorClient) myUserMonitorClientMock.proxy(), WAIT_USER_RESPONSES_TIMEOUT);
    myUserMonitorThread.start();

    System.setProperty(UserMonitorThread.SCAN_TIMEOUT_PROPERTY, ""); // seconds


    myUserMonitorThread.triggerFindNow();
    new WaitFor() { @Override
                    protected boolean condition() { return started[0]; } };
    myUserMonitorClientMock.expects(once()).method("setOnlineUsers").with(eq(Collections.emptySet()));

    new WaitFor(myUserMonitorThread.getWaitUserResponsesTimeout() + 100) {
      @Override
      protected boolean condition() {
        return !myUserMonitorThread.isFinding();
      }
    };
    assertFalse("Sanity check", myUserMonitorThread.isFinding());
  }

  @Override
  protected void tearDown() throws Exception {
    myUserMonitorThread.shutdown();
    new WaitFor(5000){
      @Override
      protected boolean condition() {
        return !myUserMonitorThread._isAlive();
      }
    };
    myUserMonitorThread.join();
    super.tearDown();
  }

  public void testForceFind() throws Exception {

    myUserMonitorThread.triggerFindNow();
    expectSetOneOnlineUser();
    myUserMonitorThread.findNow(createProgressIndicator());
    assertFalse("Should trigger user finding and then stop", myUserMonitorThread.isFinding());
  }

  public void testSecondCycleOfUserFinding() throws Exception {
    Thread.sleep(1010 * SECS_BETWEEN_SCANS);

    UserMonitorThread.LOG.debug("Expect another search");
    assertTrue("Should search for users again", myUserMonitorThread.isFinding());
    expectSetOneOnlineUser();
    Thread.sleep(myUserMonitorThread.getWaitUserResponsesTimeout());
    Thread.sleep(10);
    assertFalse("Search should be finished", myUserMonitorThread.isFinding());
  }

  private void expectSetOneOnlineUser() throws UnknownHostException {
    User p2PUser = UserImpl.create("nick", P2PTransport.CODE);

    List<String> projects = new ArrayList<>();
    projects.add("project1");

    OnlineUserInfo onlineUserInfo = new OnlineUserInfo(InetAddress.getByName("localhost"), PORT, projects, new UserPresence(PresenceMode.AWAY));

    myUserMonitorClientMock.expects(once()).method("createUser").with(
        eq("nick"),
        eq(onlineUserInfo)
    ).will(returnValue(p2PUser));

    myUserMonitorThread.addOnlineUser("localhost", "nick", new Integer(PORT), projects, new UserPresence(PresenceMode.AWAY));

    myUserMonitorClientMock.expects(once()).method("setOnlineUsers")
        .with(eq(new HashSet(Arrays.asList(p2PUser))));
  }


  private static ProgressIndicator createProgressIndicator() {
    return new NullProgressIndicator();
  }
}
