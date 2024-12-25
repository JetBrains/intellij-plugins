/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.protobuf.lang.psi.ProtoTokenTypes;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/** The syntax highlighter used for standalone prototext files. */
public class PbTextSyntaxHighlighter extends SyntaxHighlighterBase {

  public static final TextAttributesKey IDENTIFIER =
      createTextAttributesKey("PROTOTEXT_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
  public static final TextAttributesKey NUMBER =
      createTextAttributesKey("PROTOTEXT_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
  public static final TextAttributesKey KEYWORD =
      TextAttributesKey.createTextAttributesKey(
          "PROTOTEXT", DefaultLanguageHighlighterColors.KEYWORD);
  public static final TextAttributesKey STRING =
      createTextAttributesKey("PROTOTEXT_STRING", DefaultLanguageHighlighterColors.STRING);
  public static final TextAttributesKey ENUM_VALUE =
      createTextAttributesKey("PROTOTEXT_ENUM_VALUE", DefaultLanguageHighlighterColors.CONSTANT);
  public static final TextAttributesKey LINE_COMMENT =
      createTextAttributesKey(
          "PROTOTEXT_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
  public static final TextAttributesKey OPERATION_SIGN =
      createTextAttributesKey(
          "PROTOTEXT_OPERATION_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
  public static final TextAttributesKey BRACES =
      createTextAttributesKey("PROTOTEXT_BRACES", DefaultLanguageHighlighterColors.BRACES);
  public static final TextAttributesKey DOT =
      createTextAttributesKey("PROTOTEXT_DOT", DefaultLanguageHighlighterColors.DOT);
  public static final TextAttributesKey SEMICOLON =
      createTextAttributesKey("PROTOTEXT_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
  public static final TextAttributesKey COMMA =
      createTextAttributesKey("PROTOTEXT_COMMA", DefaultLanguageHighlighterColors.COMMA);
  public static final TextAttributesKey BRACKETS =
      TextAttributesKey.createTextAttributesKey(
          "PROTOTEXT_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);

  // String literal escape sequences.
  public static final TextAttributesKey VALID_STRING_ESCAPE =
      createTextAttributesKey(
          "PROTOTEXT_VALID_STRING_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
  public static final TextAttributesKey INVALID_STRING_ESCAPE =
      createTextAttributesKey(
          "PROTOTEXT_INVALID_STRING_ESCAPE",
          DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);

  // Directives in comments
  public static final TextAttributesKey COMMENT_DIRECTIVE =
      createTextAttributesKey(
          "PROTOTEXT_COMMENT_DIRECTIVE", DefaultLanguageHighlighterColors.KEYWORD);

  // Invalid characters.
  public static final TextAttributesKey BAD_CHARACTER =
      createTextAttributesKey("PROTOTEXT_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

  private static final Map<IElementType, TextAttributesKey> attributesKeyMap = buildAttributesMap();

  private static Map<IElementType, TextAttributesKey> buildAttributesMap() {
    Map<IElementType, TextAttributesKey> map = new HashMap<>();
    fillMap(map, IDENTIFIER, ProtoTokenTypes.IDENTIFIER_LITERAL);
    fillMap(map, NUMBER, ProtoTokenTypes.INTEGER_LITERAL, ProtoTokenTypes.FLOAT_LITERAL);
    fillMap(map, STRING, ProtoTokenTypes.STRING_LITERAL);
    fillMap(map, LINE_COMMENT, ProtoTokenTypes.LINE_COMMENT);
    fillMap(
        map, OPERATION_SIGN, ProtoTokenTypes.COLON, ProtoTokenTypes.MINUS, ProtoTokenTypes.SLASH);
    fillMap(
        map,
        BRACES,
        ProtoTokenTypes.LBRACE,
        ProtoTokenTypes.RBRACE,
        ProtoTokenTypes.LT,
        ProtoTokenTypes.GT);
    fillMap(map, BRACKETS, ProtoTokenTypes.LBRACK, ProtoTokenTypes.RBRACK);
    fillMap(map, DOT, ProtoTokenTypes.DOT);
    fillMap(map, SEMICOLON, ProtoTokenTypes.SEMI);
    fillMap(map, COMMA, ProtoTokenTypes.COMMA);

    fillMap(map, VALID_STRING_ESCAPE, StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN);
    fillMap(
        map,
        INVALID_STRING_ESCAPE,
        StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN,
        StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN);

    fillMap(map, BAD_CHARACTER, TokenType.BAD_CHARACTER);

    return Collections.unmodifiableMap(map);
  }

  public static TextAttributesKey getTokenKey(IElementType token) {
    return attributesKeyMap.get(token);
  }

  @Override
  public @NotNull Lexer getHighlightingLexer() {
    return new PbTextHighlightingLexer();
  }

  @Override
  public @NotNull TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return pack(attributesKeyMap.get(tokenType));
  }
}
