package com.jetbrains.lang.dart;

import com.intellij.lexer.FlexAdapter;

import java.io.Reader;

/**
 * @author: Fedor.Korotkov
 */
public class DartFlexLexer extends FlexAdapter {
  public DartFlexLexer() {
    super(new _DartLexer((Reader)null));
  }
}
