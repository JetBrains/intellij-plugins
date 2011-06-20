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

  private FlexUIDesignerApplicationManager.PendingOpenDocumentTask pendingTask;

  private Socket socket;

  private FlexUIDesignerApplicationManager applicationManager;

  public Server(@NotNull final FlexUIDesignerApplicationManager.PendingOpenDocumentTask pendingTask,
                FlexUIDesignerApplicationManager applicationManager) {
    this.pendingTask = pendingTask;
    this.applicationManager = applicationManager;
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
      pendingTask.setOut(socket.getOutputStream());
    }
    catch (IOException e) {
      LOG.error(e);
      IOUtil.close(socket);
      pendingTask = null;
      applicationManager.destroyAdlProcess();
      return;
    }

    pendingTask.run();
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
    IOUtil.close(ServiceManager.getService(SocketInputHandler.class));
    IOUtil.close(socket);
  }

  public boolean isClosed() {
    return socket == null || socket.isClosed();
  }
}