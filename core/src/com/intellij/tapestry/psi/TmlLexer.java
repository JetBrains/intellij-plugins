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

  public void advance() {
    myTokenType = null;
    super.advance();
  }

  public IElementType getTokenType() {
    if (myTokenType != null) return myTokenType;

    myTokenType = super.getTokenType();
    myTokenStart = super.getTokenStart();
    myTokenEnd = super.getTokenEnd();

    if (myTokenType == ELTokenType.TAP5_EL_CONTENT) {
      myTokenType = ELTokenType.TAP5_EL_HOLDER;
    }

    return myTokenType;
  }

  public int getTokenStart() {
    if (myTokenType != null) {
      return myTokenStart;
    }
    return super.getTokenStart();
  }

  public int getTokenEnd() {
    if (myTokenType != null) {
      return myTokenEnd;
    }
    return super.getTokenEnd();
  }

  protected boolean isValidAttributeValueTokenType(final IElementType tokenType) {
    return super.isValidAttributeValueTokenType(tokenType) || tokenType == ELTokenType.TAP5_EL_CONTENT;
  }

  public static XmlLexer createElAwareXmlLexer() {
    return new XmlLexer(new XmlLexerWithEL());
  }

  private static class XmlLexerWithEL extends _XmlLexer implements ELHostLexer {
    public XmlLexerWithEL() {
      super(new __XmlLexerWithEL());
      setElTypes(ELTokenType.TAP5_EL_CONTENT, ELTokenType.TAP5_EL_CONTENT);
    }

    public void setElTypes(final IElementType jspElContent, final IElementType jspElContent1) {
      ((ELHostLexer)getFlex()).setElTypes(jspElContent, jspElContent1);
    }
  }

  private static class __XmlLexerWithEL extends __XmlLexer implements ELHostLexer {

    public __XmlLexerWithEL() {
      super((Reader)null);
    }

    public void setElTypes(IElementType _elTokenType, IElementType _elTokenType2) {
      elTokenType = _elTokenType;
      elTokenType2 = _elTokenType2;
    }
  }

}
