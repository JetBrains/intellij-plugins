package com.intellij.flex.uiDesigner.io;

import com.intellij.flex.uiDesigner.SocketInputHandler;

import java.io.OutputStream;

public class ErrorSocketManager extends SocketManager {
  @Override
  protected void setOut(OutputStream out) {
    SocketInputHandler.getInstance().setErrorOut(out);
  }
}
