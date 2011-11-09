package com.intellij.flex.uiDesigner.libraries;

import gnu.trove.THashMap;

import java.io.IOException;

public interface DefinitionMapProcessor {
  void process(THashMap<CharSequence, Definition> definitionMap, AbcMerger abcMerger) throws IOException;
}
