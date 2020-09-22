package com.intellij.tapestry.psi;

import com.intellij.lexer.XHtmlHighlightingLexer;

/**
 * @author Alexey Chmutov
 */
public class TmlHighlightingLexer extends XHtmlHighlightingLexer {

  public TmlHighlightingLexer() {
    super(TmlLexer.createElAwareXmlLexer());
  }
}

