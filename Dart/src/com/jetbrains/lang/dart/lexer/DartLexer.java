package com.jetbrains.lang.dart.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergingLexerAdapterBase;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.DartElementType;

import static com.jetbrains.lang.dart.DartTokenTypes.REGULAR_STRING_PART;
import static com.jetbrains.lang.dart.DartTokenTypesSets.MULTI_LINE_COMMENT;
import static com.jetbrains.lang.dart.DartTokenTypesSets.MULTI_LINE_DOC_COMMENT;
import static com.jetbrains.lang.dart.DartTokenTypesSets.WHITE_SPACE;

public class DartLexer extends MergingLexerAdapterBase {

  // temporary elements, finally replaced with DartTokenTypesSets#MULTI_LINE_COMMENT or DartTokenTypesSets#MULTI_LINE_DOC_COMMENT
  static final IElementType MULTI_LINE_COMMENT_START = new DartElementType("MULTI_LINE_COMMENT_START");
  static final IElementType MULTI_LINE_DOC_COMMENT_START = new DartElementType("MULTI_LINE_DOC_COMMENT_START");
  static final IElementType MULTI_LINE_COMMENT_BODY = new DartElementType("MULTI_LINE_COMMENT_BODY");
  static final IElementType MULTI_LINE_COMMENT_END = new DartElementType("MULTI_LINE_COMMENT_END");

  public DartLexer() {
    super(createLexer(), MERGE_FUNCTION);
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
   * collapses sequence like <code>{MULTI_LINE_COMMENT_START MULTI_LINE_COMMENT_BODY* MULTI_LINE_COMMENT_END}</code> into a single <code>DartTokenTypesSets.MULTI_LINE_COMMENT</code>
   */
  private static final MergingLexerAdapterBase.MergeFunction MERGE_FUNCTION = new MergingLexerAdapterBase.MergeFunction() {
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
}
