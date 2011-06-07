package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.Closable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface SocketInputHandler extends Closable {
  void read(@NotNull InputStream inputStream, @NotNull File appDir) throws IOException;
}