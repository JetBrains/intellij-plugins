package com.intellij.flex.uiDesigner;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface SocketInputHandler {
  void read(@NotNull InputStream inputStream, @NotNull File appDir) throws IOException;

  void close() throws IOException;
}