package com.google.jstestdriver.idea.rt.execution.tc;

import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class TCMessage {

  private final StringBuilder myText = new StringBuilder("##teamcity[");
  private boolean myFinished = false;

  public TCMessage(@NotNull TCCommand command) {
    myText.append(command.getName());
  }

  public String getText() {
    if (!myFinished) {
      myFinished = true;
      myText.append(']');
    }
    return myText.toString();
  }

  public TCMessage addIntAttribute(@NotNull TCAttribute attribute, int value) {
    return addAttribute(attribute, String.valueOf(value));
  }

  public TCMessage addAttribute(@NotNull TCAttribute attribute, @NotNull String value) {
    if (myFinished) {
      throw new RuntimeException("Can't add attribute to finished message!");
    }
    myText.append(' ').append(attribute.getName()).append("='");
    myText.append(escapeStr(value));
    myText.append('\'');
    return this;
  }

  private static String escapeStr(@NotNull String str) {
    int escapedStringLength = calcEscapedStringLength(str);
    if (str.length() == escapedStringLength) return str;

    char[] resultChars = new char[escapedStringLength];
    int resultPos = 0;
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      final char escaped = escape(c);
      if (escaped != 0) {
        resultChars[resultPos++] = '|';
        resultChars[resultPos++] = escaped;
      }
      else {
        resultChars[resultPos++] = c;
      }
    }

    if (resultPos != escapedStringLength) {
      throw new RuntimeException("Incorrect escaping for '" + str + "'");
    }
    return new String(resultChars);
  }

  private static int calcEscapedStringLength(@NotNull String name) {
    int result = name.length();
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (escape(c) != 0) {
        result++;
      }
    }
    return result;
  }

  public static char escape(final char c) {
    switch (c) {
      case '\n': return 'n';
      case '\r': return 'r';
      case '\u0085': return 'x'; // next-line character
      case '\u2028': return 'l'; // line-separator character
      case '\u2029': return 'p'; // paragraph-separator character
      case '|': return '|';
      case '\'': return '\'';
      case '[': return '[';
      case ']': return ']';
      default:return 0;
    }
  }

}
