package com.jetbrains.lang.dart.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergeFunction;
import com.intellij.lexer.MergingLexerAdapterBase;
import com.intellij.psi.tree.IElementType;

import static com.jetbrains.lang.dart.DartTokenTypes.REGULAR_STRING_PART;
import static com.jetbrains.lang.dart.DartTokenTypesSets.*;

public class DartLexer extends MergingLexerAdapterBase {

  public DartLexer() {
    super(createLexer());
  }

  private static FlexAdapter createLexer() {
    return new FlexAdapter(new _DartLexer() {
      public void reset(final CharSequence buffer, final int start, final int end, final int initialState) {
        super.reset(buffer, start, end, initialState);
        myLeftBraceCount = 0;
        myStateStack.clear();
      }
    });
  }

  /**
   * Merges WHITE_SPACE and REGULAR_STRING_PART tokens,
   * collapses sequence like <code>{MULTI_LINE_(DOC_)COMMENT_START MULTI_LINE_COMMENT_BODY* MULTI_LINE_COMMENT_END}</code> into a single <code>DartTokenTypesSets.MULTI_LINE_(DOC_)COMMENT</code>.
   * Doc comment content is lazily parsed separately
   */
  private static final MergeFunction MERGE_FUNCTION = new MergeFunction() {
    public IElementType merge(final IElementType firstTokenType, final Lexer originalLexer) {
      if (firstTokenType == REGULAR_STRING_PART || firstTokenType == WHITE_SPACE) {
        // merge consequent tokens of the same type
        while (true) {
          final IElementType nextTokenType = originalLexer.getTokenType();
          if (nextTokenType != firstTokenType) break;
          originalLexer.advance();
        }
        return firstTokenType;
      }
      else if (firstTokenType == MULTI_LINE_COMMENT_START || firstTokenType == MULTI_LINE_DOC_COMMENT_START) {
        // merge multiline comments that are parsed in parts into single element
        while (true) {
          final IElementType nextTokenType = originalLexer.getTokenType();
          if (nextTokenType == null) break; // EOF reached, multi-line comment is not closed

          originalLexer.advance();
          if (nextTokenType == MULTI_LINE_COMMENT_END) break;

          assert nextTokenType == MULTI_LINE_COMMENT_BODY : nextTokenType;
        }

        return firstTokenType == MULTI_LINE_DOC_COMMENT_START ? MULTI_LINE_DOC_COMMENT
                                                              : MULTI_LINE_COMMENT;
      }

      return firstTokenType;
    }
  };

  @Override
  public MergeFunction defineMergeFunction() {
    return MERGE_FUNCTION;
  }
}
