package com.intellij.tapestry.psi;

import com.intellij.lexer.FlexAdapter;

/**
 * @author Alexey Chmutov
 *         Date: Jun 22, 2009
 *         Time: 9:12:21 PM
 */
public class TelLexer extends FlexAdapter {
  public TelLexer() {
    super(new _TelLexer());
  }
}
