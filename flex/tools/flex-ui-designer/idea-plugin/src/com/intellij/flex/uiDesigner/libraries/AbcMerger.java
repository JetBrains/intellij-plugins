package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.abc.AbcFilter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class AbcMerger extends AbcFilter {
  private final Map<CharSequence, Definition> definitionMap;

  public AbcMerger(Map<CharSequence, Definition> definitionMap, @Nullable String flexSdkVersion) {
    super(flexSdkVersion);
    this.definitionMap = definitionMap;
  }

  public void process(Library library) {
    filter();
  }
}
