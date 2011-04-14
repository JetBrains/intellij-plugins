package com.intellij.flex.uiDesigner.io;

public interface ByteProvider {
  int size();

  int writeTo(byte[] bytes, int offset);
}
