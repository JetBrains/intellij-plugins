package com.intellij.tapestry.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;

import java.io.Reader;

/**
 * @author Alexey Chmutov
 *         Date: Jun 18, 2009
 *         Time: 9:43:36 PM
 */
public class TmlLexer extends XHtmlLexer {
  private IElementType myTokenType;
  private int myTokenStart;
  private int myTokenEnd;

  public TmlLexer() {
    super(createElAwareXmlLexer());
  }

  @Override
  public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
    myTokenType = null;
    super.start(buffer, startOffset, endOffset, initialState);
  }

  @Override
  public void advance() {
    myTokenType = null;
    super.advance();
  }

  @Override
  public IElementType getTokenType() {
    if (myTokenType != null) return myTokenType;

    myTokenType = super.getTokenType();
    myTokenStart = super.getTokenStart();
    myTokenEnd = super.getTokenEnd();

    if (myTokenType == TelElementTypes.TAP5_EL_CONTENT) {
      myTokenType = TelElementTypes.TAP5_EL_HOLDER;
    }

    return myTokenType;
  }

  @Override
  public int getTokenStart() {
    return myTokenType != null ? myTokenStart : super.getTokenStart();
  }

  @Override
  public int getTokenEnd() {
    return myTokenType != null ? myTokenEnd : super.getTokenEnd();
  }

  @Override
  protected boolean isValidAttributeValueTokenType(final IElementType tokenType) {
    return super.isValidAttributeValueTokenType(tokenType) || tokenType == TelElementTypes.TAP5_EL_CONTENT;
  }

  public static XmlLexer createElAwareXmlLexer() {
    return new XmlLexer(new _XmlLexer(new __XmlLexer((Reader)null) {
      {
        elTokenType = TelElementTypes.TAP5_EL_CONTENT;
        elTokenType2 = TelElementTypes.TAP5_EL_CONTENT;
      }
    }));
  }
}
