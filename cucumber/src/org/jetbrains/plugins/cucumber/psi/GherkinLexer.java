package org.jetbrains.plugins.cucumber.psi;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author yole
 */
public class GherkinLexer extends LexerBase {
  protected CharSequence myBuffer = ArrayUtil.EMPTY_CHAR_SEQUENCE;
  protected int myStartOffset = 0;
  protected int myEndOffset = 0;
  private int myPosition;
  private IElementType myCurrentToken;
  private int myCurrentTokenStart;
  private List<String> myKeywords;
  private int myState;

  private final static int STATE_DEFAULT = 0;
  private final static int STATE_AFTER_KEYWORD = 1;
  private final static int STATE_TABLE = 2;
  private final static int STATE_AFTER_KEYWORD_WITH_PARAMETER = 3;
  private final static int STATE_INSIDE_PYSTRING = 5;

  private final static int STATE_PARAMETER_INSIDE_PYSTRING = 6;
  private final static int STATE_PARAMETER_INSIDE_STEP = 7;

  private static final String PYSTRING_MARKER = "\"\"\"";
  private final GherkinKeywordProvider myKeywordProvider;
  private String myCurLanguage;

  public GherkinLexer(GherkinKeywordProvider provider) {
    myKeywordProvider = provider;
    updateLanguage("en");
  }

  private void updateLanguage(String language) {
    myCurLanguage = language;
    myKeywords = new ArrayList<>(myKeywordProvider.getAllKeywords(language));
    Collections.sort(myKeywords, (o1, o2) -> o2.length() - o1.length());
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

  public int getState() {
    return myState;
  }

  public IElementType getTokenType() {
    return myCurrentToken;
  }

  public int getTokenStart() {
    return myCurrentTokenStart;
  }

  public int getTokenEnd() {
    return myPosition;
  }

  private boolean isStepParameter(@NotNull final String currentElementTerminator) {
    int pos = myPosition;

    if (myBuffer.charAt(pos) == '<') {
      while (pos < myEndOffset && myBuffer.charAt(pos) != '\n' && myBuffer.charAt(pos) != '>' && !isStringAtPosition(currentElementTerminator, pos)) {
        pos++;
      }

      return pos < myEndOffset && myBuffer.charAt(pos) == '>';
    }

    return false;
  }

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
    else if (c == ':') {
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
            if (myKeywordProvider.isSpaceAfterKeyword(myCurLanguage, keyword) &&
                myEndOffset - myPosition > length &&
                Character.isLetterOrDigit(myBuffer.charAt(myPosition + length))) {
              continue;
            }
            myCurrentToken = myKeywordProvider.getTokenType(myCurLanguage, keyword);
            myPosition += length;
            if (myCurrentToken == GherkinTokenTypes.STEP_KEYWORD || myCurrentToken == GherkinTokenTypes.SCENARIO_OUTLINE_KEYWORD) {
              myState = STATE_AFTER_KEYWORD_WITH_PARAMETER;
            } else {
              myState = STATE_AFTER_KEYWORD;
            }

            return;
          }
        }
      }
      if (myState == STATE_PARAMETER_INSIDE_STEP) {
        if (c == '>') {
          myState = STATE_AFTER_KEYWORD_WITH_PARAMETER;
          myPosition++;
          myCurrentToken = GherkinTokenTypes.STEP_PARAMETER_BRACE;
        } else {
          advanceToParameterEnd("\n");
          myCurrentToken = GherkinTokenTypes.STEP_PARAMETER_TEXT;
        }
        return;
      } else if (myState == STATE_AFTER_KEYWORD_WITH_PARAMETER) {
        if (myPosition < myEndOffset && myBuffer.charAt(myPosition) == '<' && isStepParameter("\n")) {
          myState = STATE_PARAMETER_INSIDE_STEP;
          myPosition++;
          myCurrentToken = GherkinTokenTypes.STEP_PARAMETER_BRACE;
        } else {
          myCurrentToken = GherkinTokenTypes.TEXT;
          advanceToParameterOrSymbol("\n", STATE_AFTER_KEYWORD_WITH_PARAMETER, true);
        }
        return;
      }
      myCurrentToken = GherkinTokenTypes.TEXT;
      advanceToEOL();
    }
  }

  @Nullable
  public static String fetchLocationLanguage(final @NotNull String commentText) {
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

  @NotNull
  public CharSequence getBufferSequence() {
    return myBuffer;
  }

  public int getBufferEnd() {
    return myEndOffset;
  }
}
