package com.intellij.flex.uiDesigner.libraries;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

public interface DefinitionProcessor {
  void process(CharSequence name, ByteBuffer buffer, Definition definition, Map<CharSequence, Definition> definitionMap) throws IOException;
}