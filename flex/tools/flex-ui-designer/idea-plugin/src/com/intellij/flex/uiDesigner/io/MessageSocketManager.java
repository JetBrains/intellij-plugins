package com.intellij.flex.uiDesigner.io;

import com.intellij.flex.uiDesigner.Client;
import com.intellij.flex.uiDesigner.DesignerApplicationLauncher;
import com.intellij.flex.uiDesigner.SocketInputHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

public class MessageSocketManager extends SocketManager {
  private DesignerApplicationLauncher pendingTask;
  private final File appDir;

  public MessageSocketManager(DesignerApplicationLauncher pendingTask, File appDir) {
    this.pendingTask = pendingTask;
    this.appDir = appDir;
  }

  @Override
  protected void setOut(OutputStream out) {
    Client.getInstance().setOut(out);
  }

  @Override
  protected void clientSocketNotAccepted() {
    pendingTask.clientSocketNotAccepted();
    pendingTask = null;
  }

  @Override
  protected void clientSocketAccepted() {
    pendingTask.clientOpened();
    pendingTask = null;

    try {
      SocketInputHandler.getInstance().read(socket.getInputStream(), appDir);
    }
    catch (IOException e) {
      if (!(e instanceof SocketException && socket.isClosed())) {
        LOG.error(e);
      }
    }
  }

  @Override
  public void dispose() {
    if (serverSocket != null) {
      pendingTask = null;
    }

    super.dispose();
  }
}
