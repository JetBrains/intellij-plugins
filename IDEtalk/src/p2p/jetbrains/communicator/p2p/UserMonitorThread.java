// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.p2p;

import com.intellij.util.Time;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.TalkProgressIndicator;
import jetbrains.communicator.util.CommunicatorStrings;
import jetbrains.communicator.util.WaitFor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author Kir Maximov
 *         <p/>
 *         This thread periodically scans the local network via multicast
 *         request and passes obtained users to UserMonitorClient
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public final class UserMonitorThread extends Thread {
  public static final Logger LOG = Logger.getLogger(UserMonitorThread.class);

  static final long WAIT_USER_RESPONSES_TIMEOUT = 3000;
  static final String SCAN_TIMEOUT_PROPERTY = "ideTalk.scanTimeout";
  static final long TIMEOUT_BETWEEN_SCANS = 3 * Time.MINUTE;

  private final MulticastPingThread[] myMulticastThreads;
  private final UserMonitorClient myClient;
  private final long myWaitUserResponsesTimeout;
  private final long myScansTimeout;

  private final Set<User> myAvailableUsers = Collections.synchronizedSet(new HashSet<>());

  private Thread myThread;
  private long myStartFindingAt;

  private final Object myLock = new Object();

  public UserMonitorThread(P2PTransport client, long waitUserResponsesTimeout) {
    this(createMulticastThreads(client), client, waitUserResponsesTimeout);
  }

  UserMonitorThread(MulticastPingThread[] multicastPingThread, UserMonitorClient client, long waitUserResponsesTimeout) {
    super("User Monitor Thread");

    setDaemon(true);
    assert multicastPingThread != null;
    myClient = client;
    myMulticastThreads = multicastPingThread;
    myWaitUserResponsesTimeout = waitUserResponsesTimeout;

    String timeout = System.getProperty(SCAN_TIMEOUT_PROPERTY);

    if (com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces(timeout)) {
      myScansTimeout = TIMEOUT_BETWEEN_SCANS;
    }
    else {
      try {
        myScansTimeout = Long.parseLong(timeout) * Time.SECOND;
      }
      catch (NumberFormatException e) {
        LOG.error("Invalid timeout for interval between scans: " + SCAN_TIMEOUT_PROPERTY + '=' + timeout);
        throw e;
      }
    }
  }

  private static MulticastPingThread[] createMulticastThreads(P2PTransport client) {
    List<MulticastPingThread> result = new ArrayList<>();
    for (InetAddress selfAddress : NetworkUtil.getSelfAddresses()) {
      result.add(new MulticastPingThread(selfAddress, client.getIdeFacade(), client));
    }
    return result.toArray(new MulticastPingThread[0]);
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

  @Override
  public void run() {
    super.run();
    LOG.info("Start " + getName());

    startupMulticastThreads();

    myThread = Thread.currentThread();

    while (isRunning()) {
      try {
        waitForNextSearch();

        if (!isRunning()) return;
        synchronized (myLock) {
          myStartFindingAt = System.currentTimeMillis();
          LOG.debug("Start finding users ");
        }

        try {
          sendMulticastRequests(getListeningThreads());

          //noinspection BusyWait
          Thread.sleep(myWaitUserResponsesTimeout);

          flushOnlineUsers();
        }
        finally {
          synchronized (myLock) {
            myStartFindingAt = 0;
            LOG.debug("Done finding users. Timeout for " + myScansTimeout);
          }
        }
      }
      catch (UnknownHostException e) {
        LOG.error(e.getMessage(), e);
      }
      catch (NoRouteToHostException e) {
        LOG.info(e.getMessage(), e);
      }
      catch (IOException e) {
        LOG.error(e.getMessage(), e);
      }
      catch (InterruptedException ignored) {
        myThread = null;
      }
      catch (Throwable e) {
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
    List<MulticastPingThread> result = new ArrayList<>();
    for (MulticastPingThread multicastThread : myMulticastThreads) {
      if (multicastThread.isAlive()) {
        result.add(multicastThread);
      }
    }
    return result;
  }

  private void startFindingUsers() {
    synchronized (myLock) {
      myStartFindingAt = System.currentTimeMillis();
      myAvailableUsers.clear();
    }
  }

  private void startupMulticastThreads() {
    for (MulticastPingThread multicastThread : myMulticastThreads) {
      multicastThread.start();
    }
    new WaitFor(Time.SECOND) {
      @Override
      protected boolean condition() {
        for (MulticastPingThread multicastThread : myMulticastThreads) {
          if (!multicastThread.isStarted()) return false;
        }
        return true;
      }
    };
  }

  private void waitForNextSearch() {
    try {
      synchronized (myLock) {
        while (isRunning() && !isFinding()) {
          myLock.wait(myScansTimeout);
          startFindingUsers();
        }
      }
    }
    catch (InterruptedException ignored) {
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
        myAvailableUsers.add(myClient.createUser(remoteUsername, onlineUserInfo));
      }
    }
    catch (UnknownHostException ignored) {
      LOG.info("Unable to find host for " + remoteAddress + ", user " + remoteUsername);
    }
  }

  boolean isFinding() {
    synchronized(myLock) {
      return isRunning() && myStartFindingAt > 0;
    }
  }

  void flushOnlineUsers() {
    Set<User> users;
    synchronized (myAvailableUsers) {
      users = new HashSet<>(myAvailableUsers);
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Setting online users: \n" + Arrays.toString(users.toArray()));
    }
    myClient.setOnlineUsers(users);
  }

  public long getWaitUserResponsesTimeout() {
    return myWaitUserResponsesTimeout;
  }

  public void findNow(TalkProgressIndicator progressIndicator) {
    if (!isRunning()) return;

    triggerFindNow();

    while (isFinding()) {
      progressIndicator.checkCanceled();
      setIndicatorText(progressIndicator);
      synchronized (myLock) {
        double fraction = (System.currentTimeMillis() - myStartFindingAt) / (50.0 + myWaitUserResponsesTimeout);
        progressIndicator.setFraction(fraction);
      }

      try {
        //noinspection BusyWait
        Thread.sleep(50);
      }
      catch (InterruptedException ignored) {
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

  private void setIndicatorText(TalkProgressIndicator progressIndicator) {
    int size = myAvailableUsers.size();
    progressIndicator.setText(
      CommunicatorStrings.getMsg("p2p.finder.progressText", String.valueOf(size), CommunicatorStrings.getText("user", size)));
  }

  boolean _isAlive() {
    for (MulticastPingThread multicastThread : myMulticastThreads) {
      if (multicastThread.isRunning()) return true;
    }
    return super.isAlive();
  }
}
