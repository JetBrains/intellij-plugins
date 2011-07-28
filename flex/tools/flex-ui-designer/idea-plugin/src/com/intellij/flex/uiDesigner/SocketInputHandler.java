package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.Closable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public abstract class SocketInputHandler implements Closable {
  public static final Topic<DocumentOpenedListener> MESSAGE_TOPIC = new Topic<DocumentOpenedListener>(
      "Flex UI Designer document opened event (only requested)", DocumentOpenedListener.class);

  public abstract void read(@NotNull InputStream inputStream, @NotNull File appDir) throws IOException;

  public abstract DataOutputStream getErrorOut();

  public abstract void setErrorOut(OutputStream outputStream);

  public static SocketInputHandler getInstance() {
    return ServiceManager.getService(SocketInputHandler.class);
  }

  public interface DocumentOpenedListener {
    void documentOpened();
  }
}