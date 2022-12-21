// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.lexer;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static org.angular2.lang.expr.lexer.Angular2TokenTypes.*;

public class Angular2Lexer extends MergingLexerAdapterBase {

  private MyMergeFunction myMergeFunction;

  public Angular2Lexer() {
    super(new FlexAdapter(new _Angular2Lexer(null)));
  }

  @Override
  public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    super.start(buffer, startOffset, endOffset, initialState);
    myMergeFunction = new MyMergeFunction(false);
  }

  @Override
  public MergeFunction getMergeFunction() {
    return myMergeFunction;
  }

  @Override
  public void restore(@NotNull LexerPosition position) {
    MyLexerPosition pos = (MyLexerPosition)position;
    myMergeFunction = new MyMergeFunction(pos.isPrevTokenEscapeSequence());
    super.restore(pos.getOriginal());
  }

  @Override
  public @NotNull LexerPosition getCurrentPosition() {
    return new MyLexerPosition(super.getCurrentPosition(), myMergeFunction.isPrevTokenEscapeSequence());
  }

  private static final class MyLexerPosition implements LexerPosition {

    private final LexerPosition myOriginal;
    private final boolean myPrevTokenEscapeSequence;

    private MyLexerPosition(LexerPosition original, boolean prevTokenEscapeSequence) {
      myOriginal = original;
      myPrevTokenEscapeSequence = prevTokenEscapeSequence;
    }

    public boolean isPrevTokenEscapeSequence() {
      return myPrevTokenEscapeSequence;
    }

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

  private static final class MyMergeFunction implements MergeFunction {

    private boolean myPrevTokenEscapeSequence;

    private MyMergeFunction(boolean prevTokenEscapeSequence) {
      this.myPrevTokenEscapeSequence = prevTokenEscapeSequence;
    }

    public boolean isPrevTokenEscapeSequence() {
      return myPrevTokenEscapeSequence;
    }

    @Override
    public IElementType merge(IElementType type, Lexer originalLexer) {
      if (type != STRING_LITERAL_PART) {
        myPrevTokenEscapeSequence = STRING_PART_SPECIAL_SEQ.contains(type);
        return type;
      }
      while (true) {
        final IElementType tokenType = originalLexer.getTokenType();
        if (tokenType != STRING_LITERAL_PART) {
          if (myPrevTokenEscapeSequence
              || STRING_PART_SPECIAL_SEQ.contains(tokenType)) {
            myPrevTokenEscapeSequence = false;
            return STRING_LITERAL_PART;
          }
          else {
            return STRING_LITERAL;
          }
        }
        originalLexer.advance();
      }
    }
  }
}
