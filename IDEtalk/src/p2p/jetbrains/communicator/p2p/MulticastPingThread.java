// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.p2p;

import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.p2p.commands.AddOnlineUserP2PCommand;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Kir Maximov
 */
@SuppressWarnings({"HardCodedStringLiteral"})
class MulticastPingThread extends Thread {

  private static final Logger LOG = Logger.getLogger(MulticastPingThread.class);

  public static final int MULTICAST_PORT = 2863;
  static final String PING_MESSAGE = "Is there anybody out there?";
  private static final String MULTICAST_ADORES = "239.203.13.64";
  private static final int BUFFER_SIZE = PING_MESSAGE.getBytes(StandardCharsets.UTF_8).length + 6;
  private static final int ALLOWED_FAILURES = 3000;

  private volatile boolean myIsRunning;
  private MulticastSocket myDatagramSocket;
  protected boolean myStarted;

  private final InetAddress mySelfAddress;
  private final IDEFacade myIdeFacade;
  private final UserMonitorClient myUserMonitorClient;

  private int myFailuresCounter;

  MulticastPingThread(InetAddress address, IDEFacade ideFacade, UserMonitorClient userMonitorClient) {
    super(address.toString() + " IDEtalk Multicast Thread");
    setDaemon(true); //Sometimes this thread lives forever, that's why idea.exe process never ends
                     // see jetbrains.communicator.p2p.NetworkUtil.sendMessage(...) inside try/catch block
                     //TODO: make it not a Daemon thread again and fix the problem somewhere else. Where...?
    mySelfAddress = address;
    myIdeFacade = ideFacade;
    myUserMonitorClient = userMonitorClient;

    System.setProperty("sun.net.client.defaultConnectTimeout", "2000");
  }

  public void sendMulticastPingRequest() throws IOException {
    if (!myIsRunning) return;

    MulticastSocket datagramSocket = null;
    try {
      datagramSocket = new MulticastSocket();
      datagramSocket.setInterface(mySelfAddress);
      LOG.debug("Sending Multicast ping request: " + mySelfAddress);
      sendMessage(datagramSocket, PING_MESSAGE + myUserMonitorClient.getPort());
      myFailuresCounter = 0;
    } catch (IOException e) {

      if (++myFailuresCounter > ALLOWED_FAILURES) {
        LOG.info("Unable to send multicast request on interface " + mySelfAddress + ". I give up after " +
            myFailuresCounter + " attempts.", e);
        myIsRunning = false;
      }
    }
    finally {
      if (datagramSocket != null) {
        datagramSocket.close();
      }
    }
  }

  private static void sendMessage(MulticastSocket datagramSocket, String msg) throws IOException {
    DatagramPacket packet = new DatagramPacket(msg.getBytes(StandardCharsets.UTF_8), 0, msg.getBytes(StandardCharsets.UTF_8).length,
                                               InetAddress.getByName(MULTICAST_ADORES), MULTICAST_PORT);
    datagramSocket.send(packet);
  }

  @Override
  public void run() {
    LOG.info(getName() + ": Start thread.");
    Runtime.getRuntime().addShutdownHook(new Thread("IDETalk shutdown hook") {
      @Override
      public void run() {
        MulticastPingThread.this.interrupt(); // Yes, MulticastPingThread is daemon, but it still keeps JVM running.
                                              // Seems this is some troubles with native calls.
                                              // see IDEA-52501
      }
    });
    myDatagramSocket = null;
    myIsRunning = true;

    try {
      myDatagramSocket = new MulticastSocket(MULTICAST_PORT);
      myDatagramSocket.setInterface(mySelfAddress);
      myDatagramSocket.joinGroup(InetAddress.getByName(MULTICAST_ADORES));

      byte[] buffer = new byte[BUFFER_SIZE];
      while (myIsRunning) {
        try {
          DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
          LOG.debug(getName() + ": Listening for multicast messages... ");
          myStarted = true;
          myDatagramSocket.receive(datagramPacket);
          String message = new String(buffer, 0, datagramPacket.getLength(), StandardCharsets.UTF_8);
          final InetAddress remoteAddress = datagramPacket.getAddress();

          if (LOG.isDebugEnabled()) {
            LOG.debug(getName() + ": Got multicast message '" + message + "' from " + remoteAddress);
          }
          if (message.startsWith(PING_MESSAGE)) {
            final int targetPort = extractPort(message);

            if (shouldAddSelf(datagramPacket, targetPort)) {
              addSelfInfoTo(remoteAddress, targetPort);
            }
          }
        }
        catch(SocketException e) {
          if (!"Socket closed".equalsIgnoreCase(e.getMessage())) {
            LOG.error(e.getMessage(), e);
          }
          else {
            myIsRunning = false;
          }
        }
      }
    } catch (SocketException e) {
      final String msg = e.getMessage();
      if (msg != null) {
        LOG.info(msg, e);
      }
      else {
        logError(e);
      }
    } catch (IOException e) {
      logError(e);
    }
    finally {
      myIsRunning = false;
      if (myDatagramSocket != null && !myDatagramSocket.isClosed()){
        myDatagramSocket.close();
      }
    }
  }

  private void logError(IOException e) {
    LOG.error(getName() + " is terminated:\n" + e.getMessage(), e);
  }

  private void addSelfInfoTo(InetAddress remoteAddress, int targetPort) {
    String[] projects = myIdeFacade.getProjects();

    if (LOG.isDebugEnabled()) {
      LOG.debug(getName() + ": Add self to " + remoteAddress);
    }
    AddOnlineUserP2PCommand.addSelfTo(targetPort, remoteAddress, mySelfAddress, myUserMonitorClient.getPort(), Arrays.asList(projects), myUserMonitorClient.getOwnPresence());
  }

  private boolean shouldAddSelf(DatagramPacket datagramPacket, int targetPort) {
    boolean ownAddress = NetworkUtil.isOwnAddress(datagramPacket.getAddress());
    if (ownAddress) {
      return shouldSendToSelf(datagramPacket, targetPort); // For tests
    }
    return !mySelfAddress.isLoopbackAddress();
  }

  private boolean shouldSendToSelf(DatagramPacket datagramPacket, int targetPort) {
    return datagramPacket.getAddress().equals(mySelfAddress) && targetPort == myUserMonitorClient.getPort();
  }

  static int extractPort(String message) {
    String port = message.substring(PING_MESSAGE.length());
    try {
      return Integer.valueOf(port).intValue();
    }
    catch (Exception e) {
      LOG.debug(e);
    }
    return P2PTransport.XML_RPC_PORT;
  }

  public void shutdown() {
    if (myDatagramSocket != null) {
      myDatagramSocket.close();
    }
    if (myIsRunning) {
      myIsRunning = false;
      //try {
      //  while (!myDisposed) {
      //    Thread.sleep(100);
      //  }
      //}
      //catch (InterruptedException ignored) {
      //}
    }
  }

  public boolean isStarted() {
    return myStarted;
  }

  public boolean isRunning() {
    return myIsRunning;
  }
}
