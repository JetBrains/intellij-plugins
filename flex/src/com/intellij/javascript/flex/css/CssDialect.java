package com.intellij.javascript.flex.css;

import com.intellij.lang.javascript.flex.FlexBundle;

/**
 * @author Eugene.Kudelevsky
 */
@SuppressWarnings({"UnusedDeclaration"})
public enum CssDialect {
  CLASSIC(FlexBundle.message("css.w3c.dialect.name")),
  FLEX(FlexBundle.message("css.flex.dialect.name"));

  private final String myDisplayName;

  private CssDialect(String displayName) {
    myDisplayName = displayName;
  }

  public String getDisplayName() {
    return myDisplayName;
  }
}
