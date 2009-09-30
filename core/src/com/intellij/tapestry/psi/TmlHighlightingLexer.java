package com.intellij.tapestry.psi;

import com.intellij.lexer.XHtmlHighlightingLexer;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;

/**
 * @author Alexey Chmutov
 *         Date: Jun 24, 2009
 *         Time: 10:52:01 PM
 */
public class TmlHighlightingLexer extends XHtmlHighlightingLexer {

  public TmlHighlightingLexer() {
    super(TmlLexer.createElAwareXmlLexer());
    registerHandler(TelTokenTypes.TAP5_EL_CONTENT, new ElEmbeddmentHandler());
  }

  @Override
  protected boolean isValidAttributeValueTokenType(final IElementType tokenType) {
    return super.isValidAttributeValueTokenType(tokenType) ||tokenType == TelTokenTypes.TAP5_EL_CONTENT;
  }

  @Override
  protected Lexer createELLexer(Lexer newLexer) {
    return getTokenType() == TelTokenTypes.TAP5_EL_CONTENT ? new TelLexer() : newLexer;
  }
}

