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
package jetbrains.communicator.idea.monitor;

import jetbrains.communicator.core.IDEtalkOptions;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.impl.BaseTestCase;
import jetbrains.communicator.core.users.PresenceMode;
import jetbrains.communicator.mock.MockTransport;
import jetbrains.communicator.util.WaitFor;

/**
 * @author Kir
 */
public class UserActivityMonitorTest extends BaseTestCase {
  private Thread myThread;
  private UserActivityMonitor myMonitor;
  private MockTransport myTransport;


  public UserActivityMonitorTest(String string) {
    super(string);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myTransport = new MockTransport();
    Pico.getInstance().registerComponentInstance(myTransport);

    myMonitor = new UserActivityMonitor();
    myMonitor.setRefreshInterval(100);
    myThread = new Thread(myMonitor, "UserActivityMonitorTest-" + getName());
  }

  public void testOfflineTransport() {
    myTransport.setOnline(false);
    startThread();

    assertNull(myTransport.getPresence());
  }

  public void testNotAvailable() {
    startThread();

    assertNotNull(myTransport.getPresence());
    assertEquals(PresenceMode.EXTENDED_AWAY, myTransport.getPresence().getPresenceMode());
  }

  public void testAvailable() {
    myMonitor.activity();
    startThread();

    assertNotNull(myTransport.getPresence());
    assertEquals(PresenceMode.AVAILABLE, myTransport.getPresence().getPresenceMode());
  }

  public void testAway() throws Exception {
    myOptions.setNumber(IDEtalkOptions.TIMEOUT_AWAY_MIN, 1.0/60/20);
    myOptions.setNumber(IDEtalkOptions.TIMEOUT_XA_MIN, 1.0/60/5);

    myMonitor.activity();
    Thread.sleep(100);
    startThread();

    assertNotNull(myTransport.getPresence());
    assertEquals(PresenceMode.AWAY, myTransport.getPresence().getPresenceMode());
  }

  public void testExtendedAway() throws Exception {
    myMonitor.activity();
    myOptions.setNumber(IDEtalkOptions.TIMEOUT_AWAY_MIN, 1.0/60/20);
    myOptions.setNumber(IDEtalkOptions.TIMEOUT_XA_MIN, 1.0/60/5);
    Thread.sleep(250);
    startThread();

    assertNotNull(myTransport.getPresence());
    assertEquals(PresenceMode.EXTENDED_AWAY, myTransport.getPresence().getPresenceMode());
  }

  private void startThread() {
    myThread.start();
    new WaitFor(500) {
      @Override
      protected boolean condition() {
        return myTransport.getPresence() != null;
      }
    };
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      myMonitor.disposeComponent();
      myThread.join();
    }
    finally {
      super.tearDown();
    }
  }
}
