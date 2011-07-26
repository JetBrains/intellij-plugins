package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.Closable;
import com.intellij.flex.uiDesigner.io.IOUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

class Server implements Runnable, Closable {
  private static final Logger LOG = Logger.getInstance(Server.class.getName());

  private ServerSocket serverSocket;
  private Socket socket;

  private FlexUIDesignerApplicationManager.FirstOpenDocumentTask pendingTask;

  public Server(@NotNull final FlexUIDesignerApplicationManager.FirstOpenDocumentTask pendingTask) {
    this.pendingTask = pendingTask;
  }

  public int listen() throws IOException {
    serverSocket = new ServerSocket(0, 1);

    ApplicationManager.getApplication().executeOnPooledThread(this);
    int port = serverSocket.getLocalPort();
    assert port != -1;
    return port;
  }

  public int errorListen() throws IOException {
    final ServerSocket errorServerSocket = new ServerSocket(0, 1);

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        try {
          Socket errorSocket = errorServerSocket.accept();
          errorServerSocket.close();
          ServiceManager.getService(SocketInputHandler.class).setErrorOut(errorSocket.getOutputStream());
        }
        catch (IOException e) {
          LOG.error(e);
        }
      }
    });

    int port = errorServerSocket.getLocalPort();
    assert port != -1;
    return port;
  }

  public void run() {
    try {
      socket = serverSocket.accept();
      serverSocket.close();
      serverSocket = null;
      Client.getInstance().setOut(socket.getOutputStream());
    }
    catch (IOException e) {
      // if null, so, already closed via close()
      if (pendingTask != null) {
        LOG.error(e);
        IOUtil.close(socket);
        pendingTask.clientSocketNotAccepted();
        pendingTask = null;
      }
      return;
    }

    pendingTask.clientOpened();
    pendingTask = null;

    try {
      ServiceManager.getService(SocketInputHandler.class).read(socket.getInputStream(), FlexUIDesignerApplicationManager.APP_DIR);
    }
    catch (IOException e) {
      if (!(e instanceof SocketException && socket.isClosed())) {
        LOG.error(e);
      }
    }
    finally {
      IOUtil.close(this);
    }
  }

  public void close() throws IOException {
    if (serverSocket == null) {
      IOUtil.close(ServiceManager.getService(SocketInputHandler.class));
      IOUtil.close(socket);
    }
    else {
      pendingTask = null;
      IOUtil.close(serverSocket);
    }
  }

  public boolean isClosed() {
    return socket == null || socket.isClosed();
  }
}