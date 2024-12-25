// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lexer.LexerBase;
import com.intellij.openapi.util.text.Strings;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class GherkinLexer extends LexerBase {
  protected CharSequence myBuffer = Strings.EMPTY_CHAR_SEQUENCE;
  protected int myStartOffset = 0;
  protected int myEndOffset = 0;
  private int myPosition;
  private IElementType myCurrentToken;
  private int myCurrentTokenStart;
  private List<String> myKeywords;
  private int myState;

  private static final int STATE_DEFAULT = 0;
  private static final int STATE_AFTER_KEYWORD = 1;
  private static final int STATE_TABLE = 2;
  private static final int STATE_AFTER_STEP_KEYWORD = 3;
  private static final int STATE_AFTER_SCENARIO_KEYWORD = 4;
  private static final int STATE_INSIDE_PYSTRING = 5;

  private static final int STATE_PARAMETER_INSIDE_PYSTRING = 6;
  private static final int STATE_PARAMETER_INSIDE_STEP = 7;

  public static final String PYSTRING_MARKER = "\"\"\"";
  private final GherkinKeywordProvider myKeywordProvider;
  private String myCurLanguage;

  public GherkinLexer(GherkinKeywordProvider provider) {
    myKeywordProvider = provider;
    updateLanguage("en");
  }

  private void updateLanguage(String language) {
    myCurLanguage = language;
    myKeywords = new ArrayList<>(myKeywordProvider.getAllKeywords(language));
    myKeywords.sort((o1, o2) -> o2.length() - o1.length());
  }

  @Override
  public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
    myBuffer = buffer;
    myStartOffset = startOffset;
    myEndOffset = endOffset;
    myPosition = startOffset;
    myState = initialState;
    advance();
  }

  @Override
  public int getState() {
    return myState;
  }

  @Override
  public IElementType getTokenType() {
    return myCurrentToken;
  }

  @Override
  public int getTokenStart() {
    return myCurrentTokenStart;
  }

  @Override
  public int getTokenEnd() {
    return myPosition;
  }

  private boolean isStepParameter(final @NotNull String currentElementTerminator) {
    int pos = myPosition;

    if (myBuffer.charAt(pos) == '<') {
      while (pos < myEndOffset && myBuffer.charAt(pos) != '\n' && myBuffer.charAt(pos) != '>' && !isStringAtPosition(currentElementTerminator, pos)) {
        pos++;
      }

      return pos < myEndOffset && myBuffer.charAt(pos) == '>';
    }

    return false;
  }

  @Override
  public void advance() {
    if (myPosition >= myEndOffset) {
      myCurrentToken = null;
      return;
    }
    myCurrentTokenStart = myPosition;
    char c = myBuffer.charAt(myPosition);
    if (myState != STATE_INSIDE_PYSTRING && Character.isWhitespace(c)) {
      advanceOverWhitespace();
      myCurrentToken = TokenType.WHITE_SPACE;
      while (myPosition < myEndOffset && Character.isWhitespace(myBuffer.charAt(myPosition))) {
        advanceOverWhitespace();
      }
    } else if (c == '|' && myState != STATE_INSIDE_PYSTRING) {
      myCurrentToken = GherkinTokenTypes.PIPE;
      myPosition++;
      myState = STATE_TABLE;
    } else if (myState == STATE_PARAMETER_INSIDE_PYSTRING) {
      if (c == '>') {
        myState = STATE_INSIDE_PYSTRING;
        myPosition++;
        myCurrentToken = GherkinTokenTypes.STEP_PARAMETER_BRACE;
      } else {
        advanceToParameterEnd(PYSTRING_MARKER);
        myCurrentToken = GherkinTokenTypes.STEP_PARAMETER_TEXT;
      }
    } else if (myState == STATE_INSIDE_PYSTRING) {
      if (isStringAtPosition(PYSTRING_MARKER)) {
        myPosition += 3 /* marker length */;
        myCurrentToken = GherkinTokenTypes.PYSTRING;
        myState = STATE_DEFAULT;
      } else {
        if (myBuffer.charAt(myPosition) == '<') {
          if (isStepParameter(PYSTRING_MARKER)) {
            myPosition++;
            myState = STATE_PARAMETER_INSIDE_PYSTRING;
            myCurrentToken = GherkinTokenTypes.STEP_PARAMETER_BRACE;
          } else {
            myPosition++;
            advanceToParameterOrSymbol(PYSTRING_MARKER, STATE_INSIDE_PYSTRING, false);
            myCurrentToken = GherkinTokenTypes.PYSTRING_TEXT;
          }
        } else {
          advanceToParameterOrSymbol(PYSTRING_MARKER, STATE_INSIDE_PYSTRING, false);
          myCurrentToken = GherkinTokenTypes.PYSTRING_TEXT;
        }
      }
    } else if (myState == STATE_TABLE) {
      myCurrentToken = GherkinTokenTypes.TABLE_CELL;
      while (myPosition < myEndOffset) {
        // Cucumber: 0.7.3 Table cells can now contain escaped bars - \| and escaped backslashes - \\
        if (myBuffer.charAt(myPosition) == '\\') {
          final int nextPos = myPosition + 1;
          if (nextPos < myEndOffset) {
            final char nextChar = myBuffer.charAt(nextPos);
            if (nextChar == '|' || nextChar == '\\') {
              myPosition += 2;
              continue;
            }
            // else - common case
          }
        }
        else if (myBuffer.charAt(myPosition) == '|' || myBuffer.charAt(myPosition) == '\n') {
          break;
        }
        myPosition++;
      }
      while(myPosition > 0 && Character.isWhitespace(myBuffer.charAt(myPosition - 1))) {
        myPosition--;
      }
    }
    else if (c == '#') {
      myCurrentToken = GherkinTokenTypes.COMMENT;
      advanceToEOL();

      String commentText = myBuffer.subSequence(myCurrentTokenStart+1, myPosition).toString().trim();
      final String language = fetchLocationLanguage(commentText);
      if (language != null) {
        updateLanguage(language);
      }
    }
    else if (c == ':' && myState != STATE_AFTER_STEP_KEYWORD) {
      myCurrentToken = GherkinTokenTypes.COLON;
      myPosition++;
    }
    else if (c == '@') {
      myCurrentToken = GherkinTokenTypes.TAG;
      myPosition++;
      while (myPosition < myEndOffset && isValidTagChar(myBuffer.charAt(myPosition))) {
        myPosition++;
      }
    }
    else if (isStringAtPosition(PYSTRING_MARKER)) {
      myCurrentToken = GherkinTokenTypes.PYSTRING;
      myState = STATE_INSIDE_PYSTRING;
      myPosition += 3;
    }
    else {
      if (myState == STATE_DEFAULT) {
        for (String keyword : myKeywords) {
          int length = keyword.length();
          if (isStringAtPosition(keyword)) {
            if (myKeywordProvider.isSpaceRequiredAfterKeyword(myCurLanguage, keyword) &&
                myEndOffset - myPosition > length &&
                Character.isLetterOrDigit(myBuffer.charAt(myPosition + length))) {
              continue;
            }

            char followedByChar = myPosition + length < myEndOffset ? myBuffer.charAt(myPosition + length) : 0;
            myCurrentToken = myKeywordProvider.getTokenType(myCurLanguage, keyword);
            if (myCurrentToken == GherkinTokenTypes.STEP_KEYWORD) {
              boolean followedByWhitespace = Character.isWhitespace(followedByChar) && followedByChar != '\n';
              if (followedByWhitespace != myKeywordProvider.isSpaceRequiredAfterKeyword(myCurLanguage, keyword)) {
                myCurrentToken = GherkinTokenTypes.TEXT;
              }
            }
            myPosition += length;
            if (myCurrentToken == GherkinTokenTypes.STEP_KEYWORD) {
              myState = STATE_AFTER_STEP_KEYWORD;
            } else if (myCurrentToken == GherkinTokenTypes.SCENARIO_OUTLINE_KEYWORD) {
              myState = STATE_AFTER_SCENARIO_KEYWORD;
            } else {
              myState = STATE_AFTER_KEYWORD;
            }

            return;
          }
        }
      }
      if (myState == STATE_PARAMETER_INSIDE_STEP) {
        if (c == '>') {
          myState = STATE_AFTER_STEP_KEYWORD;
          myPosition++;
          myCurrentToken = GherkinTokenTypes.STEP_PARAMETER_BRACE;
        } else {
          advanceToParameterEnd("\n");
          myCurrentToken = GherkinTokenTypes.STEP_PARAMETER_TEXT;
        }
        return;
      } else if (isParameterAllowed()) {
        if (myPosition < myEndOffset && myBuffer.charAt(myPosition) == '<' && isStepParameter("\n")) {
          myState = STATE_PARAMETER_INSIDE_STEP;
          myPosition++;
          myCurrentToken = GherkinTokenTypes.STEP_PARAMETER_BRACE;
        } else {
          myCurrentToken = GherkinTokenTypes.TEXT;
          advanceToParameterOrSymbol("\n", STATE_AFTER_STEP_KEYWORD, true);
        }
        return;
      }
      myCurrentToken = GherkinTokenTypes.TEXT;
      advanceToEOL();
    }
  }

  protected boolean isParameterAllowed() {
    return myState == STATE_AFTER_STEP_KEYWORD || myState == STATE_AFTER_SCENARIO_KEYWORD;
  }

  public static @Nullable String fetchLocationLanguage(final @NotNull String commentText) {
    if (commentText.startsWith("language:")) {
      return commentText.substring(9).trim();
    }
    return null;
  }

  private void advanceOverWhitespace() {
    if (myBuffer.charAt(myPosition) == '\n') {
      myState = STATE_DEFAULT;
    }
    myPosition++;
  }

  private boolean isStringAtPosition(String keyword) {
    int length = keyword.length();
    return myEndOffset - myPosition >= length && myBuffer.subSequence(myPosition, myPosition + length).toString().equals(keyword);
  }

  private boolean isStringAtPosition(String keyword, int position) {
    int length = keyword.length();
    return myEndOffset - position >= length && myBuffer.subSequence(position, position + length).toString().equals(keyword);
  }


  private static boolean isValidTagChar(char c) {
    return !Character.isWhitespace(c) && c != '@';
  }

  private void advanceToEOL() {
    myPosition++;
    int mark = myPosition;
    while (myPosition < myEndOffset && myBuffer.charAt(myPosition) != '\n') {
      myPosition++;
    }
    returnWhitespace(mark);
    myState = STATE_DEFAULT;
  }

  private void returnWhitespace(int mark) {
    while(myPosition > mark && Character.isWhitespace(myBuffer.charAt(myPosition - 1))) {
      myPosition--;
    }
  }

  private void advanceToParameterOrSymbol(String s, int parameterState, boolean shouldReturnWhitespace) {
    int mark = myPosition;

    while (myPosition < myEndOffset && !isStringAtPosition(s) && !isStepParameter(s)) {
      myPosition++;
    }

    if (shouldReturnWhitespace) {
      myState = STATE_DEFAULT;
      if (myPosition < myEndOffset) {
        if (!isStringAtPosition(s)) {
          myState = parameterState;
        }
      }

      returnWhitespace(mark);
    }
  }

  private void advanceToParameterEnd(String endSymbol) {
    myPosition++;
    int mark = myPosition;
    while (myPosition < myEndOffset && !isStringAtPosition(endSymbol) && myBuffer.charAt(myPosition) != '>') {
      myPosition++;
    }

    if (myPosition < myEndOffset) {
      if (isStringAtPosition(endSymbol)) {
        myState = STATE_DEFAULT;
      }
    }

    returnWhitespace(mark);
  }

  @Override
  public @NotNull CharSequence getBufferSequence() {
    return myBuffer;
  }

  @Override
  public int getBufferEnd() {
    return myEndOffset;
  }
}
