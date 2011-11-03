package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;

public final class AbcNameFilterByEquals implements Condition<CharSequence> {
  private final String equals;

  public AbcNameFilterByEquals(String equals) {
    this.equals = equals;
  }

  @Override
  public boolean value(CharSequence name) {
    return StringUtil.equals(name, equals);
  }
}
