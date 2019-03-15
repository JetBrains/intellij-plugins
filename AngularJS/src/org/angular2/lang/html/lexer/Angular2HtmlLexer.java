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

  @Override
  protected boolean isHtmlTagState(int state) {
    return state == _Angular2HtmlLexer.START_TAG_NAME || state == _Angular2HtmlLexer.END_TAG_NAME;
  }

  @Override
  public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    super.start(buffer, startOffset, endOffset,
                ((Angular2HtmlMergingLexer)getDelegate()).initExpansionFormNestingLevel(initialState));
  }

  @Override
  public int getState() {
    return super.getState() | ((Angular2HtmlMergingLexer)getDelegate()).getExpansionFormNestingLevelState();
  }

  public static class Angular2HtmlMergingLexer extends MergingLexerAdapterBase {

    public static boolean isLexerWithinInterpolation(int state) {
      state &= BASE_STATE_MASK;
      return state == _Angular2HtmlLexer.INTERPOLATION;
    }

    public static boolean isLexerWithinUnterminatedInterpolation(int state) {
      state &= BASE_STATE_MASK;
      return state == _Angular2HtmlLexer.UNTERMINATED_INTERPOLATION;
    }

    public static boolean isLexerWithinExpansionForm(int state) {
      state &= BASE_STATE_MASK;
      return state == _Angular2HtmlLexer.EXPANSION_FORM_CONTENT
             || state == _Angular2HtmlLexer.EXPANSION_FORM_CASE_END;
    }

    public static int getBaseLexerState(int state) {
      return state & BASE_STATE_MASK;
    }

    private static final int EXPANSION_LEVEL_STATE_SHIFT = 28;
    private static final int EXPANSION_LEVEL_STATE_MASK = 0xf << EXPANSION_LEVEL_STATE_SHIFT;

    private int myExpansionFormNestingLevelCur;
    private int myExpansionFormNestingLevelNext;

    public Angular2HtmlMergingLexer(@NotNull FlexAdapter original) {
      super(original);
    }

    public int initExpansionFormNestingLevel(int initialState) {
      int levelState = (initialState & EXPANSION_LEVEL_STATE_MASK) >> EXPANSION_LEVEL_STATE_SHIFT;
      myExpansionFormNestingLevelCur = (levelState >> 2) & 0x3;
      myExpansionFormNestingLevelNext = levelState & 0x3;
      getFlexLexer().setExpansionFormNestingLevel(myExpansionFormNestingLevelCur);
      return initialState & ~EXPANSION_LEVEL_STATE_MASK;
    }


    public int getExpansionFormNestingLevelState() {
      return (((myExpansionFormNestingLevelCur & 0x3) << 2) + (getExpansionFormNestingLevelNext() & 0x3)) << EXPANSION_LEVEL_STATE_SHIFT;
    }

    private int getExpansionFormNestingLevelNext() {
      return myExpansionFormNestingLevelNext >= 0 ? myExpansionFormNestingLevelNext
                                                  : getFlexLexer().getExpansionFormNestingLevel();
    }

    @Override
    public MergeFunction getMergeFunction() {
      return this::merge;
    }

    @Override
    public void advance() {
      myExpansionFormNestingLevelCur = getExpansionFormNestingLevelNext();
      super.advance();
      if (myExpansionFormNestingLevelNext >= 0) {
        getFlexLexer().setExpansionFormNestingLevel(myExpansionFormNestingLevelNext);
        myExpansionFormNestingLevelNext = -1;
      }
    }

    private _Angular2HtmlLexer getFlexLexer() {
      return (_Angular2HtmlLexer)((FlexAdapter)getOriginal()).getFlex();
    }

    protected IElementType merge(IElementType type, Lexer originalLexer) {
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
}
