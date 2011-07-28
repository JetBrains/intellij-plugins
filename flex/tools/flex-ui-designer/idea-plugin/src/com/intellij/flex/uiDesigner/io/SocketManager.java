package com.intellij.flex.uiDesigner.io;

import com.intellij.flex.uiDesigner.FlexUIDesignerApplicationManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

abstract class SocketManager implements Runnable, Disposable {
  protected static final Logger LOG = Logger.getInstance(SocketManager.class.getName());

  protected ServerSocket serverSocket;
  protected Socket socket;

  public int listen() throws IOException {
    FlexUIDesignerApplicationManager.getInstance().disposeOnApplicationClosed(this);

    serverSocket = new ServerSocket(0, 1);
    ApplicationManager.getApplication().executeOnPooledThread(this);
    int port = serverSocket.getLocalPort();
    assert port != -1;
    return port;
  }

  @Override
  public void run() {
    try {
      socket = serverSocket.accept();
      serverSocket.close();
      setOut(socket.getOutputStream());
      serverSocket = null;
    }
    catch (IOException e) {
      // if null, so, already disposed (exception thrown by accept)
      if (serverSocket != null) {
        LOG.error(e);
        IOUtil.close(socket);
        socket = null;
        clientSocketNotAccepted();
      }
      return;
    }

    clientSocketAccepted();
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
