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

    SyntaxHighlighterBase.fillMap(myMap, DroolsSyntaxHighlighterColors.PARENTHS, DroolsTokenTypes.LPAREN, DroolsTokenTypes.RPAREN);
    SyntaxHighlighterBase.fillMap(myMap, DroolsSyntaxHighlighterColors.BRACES, DroolsTokenTypes.LBRACE, DroolsTokenTypes.RBRACE);
    SyntaxHighlighterBase.fillMap(myMap, DroolsSyntaxHighlighterColors.BRACKETS, DroolsTokenTypes.LBRACKET, DroolsTokenTypes.RBRACKET);
    SyntaxHighlighterBase.fillMap(myMap, DroolsSyntaxHighlighterColors.COMMA, DroolsTokenTypes.COMMA);
    SyntaxHighlighterBase.fillMap(myMap, DroolsSyntaxHighlighterColors.STRING, DroolsTokenTypes.STRING_LITERAL);
    SyntaxHighlighterBase.fillMap(myMap, DroolsSyntaxHighlighterColors.NUMBER, DroolsTokenTypes.INT_TOKEN);
    SyntaxHighlighterBase.fillMap(myMap, DroolsSyntaxHighlighterColors.NUMBER, DroolsTokenTypes.FLOAT_TOKEN);
    SyntaxHighlighterBase.fillMap(myMap, DroolsSyntaxHighlighterColors.COMMA, JavaTokenType.COMMA);
    SyntaxHighlighterBase.fillMap(myMap, DroolsSyntaxHighlighterColors.DOT, JavaTokenType.DOT);
    SyntaxHighlighterBase.fillMap(myMap, DroolsSyntaxHighlighterColors.BAD_CHARACTER, TokenType.BAD_CHARACTER);

    SyntaxHighlighterBase.fillMap(myMap, DroolsTokenTypeSets.PRIMITIVE_TYPES, DroolsSyntaxHighlighterColors.KEYWORD);

    SyntaxHighlighterBase.fillMap(myMap, DroolsTokenTypeSets.KEYWORDS, DroolsSyntaxHighlighterColors.KEYWORD);
    SyntaxHighlighterBase.fillMap(myMap, DroolsTokenTypeSets.KEYWORD_ATTRS, DroolsSyntaxHighlighterColors.ATTRIBUTES);

    SyntaxHighlighterBase.fillMap(myMap, DroolsTokenTypeSets.OPERATIONS, DroolsSyntaxHighlighterColors.OPERATIONS);
    SyntaxHighlighterBase.fillMap(myMap, DroolsTokenTypeSets.OPERATORS, DroolsSyntaxHighlighterColors.KEYWORD_OPERATIONS);

    SyntaxHighlighterBase.fillMap(myMap, DroolsTokenTypeSets.KEYWORD_OPS, DroolsSyntaxHighlighterColors.KEYWORD_OPERATIONS);
    SyntaxHighlighterBase.fillMap(myMap, DroolsTokenTypeSets.BOOLEANS,  DroolsSyntaxHighlighterColors.KEYWORD);

    SyntaxHighlighterBase.fillMap(myMap, DroolsSyntaxHighlighterColors.LINE_COMMENT,  DroolsTokenTypeSets.SINGLE_LINE_COMMENT);
    SyntaxHighlighterBase.fillMap(myMap, DroolsSyntaxHighlighterColors.LINE_COMMENT,  DroolsTokenTypeSets.SINGLE_LINE_COMMENT_DEPR);
    SyntaxHighlighterBase.fillMap(myMap, DroolsSyntaxHighlighterColors.BLOCK_COMMENT,  DroolsTokenTypeSets.MULTI_LINE_COMMENT);
  }

  @Override
  @NotNull
  public Lexer getHighlightingLexer() {
    return new DroolsLexer();
  }

  @Override
  public TextAttributesKey @NotNull [] getTokenHighlights(final IElementType tokenType) {
    return SyntaxHighlighterBase.pack(myMap.get(tokenType));
  }
}
