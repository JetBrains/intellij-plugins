package com.jetbrains.typoscript.lang;

import com.intellij.lexer.FlexAdapter;

import java.io.Reader;


public class TypoScriptLexer extends FlexAdapter {
  public TypoScriptLexer() {
    super(new _TypoScriptLexer((Reader)null));
  }
}
