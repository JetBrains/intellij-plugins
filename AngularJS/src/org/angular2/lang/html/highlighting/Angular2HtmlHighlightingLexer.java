// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lexer.*;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.highlighting.Angular2SyntaxHighlighter;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.lexer.Angular2HtmlLexer;
import org.angular2.lang.html.lexer._Angular2HtmlLexer;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

import static org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR;
import static org.angular2.lang.html.lexer.Angular2HtmlLexer.Angular2HtmlMergingLexer.*;
import static org.angular2.lang.html.parser.Angular2HtmlElementTypes.*;

public class Angular2HtmlHighlightingLexer extends HtmlHighlightingLexer {

  private static final EnumSet<Angular2AttributeType> NG_EL_ATTRIBUTES = EnumSet.of(Angular2AttributeType.EVENT,
                                                                                    Angular2AttributeType.BANANA_BOX_BINDING,
                                                                                    Angular2AttributeType.PROPERTY_BINDING,
                                                                                    Angular2AttributeType.TEMPLATE_BINDINGS);

  static final IElementType EXPRESSION_WHITE_SPACE = new IElementType("NG:EXPRESSION_WHITE_SPACE", Angular2Language.INSTANCE);
  static final IElementType EXPANSION_FORM_CONTENT = new IElementType("NG:EXPANSION_FORM_CONTENT", Angular2HtmlLanguage.INSTANCE);
  static final IElementType EXPANSION_FORM_COMMA = new IElementType("NG:EXPANSION_FORM_COMMA", Angular2HtmlLanguage.INSTANCE);

  public Angular2HtmlHighlightingLexer(boolean tokenizeExpansionForms,
                                       @Nullable Pair<String, String> interpolationConfig,
                                       @Nullable FileType styleFileType) {
    super(new Angular2HtmlLexer.Angular2HtmlMergingLexer(
            new FlexAdapter(new _Angular2HtmlLexer(tokenizeExpansionForms, interpolationConfig))),
          true, styleFileType);
    registerHandler(INTERPOLATION_EXPR, new ElEmbeddmentHandler());
  }

  @Override
  protected Lexer getInlineScriptHighlightingLexer() {
    return new MergingLexerAdapterBase(new Angular2SyntaxHighlighter().getHighlightingLexer()) {
      @Override
      public MergeFunction getMergeFunction() {
        return (type, lexer) -> type == JSTokenTypes.WHITE_SPACE ? EXPRESSION_WHITE_SPACE : type;
      }
    };
  }

  @Nullable
  @Override
  protected Lexer createELLexer(Lexer newLexer) {
    return getInlineScriptHighlightingLexer();
  }

  @Override
  public IElementType getTokenType() {
    IElementType tokenType = super.getTokenType();

    final int state = getState();
    // we need to convert attribute names according to their function
    if (tokenType == XML_NAME && (state & BASE_STATE_MASK) == _Angular2HtmlLexer.TAG_ATTRIBUTES
        && (seenScript == seenAttribute)) {
      Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(getTokenText(), true);
      if (info.type != Angular2AttributeType.REGULAR) {
        seenScript = NG_EL_ATTRIBUTES.contains(info.type);
        seenAttribute = true;
        return info.type.getElementType();
      }
      seenScript = false;
    }
    else if (tokenType != null && isLexerWithinExpansionForm(state)) {
      if (tokenType == TAG_WHITE_SPACE
          || tokenType == JSTokenTypes.IDENTIFIER
          || tokenType == XML_DATA_CHARACTERS) {
        return EXPANSION_FORM_CONTENT;
      }
      else if (tokenType == XML_COMMA) {
        return EXPANSION_FORM_COMMA;
      }
    }
    else if (tokenType == TAG_WHITE_SPACE && isLexerWithinInterpolation(state)
             || (tokenType == XML_WHITE_SPACE && hasSeenScript())) {
      return EXPRESSION_WHITE_SPACE;
    }
    else if (tokenType == TAG_WHITE_SPACE && (getBaseLexerState(state) == 0
                                              || isLexerWithinUnterminatedInterpolation(state))) {
      return XmlTokenType.XML_REAL_WHITE_SPACE;
    }
    return tokenType;
  }
}
