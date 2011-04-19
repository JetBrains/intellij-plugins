package com.intellij.flex.uiDesigner.abc;

import java.util.Collection;

public class AbcNameFilterByNameSet implements AbcNameFilter {
  private final Collection<CharSequence> definitions;
  protected final boolean inclusion;

  public AbcNameFilterByNameSet(Collection<CharSequence> definitions) {
    this.definitions = definitions;
    inclusion = false;
  }

  AbcNameFilterByNameSet(Collection<CharSequence> definitions, boolean inclusion) {
    this.definitions = definitions;
    this.inclusion = inclusion;
  }

  @Override
  public boolean accept(String name) {
    return definitions.contains(name) == inclusion;
  }
}
