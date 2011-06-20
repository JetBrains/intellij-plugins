package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.Closable;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public interface SocketInputHandler extends Closable {
  void read(@NotNull InputStream inputStream, @NotNull File appDir) throws IOException;

  DataOutputStream getErrorOut();

  void setErrorOut(OutputStream outputStream);
}