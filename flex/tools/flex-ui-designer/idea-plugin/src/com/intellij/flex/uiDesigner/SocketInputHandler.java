package com.intellij.flex.uiDesigner;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.ActionCallback;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class SocketInputHandler implements Disposable {
  public abstract void read(@NotNull InputStream inputStream, @NotNull File appDir) throws IOException;

  public abstract int addCallback(ActionCallback actionCallback);

  public static SocketInputHandler getInstance() {
    return DesignerApplicationManager.getService(SocketInputHandler.class);
  }

  abstract SocketInputHandlerImpl.Reader getReader();

  public abstract void unregisterDocumentFactories() throws IOException;
}