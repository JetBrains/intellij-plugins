// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.lexer;

import com.intellij.lexer.*;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.xml.XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;
import static com.intellij.psi.xml.XmlTokenType.XML_COMMA;
import static com.intellij.psi.xml.XmlTokenType.XML_COMMENT_CHARACTERS;
import static com.intellij.psi.xml.XmlTokenType.XML_DATA_CHARACTERS;
import static com.intellij.psi.xml.XmlTokenType.XML_REAL_WHITE_SPACE;
import static com.intellij.psi.xml.XmlTokenType.XML_TAG_CHARACTERS;
import static com.intellij.psi.xml.XmlTokenType.XML_WHITE_SPACE;
import static org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR;
import static org.angular2.lang.html.lexer.Angular2HtmlTokenTypes.*;
import static org.angular2.lang.html.lexer.Angular2HtmlTokenTypes.XML_CHAR_ENTITY_REF;
import static org.angular2.lang.html.lexer.Angular2HtmlTokenTypes.XML_ENTITY_REF_TOKEN;

public class Angular2HtmlLexer extends HtmlLexer {

  private static final TokenSet TOKENS_TO_MERGE =
    TokenSet.create(XML_COMMENT_CHARACTERS, XML_WHITE_SPACE, XML_REAL_WHITE_SPACE,
                    XML_ATTRIBUTE_VALUE_TOKEN, XML_DATA_CHARACTERS, XML_TAG_CHARACTERS);

  private static final TokenSet EXPANSION_TOKENS =
    TokenSet.create(XML_COMMA, LBRACE, RBRACE);

  private static final TokenSet INTERPOLATION_CONTENT_TOKENS =
    TokenSet.orSet(TokenSet.create(XML_REAL_WHITE_SPACE, XML_ATTRIBUTE_VALUE_TOKEN,
                                   XML_CHAR_ENTITY_REF, XML_ENTITY_REF_TOKEN,
                                   XML_DATA_CHARACTERS),
                   EXPANSION_TOKENS);

  public Angular2HtmlLexer(boolean tokenizeExpansionForms,
                           @Nullable Pair<String, String> interpolationConfig) {
    super(new Angular2HtmlMergingLexer(
      new FlexAdapter(new _Angular2HtmlLexer()), tokenizeExpansionForms, interpolationConfig), true);
  }

  public static class Angular2HtmlMergingLexer extends MergingLexerAdapterBase {

    private static final int INTERPOLATION_STATE_OFFSET = 29;
    private static final int INTERPOLATION_STATE_MASK = 3 << INTERPOLATION_STATE_OFFSET;

    private final boolean myTokenizeExpansionForms;
    private final Pair<String, String> myInterpolationConfig;

    private int myInterpolationScanningState;

    public Angular2HtmlMergingLexer(@NotNull Lexer original, boolean tokenizeExpansionForms,
                                    @Nullable Pair<String, String> interpolationConfig) {
      super(original);
      myTokenizeExpansionForms = tokenizeExpansionForms;
      myInterpolationConfig = interpolationConfig == null ? Pair.create("{{", "}}") : interpolationConfig;
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
      super.start(buffer, startOffset, endOffset, initialState & ~INTERPOLATION_STATE_MASK);
      myInterpolationScanningState = (initialState & INTERPOLATION_STATE_MASK) >> INTERPOLATION_STATE_OFFSET;
    }

    @Override
    public int getState() {
      return super.getState() | (myInterpolationScanningState << INTERPOLATION_STATE_OFFSET);
    }

    @Override
    public MergeFunction getMergeFunction() {
      return this::merge;
    }

    @Override
    public void restore(@NotNull LexerPosition position) {
      super.restore(((MyLexerPosition)position).getOriginal());
      myInterpolationScanningState = ((MyLexerPosition)position).getInterpolationScanningState();
    }

    @NotNull
    @Override
    public LexerPosition getCurrentPosition() {
      return new MyLexerPosition(super.getCurrentPosition(), myInterpolationScanningState);
    }

    private IElementType merge(IElementType type, Lexer originalLexer) {
      if (INTERPOLATION_CONTENT_TOKENS.contains(type)) {
        switch (myInterpolationScanningState) {
          case 3:
            myInterpolationScanningState = 0;
          case 0:
            if (tryConsumeInterpolationBoundary(myInterpolationConfig.first)) {
              myInterpolationScanningState = 1;
              return INTERPOLATION_START;
            }
            break;
          case 1:
            if (tryConsumeInterpolationContent()) {
              myInterpolationScanningState = 2;
              return INTERPOLATION_EXPR;
            }
          case 2:
            if (tryConsumeInterpolationBoundary(myInterpolationConfig.second)) {
              myInterpolationScanningState = 3;
              return INTERPOLATION_END;
            }
            myInterpolationScanningState = 0;
            break;
        }
      }
      else {
        myInterpolationScanningState = 0;
      }
      type = convertType(type);
      if (!TOKENS_TO_MERGE.contains(type)) {
        return type;
      }
      while (true) {
        final IElementType tokenType = convertType(originalLexer.getTokenType());
        if (tokenType != type
            || (INTERPOLATION_CONTENT_TOKENS.contains(tokenType)
                && inBuffer(myInterpolationConfig.first, 0))) {
          break;
        }
        originalLexer.advance();
      }
      return type;
    }

    @Contract("null -> null; !null -> !null")
    private IElementType convertType(@Nullable IElementType tokenType) {
      return !myTokenizeExpansionForms && EXPANSION_TOKENS.contains(tokenType) ?
             XML_DATA_CHARACTERS : tokenType;
    }

    private boolean tryConsumeInterpolationBoundary(String boundary) {
      if (inBuffer(boundary, -1)) {
        final Lexer original = getOriginal();
        int interpolationCharsToConsume = boundary.length() - 1;
        while (interpolationCharsToConsume > 0) {
          original.advance();
          interpolationCharsToConsume--;
        }
        return true;
      }
      return false;
    }

    private boolean tryConsumeInterpolationContent() {
      if (inBuffer(myInterpolationConfig.second, -1)) {
        return false;
      }
      final Lexer originalLexer = getOriginal();
      while (true) {
        final IElementType tokenType = originalLexer.getTokenType();
        if (!INTERPOLATION_CONTENT_TOKENS.contains(tokenType)
            || inBuffer(myInterpolationConfig.second, 0)) {
          break;
        }
        originalLexer.advance();
      }
      return true;
    }

    private boolean inBuffer(@NotNull String text, int offset) {
      final Lexer original = getOriginal();
      final int tokenPos = original.getTokenStart() + offset;
      return tokenPos >= 0 && text.contentEquals(
        original
          .getBufferSequence()
          .subSequence(tokenPos, Math.min(tokenPos + text.length(), original.getBufferEnd())));
    }
  }

  private static class MyLexerPosition implements LexerPosition {

    private final LexerPosition myOriginal;
    private final int myInterpolationScanningState;

    private MyLexerPosition(@NotNull LexerPosition original, int interpolationScanningState) {
      myOriginal = original;
      myInterpolationScanningState = interpolationScanningState;
    }

    public int getInterpolationScanningState() {
      return myInterpolationScanningState;
    }

    @NotNull
    public LexerPosition getOriginal() {
      return myOriginal;
    }

    @Override
    public int getOffset() {
      return myOriginal.getOffset();
    }

    @Override
    public int getState() {
      return myOriginal.getState();
    }
  }
}
