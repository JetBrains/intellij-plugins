package com.intellij.flex.uiDesigner.abc;

public class AbcNameFilterStartsWith implements AbcNameFilter {
  private final boolean inclusion;
  private final String startsWith;

  public AbcNameFilterStartsWith(String startsWith, boolean inclusion) {
    this.startsWith = startsWith;
    this.inclusion = inclusion;
  }

  @Override
  public boolean accept(String name) {
    if (name.startsWith(startsWith)) {
      return inclusion;
    }

    return !inclusion;
  }
}
