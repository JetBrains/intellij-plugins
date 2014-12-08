package com.dmarcotte.handlebars.parsing;

import com.intellij.lexer.FlexAdapter;

import java.io.Reader;


public class HbRawLexer extends FlexAdapter {
  public HbRawLexer() {
    super(new _HbLexer((Reader)null));
  }
}