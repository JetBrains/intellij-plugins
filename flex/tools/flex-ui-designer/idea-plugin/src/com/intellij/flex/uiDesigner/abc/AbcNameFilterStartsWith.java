package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.text.StringUtil;

public class AbcNameFilterStartsWith implements AbcNameFilter {
  private final boolean inclusion;
  private final String startsWith;

  public AbcNameFilterStartsWith(String startsWith, boolean inclusion) {
    this.startsWith = startsWith;
    this.inclusion = inclusion;
  }

  @Override
  public boolean accept(CharSequence name) {
    if (StringUtil.startsWith(name, startsWith)) {
      return inclusion;
    }

    return !inclusion;
  }
}
