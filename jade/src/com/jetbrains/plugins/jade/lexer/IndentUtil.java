// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.lexer;

public final class IndentUtil {
  public static int calcIndent(CharSequence text, int position, int tabSize) {
    int result = 0;

    while (position < text.length()) {
      char c = text.charAt(position);
      if (!Character.isWhitespace(c)) {
        break;
      }
      if (c == '\n') {
        // whitespace-only line
        result = 0;
      }
      else if (c == '\t') {
        result += tabSize;
      }
      else {
        result++;
      }
      position++;
    }
    return result;
  }

  /**
   * @return -1 if there's no second line
   */
  public static int calcSecondLineIndent(String text, int tabSize) {
    int i = text.indexOf('\n');
    if (i == -1) {
      return -1;
    }

    return calcIndent(text, i + 1, tabSize);
  }
}
