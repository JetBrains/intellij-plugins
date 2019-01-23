package com.dmarcotte.handlebars.parsing;

import com.intellij.lexer.FlexAdapter;


public class HbRawLexer extends FlexAdapter {
  public HbRawLexer() {
    super(new _HbLexer(null));
  }
}