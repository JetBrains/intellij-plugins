package com.intellij.flex.uiDesigner.abc;

import java.util.Collection;

public class AbcNameFilterByNameSetAndStartsWith extends AbcNameFilterByNameSet {
  private final String[] startsWith;

  public AbcNameFilterByNameSetAndStartsWith(Collection<CharSequence> definitions, String[] startsWith) {
    this(definitions, startsWith, false);
  }

  AbcNameFilterByNameSetAndStartsWith(Collection<CharSequence> definitions, String[] startsWith, boolean inclusion) {
    super(definitions, inclusion);
    this.startsWith = startsWith;
  }

  @Override
  public boolean accept(String name) {
    if (inclusion) {
      if (super.accept(name)) {
        return true;
      }
    }
    else if (!super.accept(name)) {
      return false;
    }

    for (String s : startsWith) {
      if (name.startsWith(s)) {
        return inclusion;
      }
    }

    return !inclusion;
  }
}
