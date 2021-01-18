/*
 * Copyright 2015 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.lang.ognl.highlight;

import com.intellij.ide.highlighter.EmbeddedTokenHighlighter;
import com.intellij.lang.ognl.OgnlTypes;
import com.intellij.lang.ognl.psi.OgnlTokenGroups;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides basic syntax highlighting.
 *
 * @author Yann C&eacute;bron
 */
public final class OgnlHighlighter extends SyntaxHighlighterBase implements EmbeddedTokenHighlighter {
  private static final Map<IElementType, TextAttributesKey> keys1;

  @NotNull
  @Override
  public Lexer getHighlightingLexer() {
    return new OgnlHighlightingLexer();
  }

  @Override
  public TextAttributesKey @NotNull [] getTokenHighlights(final IElementType iElementType) {
    return pack(BACKGROUND, keys1.get(iElementType));
  }

  public static final TextAttributesKey BACKGROUND = TextAttributesKey.createTextAttributesKey(
    "OGNL.BACKGROUND", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR);

  public static final TextAttributesKey EXPRESSION_BOUNDS = TextAttributesKey.createTextAttributesKey(
    "OGNL.EXPRESSION", DefaultLanguageHighlighterColors.KEYWORD);

  public static final TextAttributesKey BAD_CHARACTER = TextAttributesKey.createTextAttributesKey(
    "OGNL.BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

  public static final TextAttributesKey IDENTIFIER = TextAttributesKey.createTextAttributesKey(
    "OGNL.IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);

  public static final TextAttributesKey KEYWORDS = TextAttributesKey.createTextAttributesKey(
    "OGNL.KEYWORDS", DefaultLanguageHighlighterColors.KEYWORD);

  public static final TextAttributesKey OPERATIONS = TextAttributesKey.createTextAttributesKey(
    "OGNL.OPERATIONS", DefaultLanguageHighlighterColors.OPERATION_SIGN);

  public static final TextAttributesKey NUMBER = TextAttributesKey.createTextAttributesKey(
    "OGNL.NUMBER", DefaultLanguageHighlighterColors.NUMBER);

  public static final TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey(
    "OGNL.STRING", DefaultLanguageHighlighterColors.STRING);

  public static final TextAttributesKey COMMA = TextAttributesKey.createTextAttributesKey(
    "OGNL.COMMA", DefaultLanguageHighlighterColors.COMMA);

  public static final TextAttributesKey BRACKETS = TextAttributesKey.createTextAttributesKey(
    "OGNL.BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);

  public static final TextAttributesKey PARENTHESES = TextAttributesKey.createTextAttributesKey(
    "OGNL.PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);

  public static final TextAttributesKey BRACES = TextAttributesKey.createTextAttributesKey(
    "OGNL.BRACES", DefaultLanguageHighlighterColors.BRACES);

  public static final TextAttributesKey FQN_TYPE = TextAttributesKey.createTextAttributesKey(
    "OGNL.FQN_TYPE", DefaultLanguageHighlighterColors.CLASS_REFERENCE);

  static {
    keys1 = new HashMap<>();

    // single characters
    keys1.put(TokenType.BAD_CHARACTER, BAD_CHARACTER);

    keys1.put(OgnlTypes.COMMA, COMMA);

    // EXPR_HOLDER
    keys1.put(OgnlTypes.EXPRESSION_START, EXPRESSION_BOUNDS);
    keys1.put(OgnlTypes.EXPRESSION_END, EXPRESSION_BOUNDS);

    keys1.put(OgnlTypes.IDENTIFIER, IDENTIFIER);

    SyntaxHighlighterBase.fillMap(keys1, OgnlTokenGroups.KEYWORDS, KEYWORDS);
    SyntaxHighlighterBase.fillMap(keys1, OgnlTokenGroups.OPERATION_KEYWORDS, KEYWORDS);

    SyntaxHighlighterBase.fillMap(keys1, OgnlTokenGroups.OPERATION_SIGNS, OPERATIONS);
    keys1.put(OgnlTypes.DOLLAR, OPERATIONS);
    keys1.put(OgnlTypes.QUESTION, OPERATIONS);

    // literals
    SyntaxHighlighterBase.fillMap(keys1, OgnlTokenGroups.NUMBERS, NUMBER);
    SyntaxHighlighterBase.fillMap(keys1, OgnlTokenGroups.TEXT, STRING);

    // string/character escape sequences
    keys1.put(StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    keys1.put(StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
    keys1.put(StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN, DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);

    // braces
    keys1.put(OgnlTypes.LBRACKET, BRACKETS);
    keys1.put(OgnlTypes.RBRACKET, BRACKETS);

    keys1.put(OgnlTypes.LPARENTH, PARENTHESES);
    keys1.put(OgnlTypes.RPARENTH, PARENTHESES);

    keys1.put(OgnlTypes.LBRACE, BRACES);
    keys1.put(OgnlTypes.RBRACE, BRACES);
  }

  @NotNull
  @Override
  public MultiMap<IElementType, TextAttributesKey> getEmbeddedTokenAttributes() {
    MultiMap<IElementType, TextAttributesKey> map = MultiMap.create();
    map.putAllValues(keys1);
    return map;
  }

}