package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.text.StringUtil;

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
  public boolean accept(CharSequence name) {
    if (inclusion) {
      if (super.accept(name)) {
        return true;
      }
    }
    else if (!super.accept(name)) {
      return false;
    }

    for (String s : startsWith) {
      if (StringUtil.startsWith(name, s)) {
        return inclusion;
      }
    }

    return !inclusion;
  }
}
