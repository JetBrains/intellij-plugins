// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.HtmlHighlightingLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.angular2.lang.expr.highlighting.Angular2SyntaxHighlighter;
import org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType;
import org.angular2.lang.html.lexer.Angular2HtmlLexer;
import org.angular2.lang.html.lexer._Angular2HtmlLexer;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.Nullable;

import static org.angular2.lang.html.parser.Angular2HtmlElementTypes.*;

public class Angular2HtmlHighlightingLexer extends HtmlHighlightingLexer {

  private static final TokenSet NG_EL_ATTRIBUTES = TokenSet.create(EVENT, BANANA_BOX_BINDING,
                                                                   PROPERTY_BINDING, TEMPLATE_BINDINGS);

  private Lexer angular2ExpressionLexer;

  public Angular2HtmlHighlightingLexer(boolean tokenizeExpansionForms, Pair<String, String> interpolationConfig) {
    super(new Angular2HtmlLexer.Angular2HtmlMergingLexer(
            new FlexAdapter(new _Angular2HtmlLexer()), tokenizeExpansionForms, interpolationConfig),
          true, FileTypeRegistry.getInstance().findFileTypeByName("CSS"));
    registerHandler(Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, new ElEmbeddmentHandler());
  }

  @Override
  protected Lexer getInlineScriptHighlightingLexer() {
    return new Angular2SyntaxHighlighter().getHighlightingLexer();
  }

  @Nullable
  @Override
  protected Lexer createELLexer(Lexer newLexer) {
    return new Angular2SyntaxHighlighter().getHighlightingLexer();
  }

  @Override
  public IElementType getTokenType() {
    if (angular2ExpressionLexer != null) {
      return angular2ExpressionLexer.getTokenType();
    }
    IElementType tokenType = super.getTokenType();

    // we need to convert attribute names according to their function
    if (tokenType == XML_NAME && (getState() & BASE_STATE_MASK) == _Angular2HtmlLexer.TAG_ATTRIBUTES) {
      Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(getTokenText(), true);
      if (info.elementType != XML_ATTRIBUTE) {
        seenScript = NG_EL_ATTRIBUTES.contains(info.elementType);
        seenAttribute = true;
        return info.elementType;
      }
      seenScript = false;
    }
    return tokenType;
  }

  @Override
  public void advance() {
    if (angular2ExpressionLexer != null) {
      angular2ExpressionLexer.advance();
      if (angular2ExpressionLexer.getTokenType() == null) {
        angular2ExpressionLexer = null;
      }
    }

    if (angular2ExpressionLexer == null) {
      super.advance();
    }
  }
}
