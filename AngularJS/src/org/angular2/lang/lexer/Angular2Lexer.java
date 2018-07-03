// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angular2.lang.lexer;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static org.angular2.lang.lexer.Angular2TokenTypes.*;

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

  @NotNull
  @Override
  public LexerPosition getCurrentPosition() {
    return new MyLexerPosition(super.getCurrentPosition(), myMergeFunction.isPrevTokenEscapeSequence());
  }

  private static class MyLexerPosition implements LexerPosition {

    private final LexerPosition myOriginal;
    private final boolean myPrevTokenEscapeSequence;

    public MyLexerPosition(LexerPosition original, boolean prevTokenEscapeSequence) {
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

  private static class MyMergeFunction implements MergeFunction {

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
        myPrevTokenEscapeSequence = type == ESCAPE_SEQUENCE
                                    || type == INVALID_ESCAPE_SEQUENCE;
        return type;
      }
      while (true) {
        final IElementType tokenType = originalLexer.getTokenType();
        if (tokenType != STRING_LITERAL_PART) {
          if (myPrevTokenEscapeSequence
              || tokenType == ESCAPE_SEQUENCE
              || tokenType == INVALID_ESCAPE_SEQUENCE) {
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
