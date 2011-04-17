package com.intellij.flex.uiDesigner.abc;

final class AbcNameFilterByEquals implements AbcNameFilter {
  private final String equals;

  public AbcNameFilterByEquals(String equals) {
    this.equals = equals;
  }

  @Override
  public boolean accept(String name) {
    return name.equals(equals);
  }
}
