package com.intellij.flex.uiDesigner;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

class Server implements Runnable, Closable {
  private static final Logger LOG = Logger.getInstance(Server.class.getName());

  private ServerSocket serverSocket;
  private FlexUIDesignerApplicationManager.PendingOpenDocumentTask myPendingTask;

  private Socket socket;

  private FlexUIDesignerApplicationManager applicationManager;

  public Server(@NotNull final FlexUIDesignerApplicationManager.PendingOpenDocumentTask pendingTask,
                FlexUIDesignerApplicationManager applicationManager) {
    myPendingTask = pendingTask;
    this.applicationManager = applicationManager;
  }

  public int listen() throws IOException {
    serverSocket = new ServerSocket(0, 1);

    ApplicationManager.getApplication().executeOnPooledThread(this);
    int port = serverSocket.getLocalPort();
    assert port != -1;
    return port;
  }

  public void run() {
    final OutputStream socketOutputStream;
    try {
      socket = serverSocket.accept();
      serverSocket.close();
      socketOutputStream = socket.getOutputStream();
    }
    catch (IOException e) {
      LOG.error(e);

      if (socket != null) {
        try {
          socket.close();
        }
        catch (IOException inner) {
          LOG.error(inner);
        }
        finally {
          applicationManager.serverClosed();
        }
      }

      return;
    }

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        try {
          ServiceManager.getService(SocketInputHandler.class).read(socket.getInputStream(),
                                                                   FlexUIDesignerApplicationManager.getInstance().getAppDir());
        }
        catch (IOException e) {
          if (!(e instanceof SocketException && socket.isClosed())) {
            LOG.error(e);
          }
        }
        finally {
          try {
            close();
          }
          catch (IOException e) {
            LOG.error(e);
          }
          finally {
            applicationManager.serverClosed();
          }
        }
      }
    });

    myPendingTask.setOut(socketOutputStream);
    myPendingTask.run();
    myPendingTask = null;
  }

  public void close() throws IOException {
    ServiceManager.getService(SocketInputHandler.class).close();
    if (socket != null) {
      socket.close();
    }
  }

  public boolean isClosed() {
    return socket == null || socket.isClosed();
  }
}