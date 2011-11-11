package com.intellij.flex.uiDesigner;

import com.intellij.openapi.Disposable;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class SocketInputHandler implements Disposable {
  public static final Topic<DocumentOpenedListener> MESSAGE_TOPIC = new Topic<DocumentOpenedListener>(
      "Flex UI Designer document opened event (only requested)", DocumentOpenedListener.class);

  public abstract void read(@NotNull InputStream inputStream, @NotNull File appDir) throws IOException;

  public static SocketInputHandler getInstance() {
    return DesignerApplicationManager.getService(SocketInputHandler.class);
  }

  public interface DocumentOpenedListener {
    void documentOpened();
    void errorOccured();
  }
}