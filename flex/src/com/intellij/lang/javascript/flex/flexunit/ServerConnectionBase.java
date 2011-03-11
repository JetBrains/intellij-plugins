package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.execution.ExecutionException;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public abstract class ServerConnectionBase {

  public enum ConnectionStatus {
    NOT_CONNECTED, WAITING_FOR_CONNECTION, CONNECTED, DISCONNECTED, CONNECTION_FAILED
  }

  private static final Logger LOG = Logger.getInstance(ServerConnectionBase.class.getName());

  private final Object myLock = new Object();
  private ConnectionStatus myStatus = ConnectionStatus.NOT_CONNECTED;
  private boolean myStopped;
  private ServerSocket myServerSocket;
  private OutputStreamWriter myWriter;
  private static final int ACCEPT_TIMEOUT = 250; // ms

  public void open(int port) throws ExecutionException {
    try {
      myServerSocket = new ServerSocket(port);
      myServerSocket.setSoTimeout(ACCEPT_TIMEOUT);
    }
    catch (IOException e) {
      throw new ExecutionException(FlexBundle.message("port.is.busy", port), e);
    }
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        try {
          doRun();
          setStatus(ConnectionStatus.DISCONNECTED);
        }
        catch (IOException e) {
          LOG.warn(e);
          setStatus(ConnectionStatus.CONNECTION_FAILED);
        }
      }
    });
  }

  public static int getFreePort(int defaultPort, int attempts) {
    for (int i = 0; i < attempts; i++) {
      int port = defaultPort + i;
      if (tryPort(port)) return port;
    }
    return -1;
  }

  public static boolean tryPort(int port) {
    ServerSocket socket;
    try {
      socket = new ServerSocket(port);
      socket.close();
      return true;
    }
    catch (IOException e) {
      return false;
    }
  }


  protected void setStatus(ConnectionStatus status) {
    synchronized (myLock) {
      myStatus = status;
    }
  }

  public ConnectionStatus getStatus() {
    synchronized (myLock) {
      return myStatus;
    }
  }

  public void write(String text) {
    if (getStatus() != ConnectionStatus.CONNECTED || myWriter == null) {
      return;
    }

    try {
      myWriter.write(text);
      myWriter.flush();
    }
    catch (IOException e) {
      LOG.warn("Failed to write", e);
    }
  }

  protected abstract void run(InputStream inputStream) throws IOException;

  private void doRun() throws IOException {
    try {
      setStatus(ConnectionStatus.WAITING_FOR_CONNECTION);
      LOG.debug("listening port " + myServerSocket.getLocalPort() + ", timeout: " + myServerSocket.getSoTimeout() + " ms");

      Socket socket = null;
      while (!isStopped() && socket == null) {
        try {
          socket = myServerSocket.accept();
        }
        catch (SocketTimeoutException e) {
          //trace("timeout");
        }
      }

      if (socket == null) {
        return;
      }

      try {
        setStatus(ConnectionStatus.CONNECTED);
        LOG.debug("connected");

        myWriter = new OutputStreamWriter(socket.getOutputStream());
        run(socket.getInputStream());
      }
      finally {
        LOG.debug("closing client socket");
        socket.close();
        myWriter.close();
        myWriter = null;
      }
    }
    finally {
      myServerSocket.close();
    }
  }

  public void close() {
    LOG.debug("stopping");
    synchronized (myLock) {
      if (myStopped) return;
      myStopped = true;
    }
  }

  protected boolean isStopped() {
    synchronized (myLock) {
      return myStopped;
    }
  }

}
