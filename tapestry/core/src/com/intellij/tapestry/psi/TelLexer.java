package com.intellij.tapestry.psi;

import com.intellij.lexer.FlexAdapter;

/**
 * @author Alexey Chmutov
 */
public class TelLexer extends FlexAdapter {
  public TelLexer() {
    super(new _TelLexer());
  }
}
