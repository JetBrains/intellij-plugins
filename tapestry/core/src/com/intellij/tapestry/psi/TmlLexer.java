package com.intellij.tapestry.psi;

import com.intellij.lexer.XHtmlLexer;
import com.intellij.lexer.XmlLexer;
import com.intellij.lexer._XmlLexer;
import com.intellij.lexer.__XmlLexer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.xml.util.HtmlUtil.STYLE_ATTRIBUTE_NAME;

/**
 * @author Alexey Chmutov
 */
public class TmlLexer extends XHtmlLexer {

  public TmlLexer() {
    super(createElAwareXmlLexer());
  }

  @Override
  protected boolean isAttributeEmbedmentToken(@NotNull IElementType tokenType, @NotNull CharSequence attributeName) {
    return tokenType == TelTokenTypes.TAP5_EL_CONTENT ? !StringUtil.equals(attributeName, STYLE_ATTRIBUTE_NAME)
                                                      : super.isAttributeEmbedmentToken(tokenType, attributeName);
  }

  public static XmlLexer createElAwareXmlLexer() {
    final __XmlLexer internalLexer = new __XmlLexer(null);
    internalLexer.setElTypes(TelTokenTypes.TAP5_EL_CONTENT, TelTokenTypes.TAP5_EL_CONTENT);
    return new XmlLexer(new _XmlLexer(internalLexer));
  }
}
