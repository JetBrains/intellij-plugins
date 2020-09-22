package com.intellij.tapestry.psi;

import com.intellij.lexer.XHtmlLexer;
import com.intellij.lexer.XmlLexer;
import com.intellij.lexer._XmlLexer;
import com.intellij.lexer.__XmlLexer;

/**
 * @author Alexey Chmutov
 */
public class TmlLexer extends XHtmlLexer {

  public TmlLexer() {
    super(createElAwareXmlLexer());
  }

  @Override
  public boolean isElLexer() {
    return true;
  }

  public static XmlLexer createElAwareXmlLexer() {
    final __XmlLexer internalLexer = new __XmlLexer(null);
    internalLexer.setElTypes(TelTokenTypes.TAP5_EL_CONTENT, TelTokenTypes.TAP5_EL_CONTENT);
    return new XmlLexer(new _XmlLexer(internalLexer));
  }
}
