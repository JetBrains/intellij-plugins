package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.text.StringUtil;

final class AbcNameFilterByEquals implements AbcNameFilter {
  private final String equals;

  public AbcNameFilterByEquals(String equals) {
    this.equals = equals;
  }

  @Override
  public boolean accept(CharSequence name) {
    return StringUtil.equals(name, equals);
  }
}
