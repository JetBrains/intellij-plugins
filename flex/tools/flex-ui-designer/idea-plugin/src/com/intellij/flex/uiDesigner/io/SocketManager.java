package com.intellij.flex.uiDesigner.io;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("SocketOpenedButNotSafelyClosed")
abstract class SocketManager implements Runnable, Disposable {
  protected static final Logger LOG = Logger.getInstance(SocketManager.class.getName());

  protected ServerSocket serverSocket;
  protected Socket socket;

  public int listen() throws IOException {
    serverSocket = new ServerSocket(0, 1);
    int port = serverSocket.getLocalPort();
    assert port != -1;
    ApplicationManager.getApplication().executeOnPooledThread(this);
    return port;
  }

  @Override
  public void run() {
    try {
      socket = serverSocket.accept();
      serverSocket.close();
      setOut(socket.getOutputStream());
      serverSocket = null;
      clientSocketAccepted();
    }
    catch (IOException e) {
      // if null, so, already disposed (exception thrown by accept)
      if (serverSocket != null) {
        LOG.error(e);
        IOUtil.close(socket);
        socket = null;
        clientSocketNotAccepted();
      }
    }
  }

  protected void clientSocketAccepted() {
  }

  protected void clientSocketNotAccepted() {
  }

  protected abstract void setOut(OutputStream out);

  @Override
  public void dispose() {
    if (serverSocket == null) {
      IOUtil.close(socket);
      socket = null;
    }
    else {
      try {
        serverSocket.close();
      }
      catch (IOException e) {
        LOG.error(e);
      }
      serverSocket = null;
    }
  }
}