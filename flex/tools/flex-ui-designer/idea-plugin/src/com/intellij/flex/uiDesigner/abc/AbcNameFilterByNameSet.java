package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.Condition;

import java.util.Collection;

public class AbcNameFilterByNameSet implements Condition<CharSequence> {
  private final Collection<CharSequence> definitions;
  protected final boolean inclusion;

  public AbcNameFilterByNameSet(Collection<CharSequence> definitions) {
    this(definitions, false);
  }

  public AbcNameFilterByNameSet(Collection<CharSequence> definitions, boolean inclusion) {
    this.definitions = definitions;
    this.inclusion = inclusion;
  }

  @Override
  public boolean value(CharSequence name) {
    return definitions.contains(name) == inclusion;
  }
}
