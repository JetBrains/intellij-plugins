package com.intellij.flex.uiDesigner.libraries;

import gnu.trove.THashMap;

public interface DefinitionMapProcessor {
  void process(THashMap<CharSequence, Definition> definitionMap, AbcMerger abcMerger);
}
