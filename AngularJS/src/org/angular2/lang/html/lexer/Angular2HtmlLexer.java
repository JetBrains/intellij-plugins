// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.lexer;

import com.intellij.lexer.*;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.xml.XmlTokenType.*;
import static org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR;
import static org.angular2.lang.html.lexer.Angular2HtmlTokenTypes.INTERPOLATION_END;
import static org.angular2.lang.html.lexer.Angular2HtmlTokenTypes.INTERPOLATION_START;

public class Angular2HtmlLexer extends HtmlLexer {

  private static final TokenSet TOKENS_TO_MERGE =
    TokenSet.create(XML_COMMENT_CHARACTERS, XML_WHITE_SPACE, XML_REAL_WHITE_SPACE,
                    XML_ATTRIBUTE_VALUE_TOKEN, XML_DATA_CHARACTERS, XML_TAG_CHARACTERS);

  public Angular2HtmlLexer(boolean tokenizeExpansionForms,
                           @Nullable Pair<String, String> interpolationConfig) {
    super(new Angular2HtmlMergingLexer(new FlexAdapter(new _Angular2HtmlLexer(
      tokenizeExpansionForms, interpolationConfig))), true);
  }

  public static class Angular2HtmlMergingLexer extends MergingLexerAdapterBase {

    public static boolean isLexerWithinInterpolationOrExpansion(int state) {
      state &= 0x7F;
      return state == _Angular2HtmlLexer.EXPANSION_FORM_CONTENT
             || state == _Angular2HtmlLexer.EXPANSION_FORM_CASE_END
             || state == _Angular2HtmlLexer.INTERPOLATION
             || state == _Angular2HtmlLexer.UNTERMINATED_INTERPOLATION;
    }

    private static final int STATE_SHIFT = BASE_STATE_SHIFT + 3;
    private static final int STATE_MASK = 0xff << STATE_SHIFT;

    private int myExpansionFormNestingLevel;

    public Angular2HtmlMergingLexer(@NotNull FlexAdapter original) {
      super(original);
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
      getFlexLexer().setExpansionFormNestingLevel((initialState & STATE_MASK) >> STATE_SHIFT);
      super.start(buffer, startOffset, endOffset, initialState & ~STATE_MASK);
    }

    @Override
    public int getState() {
      return super.getState() | ((myExpansionFormNestingLevel & 0xFF) << STATE_SHIFT);
    }

    @Override
    public MergeFunction getMergeFunction() {
      return Angular2HtmlMergingLexer::merge;
    }

    @Override
    public void restore(@NotNull LexerPosition position) {
      getFlexLexer().setExpansionFormNestingLevel(((MyLexerPosition)position).getExpansionFormNestingLevel());
      super.restore(((MyLexerPosition)position).getOriginal());
    }

    @NotNull
    @Override
    public LexerPosition getCurrentPosition() {
      return new MyLexerPosition(super.getCurrentPosition(), myExpansionFormNestingLevel);
    }

    @Override
    public void advance() {
      myExpansionFormNestingLevel = getFlexLexer().getExpansionFormNestingLevel();
      super.advance();
    }

    private _Angular2HtmlLexer getFlexLexer() {
      return (_Angular2HtmlLexer)((FlexAdapter)getOriginal()).getFlex();
    }

    private static IElementType merge(IElementType type, Lexer originalLexer) {
      final IElementType next = originalLexer.getTokenType();
      if (type == INTERPOLATION_START
          && next != INTERPOLATION_EXPR
          && next != INTERPOLATION_END) {
        type = next == XML_ATTRIBUTE_VALUE_TOKEN || next == XML_ATTRIBUTE_VALUE_END_DELIMITER
               ? XML_ATTRIBUTE_VALUE_TOKEN : XML_DATA_CHARACTERS;
      }
      if (!TOKENS_TO_MERGE.contains(type)) {
        return type;
      }
      while (true) {
        final IElementType tokenType = originalLexer.getTokenType();
        if (tokenType != type) {
          break;
        }
        originalLexer.advance();
      }
      return type;
    }
  }

  private static class MyLexerPosition implements LexerPosition {

    private final LexerPosition myOriginal;
    private final int myExpansionFormNestingLevel;

    private MyLexerPosition(@NotNull LexerPosition original, int expansionFormNestingLevel) {
      myOriginal = original;
      myExpansionFormNestingLevel = expansionFormNestingLevel;
    }

    public int getExpansionFormNestingLevel() {
      return myExpansionFormNestingLevel;
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
