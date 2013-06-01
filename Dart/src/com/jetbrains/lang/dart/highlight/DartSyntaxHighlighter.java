package com.jetbrains.lang.dart.highlight;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.DartLexer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.*;

/**
 * @author: Fedor.Korotkov
 */
public class DartSyntaxHighlighter extends SyntaxHighlighterBase {
  private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<IElementType, TextAttributesKey>();

  static {
    fillMap(ATTRIBUTES, RESERVED, DartSyntaxHighlighterColors.KEYWORD);
    fillMap(ATTRIBUTES, UNRESERVED, DartSyntaxHighlighterColors.KEYWORD);

    fillMap(ATTRIBUTES, BINARY_OPERATORS, DartSyntaxHighlighterColors.OPERATION_SIGN);
    fillMap(ATTRIBUTES, LOGIC_OPERATORS, DartSyntaxHighlighterColors.OPERATION_SIGN);
    fillMap(ATTRIBUTES, BITWISE_OPERATORS, DartSyntaxHighlighterColors.OPERATION_SIGN);
    fillMap(ATTRIBUTES, UNARY_OPERATORS, DartSyntaxHighlighterColors.OPERATION_SIGN);

    fillMap(ATTRIBUTES, STRINGS, DartSyntaxHighlighterColors.STRING);

    ATTRIBUTES.put(HEX_NUMBER, DartSyntaxHighlighterColors.NUMBER);
    ATTRIBUTES.put(NUMBER, DartSyntaxHighlighterColors.NUMBER);


    ATTRIBUTES.put(LPAREN, DartSyntaxHighlighterColors.PARENTHS);
    ATTRIBUTES.put(RPAREN, DartSyntaxHighlighterColors.PARENTHS);

    ATTRIBUTES.put(LBRACE, DartSyntaxHighlighterColors.BRACES);
    ATTRIBUTES.put(RBRACE, DartSyntaxHighlighterColors.BRACES);

    ATTRIBUTES.put(LBRACKET, DartSyntaxHighlighterColors.BRACKETS);
    ATTRIBUTES.put(RBRACKET, DartSyntaxHighlighterColors.BRACKETS);

    ATTRIBUTES.put(COMMA, DartSyntaxHighlighterColors.COMMA);
    ATTRIBUTES.put(DOT, DartSyntaxHighlighterColors.DOT);
    ATTRIBUTES.put(SEMICOLON, DartSyntaxHighlighterColors.SEMICOLON);

    ATTRIBUTES.put(MULTI_LINE_COMMENT, DartSyntaxHighlighterColors.BLOCK_COMMENT);
    ATTRIBUTES.put(SINGLE_LINE_COMMENT, DartSyntaxHighlighterColors.LINE_COMMENT);
    ATTRIBUTES.put(DOC_COMMENT, DartSyntaxHighlighterColors.DOC_COMMENT);

    ATTRIBUTES.put(BAD_CHARACTER, DartSyntaxHighlighterColors.BAD_CHARACTER);
  }

  @NotNull
  public Lexer getHighlightingLexer() {
    return new DartLexer();
  }

  @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return pack(ATTRIBUTES.get(tokenType));
  }

  public static Map<IElementType, TextAttributesKey> getKeys() {
    return ATTRIBUTES;
  }
}
