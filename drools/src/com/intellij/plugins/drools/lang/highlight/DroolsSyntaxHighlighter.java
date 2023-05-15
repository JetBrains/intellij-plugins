// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.highlight;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.plugins.drools.lang.lexer.DroolsLexer;
import com.intellij.plugins.drools.lang.lexer.DroolsTokenTypeSets;
import com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DroolsSyntaxHighlighter extends SyntaxHighlighterBase {
  private static final Map<IElementType,TextAttributesKey> myMap;

  static {
    myMap = new HashMap<>();

    fillMap(myMap, DroolsSyntaxHighlighterColors.PARENTHS, DroolsTokenTypes.LPAREN, DroolsTokenTypes.RPAREN);
    fillMap(myMap, DroolsSyntaxHighlighterColors.BRACES, DroolsTokenTypes.LBRACE, DroolsTokenTypes.RBRACE);
    fillMap(myMap, DroolsSyntaxHighlighterColors.BRACKETS, DroolsTokenTypes.LBRACKET, DroolsTokenTypes.RBRACKET);
    fillMap(myMap, DroolsSyntaxHighlighterColors.COMMA, DroolsTokenTypes.COMMA);
    fillMap(myMap, DroolsSyntaxHighlighterColors.STRING, DroolsTokenTypes.STRING_LITERAL);
    fillMap(myMap, DroolsSyntaxHighlighterColors.STRING, DroolsTokenTypes.STRING_ID);
    fillMap(myMap, DroolsSyntaxHighlighterColors.STRING, DroolsTokenTypes.STRING_IDENTIFIER);
    fillMap(myMap, DroolsSyntaxHighlighterColors.STRING, DroolsTokenTypes.STRING_TOKEN);
    fillMap(myMap, DroolsSyntaxHighlighterColors.NUMBER, DroolsTokenTypes.INT_TOKEN);
    fillMap(myMap, DroolsSyntaxHighlighterColors.NUMBER, DroolsTokenTypes.FLOAT_TOKEN);
    fillMap(myMap, DroolsSyntaxHighlighterColors.FIELD, DroolsTokenTypes.FIELD);
    fillMap(myMap, DroolsSyntaxHighlighterColors.COMMA, JavaTokenType.COMMA);
    fillMap(myMap, DroolsSyntaxHighlighterColors.DOT, JavaTokenType.DOT);
    fillMap(myMap, DroolsSyntaxHighlighterColors.BAD_CHARACTER, TokenType.BAD_CHARACTER);


    fillMap(myMap, DroolsTokenTypeSets.STRINGS, DroolsSyntaxHighlighterColors.STRING);
    fillMap(myMap, DroolsTokenTypeSets.PRIMITIVE_TYPES, DroolsSyntaxHighlighterColors.KEYWORD);

    fillMap(myMap, DroolsTokenTypeSets.KEYWORDS, DroolsSyntaxHighlighterColors.KEYWORD);
    fillMap(myMap, DroolsTokenTypeSets.KEYWORD_ATTRS, DroolsSyntaxHighlighterColors.ATTRIBUTES);

    fillMap(myMap, DroolsTokenTypeSets.OPERATIONS, DroolsSyntaxHighlighterColors.OPERATIONS);
    fillMap(myMap, DroolsTokenTypeSets.OPERATORS, DroolsSyntaxHighlighterColors.KEYWORD_OPERATIONS);

    fillMap(myMap, DroolsTokenTypeSets.KEYWORD_OPS, DroolsSyntaxHighlighterColors.KEYWORD_OPERATIONS);
    fillMap(myMap, DroolsTokenTypeSets.BOOLEANS,  DroolsSyntaxHighlighterColors.KEYWORD);

    fillMap(myMap, DroolsSyntaxHighlighterColors.LINE_COMMENT,  DroolsTokenTypeSets.SINGLE_LINE_COMMENT);
    fillMap(myMap, DroolsSyntaxHighlighterColors.LINE_COMMENT,  DroolsTokenTypeSets.SINGLE_LINE_COMMENT_DEPR);
    fillMap(myMap, DroolsSyntaxHighlighterColors.BLOCK_COMMENT,  DroolsTokenTypeSets.MULTI_LINE_COMMENT);
  }

  @Override
  @NotNull
  public Lexer getHighlightingLexer() {
    return new DroolsLexer();
  }

  @Override
  public TextAttributesKey @NotNull [] getTokenHighlights(final IElementType tokenType) {
    return pack(myMap.get(tokenType));
  }
}
