package com.intellij.plugins.drools.lang.lexer;

import com.intellij.lexer.FlexAdapter;

public class DroolsFlexLexer extends FlexAdapter {
  public DroolsFlexLexer() {
    super(new _DroolsLexer(null));
  }
}
