package com.intellij.flex.uiDesigner;

import com.intellij.openapi.Disposable;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class SocketInputHandler implements Disposable {
  public static final Topic<DocumentRenderedListener> MESSAGE_TOPIC = new Topic<DocumentRenderedListener>(
      "Flex UI Designer document opened event (only requested)", DocumentRenderedListener.class);

  public abstract void read(@NotNull InputStream inputStream, @NotNull File appDir) throws IOException;

  public static SocketInputHandler getInstance() {
    return DesignerApplicationManager.getService(SocketInputHandler.class);
  }

  public interface DocumentRenderedListener {
    void documentRendered(int id, BufferedImage image);
    void errorOccured();
  }
}