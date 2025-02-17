package com.jetbrains.plugins.jade.js;

import com.intellij.lang.javascript.JSFlexAdapter;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Range;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaScriptInJadeLexer extends LayeredLexer {
  public JavaScriptInJadeLexer() {
    super(new JSFlexAdapter(JavaScriptInJadeLanguageDialect.DIALECT_OPTION_HOLDER));

    // Reparse string literals to support interpolations
    registerLayer(new JavaScriptInJadeStringLexer(), JSTokenTypes.STRING_LITERAL);
  }

  /**
   * This lexer is to parse interpolation strings such as "text#{var + 1*2}text"
   * Simply delegates parsing of the expressions to a standard JS lexer (since 'nested' interpolations are not allowed)
   * State = 0                    if just in string
   *         1 + delegate->state  if parsing expression
   */
  private static class JavaScriptInJadeStringLexer extends LexerBase {
    private final JSFlexAdapter myExpressionLexer = new JSFlexAdapter(JavaScriptInJadeLanguageDialect.DIALECT_OPTION_HOLDER);

    private CharSequence myBuffer;
    private int myStartOffset;
    private int myEndOffset;

    private int myState;

    private int myStringPartStart;

    private Range<Integer> myNextInterpolatedPart;


    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
      myBuffer = buffer;
      myStartOffset = startOffset;
      myEndOffset = endOffset;
      myState = initialState;

      if (myState > 0) {
        myExpressionLexer.start(buffer, startOffset, endOffset, myState - 1);
      }

      myStringPartStart = 0;
      findNextInterpolatedPart();
    }

    @Override
    public int getState() {
      return myState;
    }

    @Override
    public @Nullable IElementType getTokenType() {
      if (myStringPartStart == -1) {
        return null;
      }

      return myState == 0 ? getStringElementType() : myExpressionLexer.getTokenType();
    }

    @Override
    public int getTokenStart() {
      return myState == 0 ? myStartOffset + myStringPartStart : myExpressionLexer.getTokenStart();
    }

    @Override
    public int getTokenEnd() {
      if (myState == 0) {
        return myNextInterpolatedPart == null ? myEndOffset : myNextInterpolatedPart.getFrom();
      }
      else {
        return myExpressionLexer.getTokenEnd();
      }
    }

    @Override
    public void advance() {
      if (myState == 0) {
        if (myNextInterpolatedPart == null) {
          myStringPartStart = -1;
        }
        else {
          myExpressionLexer.start(myBuffer, myNextInterpolatedPart.getFrom(), myNextInterpolatedPart.getTo(), 0);
          myState = 1 + myExpressionLexer.getState();

          myStringPartStart = myNextInterpolatedPart.getTo() - myStartOffset;
          findNextInterpolatedPart();
        }
      }
      else {
        myExpressionLexer.advance();
        if (myExpressionLexer.getTokenType() == null) {
          myState = 0;
        }
      }
    }

    @Override
    public @NotNull CharSequence getBufferSequence() {
      return myBuffer;
    }

    @Override
    public int getBufferEnd() {
      return myEndOffset;
    }

    private void findNextInterpolatedPart() {
      final int absolutePosStart = myStartOffset + myStringPartStart;
      int interpolationStart = findInterpolationStartPos(absolutePosStart);
      if (interpolationStart == -1) {
        myNextInterpolatedPart = null;
        return;
      }
      interpolationStart += 2;

      final int absolutePosEnd = interpolationStart + 1;
      int interpolationEnd = findClosingBraceWithRespectToOpeningOnes(absolutePosEnd);

      if (interpolationEnd == -1) {
        myNextInterpolatedPart = null;
      }
      else {
        myNextInterpolatedPart = new Range<>(interpolationStart, interpolationEnd);
      }
    }

    private int findInterpolationStartPos(int absolutePosStart) {
      for (int i = absolutePosStart; i + 2 < myEndOffset; ++i) {
        if (myBuffer.charAt(i) == '#'
          && myBuffer.charAt(i + 1) == '{'
          && (i == absolutePosStart || myBuffer.charAt(i - 1) != '\\')) {
          return i;
        }
      }
      return -1;
    }

    private int findClosingBraceWithRespectToOpeningOnes(int startPos) {
      int balance = 1;
      int pos = startPos;
      while (balance > 0 && pos < myEndOffset) {
        char c = myBuffer.charAt(pos++);

        if (c == '{') {
          balance++;
        }
        else if (c == '}') {
          balance--;
        }
      }

      if (balance == 0) {
        return pos - 1;
      }
      else {
        return -1;
      }
    }

    private IElementType getStringElementType() {
      if (myStringPartStart == 0 && myNextInterpolatedPart == null) {
        return JSTokenTypes.STRING_LITERAL;
      }
      else if (myStringPartStart == 0) {
        return JadeTokenTypes.INTERPOLATED_STRING_START;
      }
      else if (myNextInterpolatedPart == null) {
        return JadeTokenTypes.INTERPOLATED_STRING_END;
      }
      else {
        return JadeTokenTypes.INTERPOLATED_STRING_PART;
      }
    }
  }
}
