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

public class Angular2HtmlLexer extends HtmlLexer {

  private static final TokenSet TOKENS_TO_MERGE =
    TokenSet.create(XML_COMMENT_CHARACTERS, XML_WHITE_SPACE, XML_REAL_WHITE_SPACE,
                    XML_ATTRIBUTE_VALUE_TOKEN, XML_DATA_CHARACTERS, XML_TAG_CHARACTERS);

  public Angular2HtmlLexer(boolean tokenizeExpansionForms,
                           @Nullable Pair<String, String> interpolationConfig) {
    super(new Angular2HtmlMergingLexer(
      new FlexAdapter(new _Angular2HtmlLexer()), tokenizeExpansionForms, interpolationConfig), true);
  }

  public static class Angular2HtmlMergingLexer extends MergingLexerAdapterBase {

    public static boolean isLexerWithinInterpolationOrExpansion(int state) {
      int scanningState = ((state & STATE_MASK) >> STATE_SHIFT) & 0x7;
      return scanningState == STATE_SCAN_EXPANSION_FORM_CONTENT
        || scanningState == STATE_SCAN_INTERPOLATION_CONTENT
        || scanningState == STATE_SCAN_UNTERMINATED_INTERPOLATION_CONTENT;
    }

    private static final int STATE_SHIFT = BASE_STATE_SHIFT + 3;
    private static final int STATE_MASK = 0x7ff << STATE_SHIFT;

    private static final int STATE_INITIAL = 0;
    private static final int STATE_SCAN_EXPANSION_FORM_CONTENT = 1;
    private static final int STATE_EXPANSION_FORM_END = 2;
    private static final int STATE_SCAN_INTERPOLATION_CONTENT = 3;
    private static final int STATE_SCAN_UNTERMINATED_INTERPOLATION_CONTENT = 4;
    private static final int STATE_SCAN_INTERPOLATION_END = 5;
    private static final int STATE_INTERPOLATION_END = 6;

    private final boolean myTokenizeExpansionForms;
    private final Pair<String, String> myInterpolationConfig;

    private int myScanningState;
    private int myExpansionFormNestingLevel;

    public Angular2HtmlMergingLexer(@NotNull Lexer original, boolean tokenizeExpansionForms,
                                    @Nullable Pair<String, String> interpolationConfig) {
      super(original);
      myTokenizeExpansionForms = tokenizeExpansionForms;
      myInterpolationConfig = interpolationConfig == null ? Pair.create("{{", "}}") : interpolationConfig;
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
      super.start(buffer, startOffset, endOffset, initialState & ~STATE_MASK);
      myScanningState = ((initialState & STATE_MASK) >> STATE_SHIFT) & 0x7;
      myExpansionFormNestingLevel = ((initialState & STATE_MASK) >> STATE_SHIFT) >> 3;
    }

    @Override
    public int getState() {
      return super.getState() | ((((myExpansionFormNestingLevel & 0xFF) << 3) | myScanningState) << STATE_SHIFT);
    }

    @Override
    public MergeFunction getMergeFunction() {
      return this::merge;
    }

    @Override
    public void restore(@NotNull LexerPosition position) {
      super.restore(((MyLexerPosition)position).getOriginal());
      myScanningState = ((MyLexerPosition)position).getScanningState();
      myExpansionFormNestingLevel = ((MyLexerPosition)position).getExpansionFormNestingLevel();
    }

    @NotNull
    @Override
    public LexerPosition getCurrentPosition() {
      return new MyLexerPosition(super.getCurrentPosition(), myScanningState, myExpansionFormNestingLevel);
    }

    private IElementType merge(IElementType type, Lexer originalLexer) {
      switch (myScanningState) {
        case STATE_EXPANSION_FORM_END:
        case STATE_INTERPOLATION_END:
        case STATE_INITIAL:
          if (INTERPOLATION_CONTENT_TOKENS.contains(type)
              && tryConsumeInterpolationBoundary(myInterpolationConfig.first)) {
            myScanningState = STATE_SCAN_INTERPOLATION_CONTENT;
            return INTERPOLATION_START;
          }
          else if (myTokenizeExpansionForms && type == LBRACE) {
            myScanningState = STATE_SCAN_EXPANSION_FORM_CONTENT;
            return EXPANSION_FORM_START;
          }
          else if (type == RBRACE && myExpansionFormNestingLevel > 0) {
            myScanningState = STATE_SCAN_EXPANSION_FORM_CONTENT;
            myExpansionFormNestingLevel--;
            return EXPANSION_FORM_CASE_END;
          }
          myScanningState = STATE_INITIAL;
          break;
        case STATE_SCAN_EXPANSION_FORM_CONTENT:
          if (type == LBRACE) {
            myScanningState = STATE_INITIAL;
            myExpansionFormNestingLevel++;
            return EXPANSION_FORM_CASE_START;
          }
          else if (type == RBRACE) {
            myScanningState = STATE_EXPANSION_FORM_END;
            return EXPANSION_FORM_END;
          }
          break;
        case STATE_SCAN_INTERPOLATION_CONTENT:
          if (!INTERPOLATION_CONTENT_TOKENS.contains(type)) {
            myScanningState = STATE_INTERPOLATION_END;
            break;
          }
          if (tryConsumeInterpolationBoundary(myInterpolationConfig.second)) {
            myScanningState = STATE_INTERPOLATION_END;
            return INTERPOLATION_END;
          }
          LexerPosition position = getOriginal().getCurrentPosition();
          if (tryConsumeInterpolationContent()) {
            myScanningState = STATE_SCAN_INTERPOLATION_END;
            return INTERPOLATION_EXPR;
          }
          else {
            getOriginal().restore(position);
            myScanningState = STATE_SCAN_UNTERMINATED_INTERPOLATION_CONTENT;
          }
          break;
        case STATE_SCAN_UNTERMINATED_INTERPOLATION_CONTENT:
          if (!INTERPOLATION_CONTENT_TOKENS.contains(type)) {
            myScanningState = STATE_INTERPOLATION_END;
            break;
          }
          break;
        case STATE_SCAN_INTERPOLATION_END:
          if (tryConsumeInterpolationBoundary(myInterpolationConfig.second)) {
            myScanningState = STATE_INTERPOLATION_END;
            return INTERPOLATION_END;
          }
          myScanningState = STATE_INTERPOLATION_END;
          break;
      }
      type = convertType(type);
      if (!TOKENS_TO_MERGE.contains(type)) {
        return type;
      }
      while (true) {
        final IElementType tokenType = convertType(originalLexer.getTokenType());
        if (tokenType != type
            || (INTERPOLATION_CONTENT_TOKENS.contains(tokenType)
                && ((myScanningState != STATE_SCAN_INTERPOLATION_CONTENT && inBuffer(myInterpolationConfig.first, 0))
                    || (myScanningState == STATE_SCAN_INTERPOLATION_CONTENT && inBuffer(myInterpolationConfig.second, 0))))) {
          break;
        }
        originalLexer.advance();
      }
      return type;
    }

    @Contract("null -> null; !null -> !null")
    private IElementType convertType(@Nullable IElementType tokenType) {
      if (tokenType == XML_COMMA) {
        return myScanningState != STATE_SCAN_EXPANSION_FORM_CONTENT
               ? XML_DATA_CHARACTERS : XML_COMMA;
      }
      if (tokenType == LBRACE) {
        return myTokenizeExpansionForms
               && myScanningState != STATE_SCAN_INTERPOLATION_CONTENT
               && myScanningState != STATE_SCAN_UNTERMINATED_INTERPOLATION_CONTENT
               ? LBRACE : XML_DATA_CHARACTERS;
      }
      if (tokenType == RBRACE) {
        return (myScanningState == STATE_SCAN_EXPANSION_FORM_CONTENT
                || (myExpansionFormNestingLevel > 0
                    && (myScanningState == STATE_INTERPOLATION_END
                        || myScanningState == STATE_EXPANSION_FORM_END
                        || myScanningState == STATE_INITIAL))) ?
               RBRACE : XML_DATA_CHARACTERS;
      }
      return tokenType;
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
      final Lexer originalLexer = getOriginal();
      while (INTERPOLATION_CONTENT_TOKENS.contains(originalLexer.getTokenType())
             && !inBuffer(myInterpolationConfig.second, 0)) {
        originalLexer.advance();
      }
      return INTERPOLATION_CONTENT_TOKENS.contains(originalLexer.getTokenType());
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
    private final int myScanningState;
    private final int myExpansionFormNestingLevel;

    private MyLexerPosition(@NotNull LexerPosition original, int scanningState, int expansionFormNestingLevel) {
      myOriginal = original;
      myScanningState = scanningState;
      myExpansionFormNestingLevel = expansionFormNestingLevel;
    }

    public int getExpansionFormNestingLevel() {
      return myExpansionFormNestingLevel;
    }

    public int getScanningState() {
      return myScanningState;
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
