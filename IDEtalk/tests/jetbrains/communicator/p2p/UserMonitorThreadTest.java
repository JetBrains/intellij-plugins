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

import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.impl.users.UserImpl;
import jetbrains.communicator.core.users.PresenceMode;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.NullProgressIndicator;
import jetbrains.communicator.ide.ProgressIndicator;
import jetbrains.communicator.util.WaitFor;
import org.jmock.Mock;
import org.jmock.core.constraint.IsGreaterThan;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author Kir Maximov
 */
public class UserMonitorThreadTest extends BaseTestCase {
  private Mock myUserMonitorClientMock;
  private UserMonitorThread myUserMonitorThread;
  private MulticastPingThread myMulticastThread;
  private static final int PORT = 12234;
  private static final int WAIT_USER_RESPONSES_TIMEOUT = 500;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myUserMonitorClientMock = mock(UserMonitorClient.class);

    final boolean[] started = new boolean[1];
    myMulticastThread = new MulticastPingThread(InetAddress.getByName("localhost"), null, (UserMonitorClient) myUserMonitorClientMock.proxy()) {
      @Override
      public void run() {
        myStarted = true;
        try {
          sleep(100000);
        } catch (InterruptedException e) {
          assert false;
        }
      }

      @Override
      public void sendMulticastPingRequest() {
        started[0] = true;
      }
    };

    myUserMonitorClientMock.stubs().method("getPort").will(returnValue(PORT));
    myUserMonitorThread = new UserMonitorThread(new MulticastPingThread[]{myMulticastThread},
        (UserMonitorClient) myUserMonitorClientMock.proxy(), WAIT_USER_RESPONSES_TIMEOUT);
    myUserMonitorThread.start();

    triggerFind();
    new WaitFor() { @Override
                    protected boolean condition() { return started[0]; } };
  }

  @Override
  protected void tearDown() throws Exception {
    myUserMonitorThread.shutdown();
    new WaitFor(5000) {
      @Override
      protected boolean condition() {
        return !myUserMonitorThread._isAlive();
      }
    };
    myUserMonitorThread.join();
    super.tearDown();
  }

  private void triggerFind() {
    myUserMonitorThread.triggerFindNow();
  }

  public void testFlushOnlineUsers_NoUsers() {
    assert myUserMonitorThread.isFinding();
    myUserMonitorClientMock.expects(once()).method("setOnlineUsers").with(eq(Collections.emptySet()));
    myUserMonitorThread.flushOnlineUsers();
    assertTrue("Explicit flushing user list should not interrupt find process", myUserMonitorThread.isFinding());
  }


  public void testFlushOnlineUsers_WithUsers() throws Exception {
    expectSetOneOnlineUser();

    myUserMonitorThread.flushOnlineUsers();
  }

  public void testSetOnlineUsers() throws Exception {
    expectSetOneOnlineUser();

    assertTrue("Should be finding", myUserMonitorThread.isFinding());
    Thread.sleep(myUserMonitorThread.getWaitUserResponsesTimeout());

    new WaitFor(1000) {
      @Override
      protected boolean condition() {
        return !myUserMonitorThread.isFinding();
      }
    };
    assertFalse("Should be waiting wait for next cycle of user finding", myUserMonitorThread.isFinding());
  }

  public void testForceFind_WhenInFindState() throws Exception {
    expectSetOneOnlineUser();

    myUserMonitorThread.findNow(createProgressIndicator());  // should wait until the end of finding process;
    assertFalse("Should wait for next cycle of user finding", myUserMonitorThread.isFinding());
  }

  public void testProgressIndicatorInvocations() throws Exception {
    expectSetOneOnlineUser();

    Mock mockProgressIndicator = mock(ProgressIndicator.class);
    mockProgressIndicator.expects(atLeastOnce()).method("setText").with(eq("Found in local network: 1 user"));
    mockProgressIndicator.expects(atLeastOnce()).method("checkCanceled");
    
    final double[] lastFraction = new double[]{-1};
    mockProgressIndicator.expects(atLeastOnce()).method("setFraction").with(new IsGreaterThan(new Comparable() {
      @Override
      public int compareTo(Object o) {
        double v = ((Number) o).doubleValue();
        int result = (int) ((lastFraction[0] - v) * 100);
        lastFraction[0] = v;
        if (result >= 0) {
          System.out.println("result = " + result);
        }
        return result;
      }

      public String toString() {
        return "" + lastFraction[0];
      }
    }));

    myUserMonitorThread.findNow((ProgressIndicator) mockProgressIndicator.proxy());

    assertEquals("Should end at 100%", 1, lastFraction[0], 0.01);
  }


  public void testAddRemoteUser_LoopbackAddress() {
    Pico.setUnitTest(false);
    try {
      myUserMonitorClientMock.expects(once()).method("setOnlineUsers") .with(eq(new HashSet()));

      myUserMonitorThread.addOnlineUser("localhost", "nick", new Integer(PORT), new HashSet<>(), new UserPresence(PresenceMode.AWAY));
      myUserMonitorThread.flushOnlineUsers();
    }
    finally {
      Pico.setUnitTest(true);
    }
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
