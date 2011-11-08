package com.intellij.flex.uiDesigner.libraries;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface DefinitionProcessor {
  void process(CharSequence name, ByteBuffer buffer) throws IOException;
}