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
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.ProgressIndicator;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.WaitFor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author Kir Maximov
 *         <p/>
 *         This thread periodically scans the local network via multicast
 *         request and passes obtained users to UserMonitorClient
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class UserMonitorThread extends Thread {
  public static final Logger LOG = Logger.getLogger(UserMonitorThread.class);

  static final long WAIT_USER_RESPONSES_TIMEOUT = 3000;
  static final String SCAN_TIMEOUT_PROPERTY = "ideTalk.scanTimeout";
  public static final int MSECS_IN_SEC = 1000;
  static final long TIMEOUT_BETWEEN_SCANS = 3 * 60 * MSECS_IN_SEC;

  private final MulticastPingThread[] myMulticastThreads;
  private final UserMonitorClient myClient;
  private final long myWaitUserResponsesTimeout;
  private final long myScansTimeout;

  private final Object myAvailableUsersLock = new Object();
  private final Set<User> myAvailableUsers = new HashSet<User>();

  private Thread myThread;
  private long myStartFindingAt;

  private final Object myLock = new Object();

  public UserMonitorThread(P2PTransport client, long waitUserResponsesTimeout) {
    this(createMulticastThreads(client), client, waitUserResponsesTimeout);
  }

  UserMonitorThread(MulticastPingThread[] multicastPingThread, UserMonitorClient client, long waitUserResponsesTimeout) {
    super("User Monitor Thread");
    assert multicastPingThread != null;
    myClient = client;
    myMulticastThreads = multicastPingThread;
    myWaitUserResponsesTimeout = waitUserResponsesTimeout;

    String timeout = System.getProperty(SCAN_TIMEOUT_PROPERTY);

    if(StringUtil.isEmpty(timeout)) {
      myScansTimeout = TIMEOUT_BETWEEN_SCANS;
    }
    else {
      try {
        myScansTimeout = Long.parseLong(timeout) * MSECS_IN_SEC;
      } catch (NumberFormatException e) {
        LOG.error("Invalid timeout for interval between scans: " + SCAN_TIMEOUT_PROPERTY + '=' + timeout);
        throw e;
      }
    }
  }

  private static MulticastPingThread[] createMulticastThreads(P2PTransport client) {
    List<MulticastPingThread> result = new ArrayList<MulticastPingThread>();
    try {
      for (InetAddress selfAddress : NetworkUtil.getSelfAddresses()) {
        result.add(new MulticastPingThread(selfAddress, client.getIdeFacade(), client));
      }
    } catch (SocketException e) {
      LOG.info(e.getMessage(), e);
    }
    return result.toArray(new MulticastPingThread[result.size()]);
  }

  public void shutdown() {
    shutdownMulticastThreads();
    if (isRunning()) {
      final Thread thr = myThread;
      myThread = null;
      synchronized(myLock) {
        myLock.notifyAll();
      }
      if (thr.isAlive()) {
        thr.interrupt();
      }
    }
  }

  private void shutdownMulticastThreads() {
    for (MulticastPingThread multicastThread : myMulticastThreads) {
      if (multicastThread.isAlive()) {
        multicastThread.shutdown();
      }
    }
  }

  public void run() {
    super.run();
    LOG.info("Start " + getName());

    startupMulticastThreads();

    myThread = Thread.currentThread();

    while (isRunning()) {
      try {
        waitForNextSearch();

        if (!isRunning()) return;
        synchronized(myLock) {
          myStartFindingAt = System.currentTimeMillis();
          LOG.debug("Start finding users ");
        }

        try {
          sendMulticastRequests(getListeningThreads());

          Thread.sleep(myWaitUserResponsesTimeout);

          flushOnlineUsers();
        } finally {
          synchronized(myLock) {
            myStartFindingAt = 0;
            LOG.debug("Done finding users. Timeout for " + myScansTimeout);
          }
        }
      } catch (UnknownHostException e) {
        LOG.error(e.getMessage(), e);
      } catch (NoRouteToHostException e) {
        LOG.info(e.getMessage(), e);
      } catch (IOException e) {
        LOG.error(e.getMessage(), e);
      } catch (InterruptedException e) {
        myThread = null;
      } catch (Throwable e) {
        LOG.error(e.getMessage(), e);
        myThread = null;
      }
    }
    LOG.info("Shut down");
  }

  boolean isRunning() {
    return myThread != null;
  }

  private static void sendMulticastRequests(List<MulticastPingThread> listeningThreads) throws IOException {
    for (MulticastPingThread thread : listeningThreads) {
      thread.sendMulticastPingRequest();
    }
  }

  private List<MulticastPingThread> getListeningThreads() {
    List<MulticastPingThread> result = new ArrayList<MulticastPingThread>();
    for (MulticastPingThread multicastThread : myMulticastThreads) {
      if (multicastThread.isAlive()) {
        result.add(multicastThread);
      }
    }
    return result;
  }

  private void startFindingUsers() {
    synchronized(myLock) {
      myStartFindingAt = System.currentTimeMillis();
      synchronized(myAvailableUsersLock) {
        myAvailableUsers.clear();
      }
    }
  }

  private void startupMulticastThreads() {
    for (MulticastPingThread multicastThread : myMulticastThreads) {
      multicastThread.start();
    }
    new WaitFor(1 * MSECS_IN_SEC) { protected boolean condition() {
      for (MulticastPingThread multicastThread : myMulticastThreads) {
        if (!multicastThread.isStarted()) return false;
      }
      return true;
    } };
  }

  private void waitForNextSearch() {
    try {
      synchronized(myLock) {
        while(isRunning() && !isFinding()) {
          myLock.wait(myScansTimeout);
          startFindingUsers();
        }
      }
    } catch (InterruptedException e) {
      myThread = null;
    }
  }

  public void addOnlineUser(String remoteAddress, String remoteUsername, Integer remotePort, Collection<String> projects, UserPresence presence) {
    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Got Online Response from " + remoteUsername + " at " + remoteAddress + '/' + remotePort);
      }
      OnlineUserInfo onlineUserInfo = new OnlineUserInfo(InetAddress.getByName(remoteAddress), remotePort.intValue(), projects, presence);
      if (!onlineUserInfo.getAddress().isLoopbackAddress() || Pico.isUnitTest()) {
        User user = myClient.createUser(remoteUsername, onlineUserInfo);
        synchronized(myAvailableUsersLock) {
          myAvailableUsers.add(user);
        }
      }
    } catch (UnknownHostException e) {
      LOG.info("Unable to find host for " + remoteAddress + ", user " + remoteUsername);
    }
  }

  boolean isFinding() {
    synchronized(myLock) {
      return isRunning() && myStartFindingAt > 0;
    }
  }

  void flushOnlineUsers() {
    synchronized(myAvailableUsersLock) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Setting online users: \n" + myAvailableUsers.toString().replace(',','\n'));
      }
      myClient.setOnlineUsers(new HashSet<User>(myAvailableUsers));
    }
  }

  public long getWaitUserResponsesTimeout() {
    return myWaitUserResponsesTimeout;
  }

  public void findNow(ProgressIndicator progressIndicator) {
    if (!isRunning()) return;

    triggerFindNow();

    while (isFinding()) {
      progressIndicator.checkCanceled();
      setIndicatorText(progressIndicator);
      synchronized(myLock) {
        double fraction = (System.currentTimeMillis() - myStartFindingAt) / (50.0 + myWaitUserResponsesTimeout);
        progressIndicator.setFraction(fraction);
      }

      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        break;
      }
    }
    progressIndicator.setFraction(1.0);
  }

  void triggerFindNow() {
    if (!isFinding()) {
      LOG.info("Force finding users");
      synchronized(myLock) {
        startFindingUsers();
        myLock.notifyAll();
      }
    }
  }

  private void setIndicatorText(ProgressIndicator progressIndicator) {
    int size;
    synchronized(myAvailableUsersLock) {
      size = myAvailableUsers.size();
    }
    progressIndicator.setText(StringUtil.getMsg("p2p.finder.progressText",
        String.valueOf(size),
        StringUtil.getText("user", size)));
  }

  boolean _isAlive() {
    for (MulticastPingThread multicastThread : myMulticastThreads) {
      if (multicastThread.isRunning()) return true;
    }
    return super.isAlive();
  }
}
