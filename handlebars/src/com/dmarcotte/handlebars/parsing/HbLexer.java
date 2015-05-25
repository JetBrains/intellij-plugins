package com.dmarcotte.handlebars.parsing;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergeFunction;
import com.intellij.lexer.MergingLexerAdapterBase;
import com.intellij.psi.tree.IElementType;

public class HbLexer extends MergingLexerAdapterBase {
  private static final MergeFunction MERGE_FUNCTION = new MergeFunction() {
    @Override
    public IElementType merge(final IElementType type, final Lexer originalLexer) {
      if (HbTokenTypes.COMMENT_OPEN != type) {
        return type;
      }

      if (originalLexer.getTokenType() == HbTokenTypes.COMMENT_CONTENT) {
        originalLexer.advance();
      }

      if (originalLexer.getTokenType() == HbTokenTypes.COMMENT_CLOSE) {
        originalLexer.advance();
        return HbTokenTypes.COMMENT;
      }

      if (originalLexer.getTokenType() == null) {
        return HbTokenTypes.UNCLOSED_COMMENT;
      }


      if (originalLexer.getTokenType() == HbTokenTypes.UNCLOSED_COMMENT) {
        originalLexer.advance();
        return HbTokenTypes.UNCLOSED_COMMENT;
      }

      return type;
    }
  };

  public HbLexer() {
    super(new HbRawLexer());
  }

  @Override
  public MergeFunction getMergeFunction() {
    return MERGE_FUNCTION;
  }
}