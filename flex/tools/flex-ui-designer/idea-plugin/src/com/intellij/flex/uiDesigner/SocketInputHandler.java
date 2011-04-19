package com.intellij.flex.uiDesigner;

import java.io.IOException;
import java.io.InputStream;

public interface SocketInputHandler {
  void read(InputStream inputStream) throws IOException;

  void close() throws IOException;
}