// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.highlighting;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptHighlightingLexer;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.HtmlHighlightingLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.lexer.Angular2HtmlEmbeddedContentSupport;
import org.angular2.lang.html.lexer.Angular2HtmlLexer;
import org.angular2.lang.html.lexer._Angular2HtmlLexer;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.javascript.JSTokenTypes.STRING_LITERAL;
import static com.intellij.lang.javascript.JSTokenTypes.STRING_LITERAL_PART;
import static org.angular2.lang.html.lexer.Angular2HtmlLexer.Angular2HtmlMergingLexer.*;
import static org.angular2.lang.html.parser.Angular2HtmlElementTypes.*;

public class Angular2HtmlHighlightingLexer extends HtmlHighlightingLexer {


  @NonNls public static final IElementType EXPRESSION_WHITE_SPACE =
    new IElementType("NG:EXPRESSION_WHITE_SPACE", Angular2Language.INSTANCE);
  @NonNls public static final IElementType EXPANSION_FORM_CONTENT =
    new IElementType("NG:EXPANSION_FORM_CONTENT", Angular2HtmlLanguage.INSTANCE);
  @NonNls public static final IElementType EXPANSION_FORM_COMMA =
    new IElementType("NG:EXPANSION_FORM_COMMA", Angular2HtmlLanguage.INSTANCE);

  public Angular2HtmlHighlightingLexer(boolean tokenizeExpansionForms,
                                       @Nullable Pair<String, String> interpolationConfig,
                                       @Nullable FileType styleFileType) {
    super(new Angular2HtmlHighlightingMergingLexer(
            new FlexAdapter(new _Angular2HtmlLexer(tokenizeExpansionForms, interpolationConfig))),
          true, styleFileType);
  }

  @Override
  public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    super.start(buffer, startOffset, endOffset,
                ((Angular2HtmlLexer.Angular2HtmlMergingLexer)getDelegate()).initExpansionFormNestingLevel(initialState));
  }

  @Override
  public int getState() {
    return super.getState() | ((Angular2HtmlLexer.Angular2HtmlMergingLexer)getDelegate()).getExpansionFormNestingLevelState();
  }

  @Override
  protected boolean isHtmlTagState(int state) {
    return state == _Angular2HtmlLexer.START_TAG_NAME || state == _Angular2HtmlLexer.END_TAG_NAME;
  }

  @Override
  public IElementType getTokenType() {
    IElementType tokenType = super.getTokenType();

    final int state = getState();
    // we need to convert attribute names according to their function
    if (tokenType == XML_NAME && (state & BASE_STATE_MASK) == _Angular2HtmlLexer.TAG_ATTRIBUTES) {
      Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(getTokenText());
      if (info.type != Angular2AttributeType.REGULAR && Angular2HtmlEmbeddedContentSupport.NG_EL_ATTRIBUTES.contains(info.type)) {
        return info.type.getElementType();
      }
    }
    else if (tokenType != null && isLexerWithinExpansionForm(state)) {
      if (tokenType == TAG_WHITE_SPACE
          || tokenType == XML_REAL_WHITE_SPACE
          || tokenType == JSTokenTypes.IDENTIFIER
          || tokenType == XML_DATA_CHARACTERS) {
        return EXPANSION_FORM_CONTENT;
      }
      else if (tokenType == XML_COMMA) {
        return EXPANSION_FORM_COMMA;
      }
    }
    else if (tokenType == TAG_WHITE_SPACE && isLexerWithinInterpolation(state)
             || (tokenType == XML_WHITE_SPACE && getEmbeddedLexer() instanceof JavaScriptHighlightingLexer)) {
      return EXPRESSION_WHITE_SPACE;
    }
    else if (tokenType == TAG_WHITE_SPACE && (getBaseLexerState(state) == 0
                                              || isLexerWithinUnterminatedInterpolation(state))) {
      return XmlTokenType.XML_REAL_WHITE_SPACE;
    }
    else if (tokenType == STRING_LITERAL_PART) {
      return STRING_LITERAL;
    }
    return tokenType;
  }

  private static class Angular2HtmlHighlightingMergingLexer extends Angular2HtmlLexer.Angular2HtmlMergingLexer {

    Angular2HtmlHighlightingMergingLexer(@NotNull FlexAdapter original) {
      super(original);
    }

    @Override
    protected IElementType merge(IElementType type, Lexer originalLexer) {
      type = super.merge(type, originalLexer);
      if (type == XML_CHAR_ENTITY_REF) {
        while (originalLexer.getTokenType() == XML_CHAR_ENTITY_REF) {
          originalLexer.advance();
        }
        if (originalLexer.getTokenType() == XML_ATTRIBUTE_VALUE_TOKEN) {
          return XML_ATTRIBUTE_VALUE_TOKEN;
        }
      }
      return type;
    }
  }
}
