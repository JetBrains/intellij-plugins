/*
 * Copyright © 2022 Yuriy Artamonov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the “Software”), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.intellij.tsr;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import com.intellij.tsr.psi.TslTokenTypes;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

final class TslSyntaxHighlighter extends SyntaxHighlighterBase {
  public static final TextAttributesKey TSL_KEYWORD =
      createTextAttributesKey("TSL.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);

  public static final TextAttributesKey TSL_CLASSNAME =
      createTextAttributesKey("TSL.CLASSNAME", DefaultLanguageHighlighterColors.CLASS_NAME);

  public static final TextAttributesKey TSL_NUMBER =
      createTextAttributesKey("TSL.NUMBER", DefaultLanguageHighlighterColors.NUMBER);

  public static final TextAttributesKey TSL_BOOLEAN =
      createTextAttributesKey("TSL.BOOLEAN", DefaultLanguageHighlighterColors.NUMBER);

  public static final TextAttributesKey TSL_STRING =
      createTextAttributesKey("TSL.STRING", DefaultLanguageHighlighterColors.STRING);

  public static final TextAttributesKey TSL_PARENTHESES =
      createTextAttributesKey("TSL.PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);

  public static final TextAttributesKey TSL_BRACKETS =
      createTextAttributesKey("TSL.BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);

  public static final TextAttributesKey TSL_BRACES =
      createTextAttributesKey("TSL.BRACES", DefaultLanguageHighlighterColors.BRACES);

  public static final TextAttributesKey TSL_COMMA =
      createTextAttributesKey("TSL.COMMA", DefaultLanguageHighlighterColors.COMMA);

  public static final TextAttributesKey TSL_FIELD_NAME =
      createTextAttributesKey("TSL.FIELD_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

  public static final TextAttributesKey TSL_CONSTANT =
      createTextAttributesKey("TSL.CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);

  public static final TextAttributesKey TSL_HASHCODE =
      createTextAttributesKey("TSL.HASHCODE", DefaultLanguageHighlighterColors.METADATA);

  private static final Map<IElementType, TextAttributesKey> ourMap;

  static {
    ourMap = new HashMap<>();

    fillMap(ourMap, TSL_KEYWORD, TslTokenTypes.NULL);
    fillMap(ourMap, TSL_BOOLEAN, TslTokenTypes.TRUE, TslTokenTypes.FALSE);
    fillMap(ourMap, TSL_NUMBER, TslTokenTypes.NUMBER);
    fillMap(ourMap, TSL_STRING, TslTokenTypes.DOUBLE_QUOTED_STRING, TslTokenTypes.SINGLE_QUOTED_STRING);

    fillMap(ourMap, TSL_BRACKETS, TslTokenTypes.LBRACKET, TslTokenTypes.RBRACKET);
    fillMap(ourMap, TSL_BRACES, TslTokenTypes.LBRACE, TslTokenTypes.RBRACE);
    fillMap(ourMap, TSL_PARENTHESES, TslTokenTypes.LPARENTH, TslTokenTypes.RPARENTH);
    fillMap(ourMap, TSL_COMMA, TslTokenTypes.COMMA);
    fillMap(ourMap, TSL_HASHCODE, TslTokenTypes.STRUDEL_HEX);
  }

  @Override
  public @NotNull Lexer getHighlightingLexer() {
    return new TslLexer();
  }

  @Override
  public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
    return pack(ourMap.get(tokenType));
  }
}
