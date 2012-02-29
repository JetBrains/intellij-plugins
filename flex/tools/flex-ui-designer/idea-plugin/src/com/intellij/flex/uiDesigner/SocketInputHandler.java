package com.intellij.flex.uiDesigner;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class SocketInputHandler implements Disposable {
  public static final Topic<DocumentRenderedListener> MESSAGE_TOPIC =
    new Topic<DocumentRenderedListener>("Flash UI Designer document rendered event", DocumentRenderedListener.class);

  public abstract void read(@NotNull InputStream inputStream, @NotNull File appDir) throws IOException;

  public abstract int addCallback(ActionCallback actionCallback);

  public static SocketInputHandler getInstance() {
    return DesignerApplicationManager.getService(SocketInputHandler.class);
  }

  public interface DocumentRenderedListener {
    void documentRenderedOnAutoSave(DocumentFactoryManager.DocumentInfo info);
    void errorOccured();
  }

  abstract SocketInputHandlerImpl.Reader getReader();
}