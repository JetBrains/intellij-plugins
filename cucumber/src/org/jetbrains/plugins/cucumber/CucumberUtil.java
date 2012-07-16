package org.jetbrains.plugins.cucumber;

import org.apache.oro.text.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: Andrey.Vokin
 * Date: 7/11/12
 */
public class CucumberUtil {
  public static final char LEFT_PAR = '(';
  public static final char RIGHT_PAR = ')';
  public static final char LEFT_SQUARE_BRACE = '[';
  public static final char RIGHT_SQUARE_BRACE = ']';
  public static final char LEFT_BRACE = '{';
  public static final char RIGHT_BRACE = '}';


  public static final char ESCAPE_SLASH = '\\';
  public static final String PREFIX_CHAR = "^";
  public static final String SUFFIX_CHAR = "$";

  private static boolean isAlphabetCharOrSpace(char c) {
    return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || c == ' ';
  }

  /**
   * Approximately checks if cucumber pattern could be matched to list of word prepared from cucumber step call.
   * Method ignores parameters of cucumber step and takes into account only keywords.
   * @param pattern to check
   * @param wordList word from cucumber step call
   * @return true if pattern could by found by @param word
   */
  public static boolean matches(@Nullable Pattern pattern, @NotNull final String word) {
    if (pattern == null) {
      return false;
    }
    final String keyWord = getTheBiggestWordToSearchByIndex(pattern.getPattern());
    return keyWord.equals(word);
  }

  public static String getTheBiggestWordToSearchByIndex(String regexp) {
    String result = "";
    if (regexp.startsWith(PREFIX_CHAR)) {
      regexp = regexp.substring(1);
    }
    if (regexp.endsWith(SUFFIX_CHAR)) {
      regexp = regexp.substring(0, regexp.length() - 1);
    }

    int par = 0;
    int squareBrace = 0;
    int brace = 0;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < regexp.length(); i++) {
      char c = regexp.charAt(i);
      if (c == '#') {
        sb = new StringBuilder();
        continue;
      }
      if (c != ESCAPE_SLASH) {
        if (c == LEFT_PAR) {
          par++;
        }
        if (c == RIGHT_PAR) {
          if (par > 0) {
            par--;
          }
        }

        if (c == LEFT_BRACE) {
          brace++;
        }
        if (c == RIGHT_BRACE) {
          if (brace > 0) {
            brace--;
          }
        }

        if (c == LEFT_SQUARE_BRACE) {
          squareBrace++;
        }
        if (c == RIGHT_SQUARE_BRACE) {
          if (squareBrace > 0) {
            squareBrace--;
          }
        }
      } else {
        sb = new StringBuilder();
        i++;
      }
      if (par > 0 | squareBrace > 0 | brace > 0) {
        if (par + squareBrace + brace == 1) {
          // if it's first brace
          sb = new StringBuilder();
        }
        continue;
      }
      if (isAlphabetCharOrSpace(c)) {
        sb.append(c);
        if (sb.length() > 0) {
          if (sb.toString().length() > result.length()) {
            result = sb.toString();
          }
        }
      }
    }
    if (sb.length() > 0) {
      if (sb.toString().length() > result.length()) {
        result = sb.toString();
      }
    }
    return result;
  }


}
