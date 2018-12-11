package com.jetbrains.lang.dart.highlight;

import com.intellij.ide.highlighter.EmbeddedTokenHighlighter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.MultiMap;
import com.jetbrains.lang.dart.lexer.DartLexer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.*;

public class DartSyntaxHighlighter extends SyntaxHighlighterBase implements EmbeddedTokenHighlighter {
  private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

  static {
    fillMap(ATTRIBUTES, RESERVED_WORDS, DartSyntaxHighlighterColors.KEYWORD);

    fillMap(ATTRIBUTES, ASSIGNMENT_OPERATORS, DartSyntaxHighlighterColors.OPERATION_SIGN);
    fillMap(ATTRIBUTES, BINARY_OPERATORS, DartSyntaxHighlighterColors.OPERATION_SIGN);
    fillMap(ATTRIBUTES, UNARY_OPERATORS, DartSyntaxHighlighterColors.OPERATION_SIGN);
    // '?' from ternary operator; ':' is handled separately in dartColorAnnotator, because there are also ':' in other syntax constructs
    ATTRIBUTES.put(QUEST, DartSyntaxHighlighterColors.OPERATION_SIGN);

    fillMap(ATTRIBUTES, STRINGS, DartSyntaxHighlighterColors.STRING);

    ATTRIBUTES.put(HEX_NUMBER, DartSyntaxHighlighterColors.NUMBER);
    ATTRIBUTES.put(NUMBER, DartSyntaxHighlighterColors.NUMBER);


    ATTRIBUTES.put(LPAREN, DartSyntaxHighlighterColors.PARENTHS);
    ATTRIBUTES.put(RPAREN, DartSyntaxHighlighterColors.PARENTHS);

    ATTRIBUTES.put(LBRACE, DartSyntaxHighlighterColors.BRACES);
    ATTRIBUTES.put(RBRACE, DartSyntaxHighlighterColors.BRACES);
    ATTRIBUTES.put(SHORT_TEMPLATE_ENTRY_START, DartSyntaxHighlighterColors.BRACES);
    ATTRIBUTES.put(LONG_TEMPLATE_ENTRY_START, DartSyntaxHighlighterColors.BRACES);
    ATTRIBUTES.put(LONG_TEMPLATE_ENTRY_END, DartSyntaxHighlighterColors.BRACES);

    ATTRIBUTES.put(LBRACKET, DartSyntaxHighlighterColors.BRACKETS);
    ATTRIBUTES.put(RBRACKET, DartSyntaxHighlighterColors.BRACKETS);

    ATTRIBUTES.put(COMMA, DartSyntaxHighlighterColors.COMMA);
    ATTRIBUTES.put(DOT, DartSyntaxHighlighterColors.DOT);
    ATTRIBUTES.put(DOT_DOT, DartSyntaxHighlighterColors.DOT);
    ATTRIBUTES.put(QUEST_DOT, DartSyntaxHighlighterColors.DOT);
    ATTRIBUTES.put(SEMICOLON, DartSyntaxHighlighterColors.SEMICOLON);
    ATTRIBUTES.put(COLON, DartSyntaxHighlighterColors.COLON);
    ATTRIBUTES.put(EXPRESSION_BODY_DEF, DartSyntaxHighlighterColors.FAT_ARROW);

    ATTRIBUTES.put(SINGLE_LINE_COMMENT, DartSyntaxHighlighterColors.LINE_COMMENT);
    ATTRIBUTES.put(SINGLE_LINE_DOC_COMMENT, DartSyntaxHighlighterColors.DOC_COMMENT);
    ATTRIBUTES.put(MULTI_LINE_COMMENT, DartSyntaxHighlighterColors.BLOCK_COMMENT);
    ATTRIBUTES.put(MULTI_LINE_DOC_COMMENT, DartSyntaxHighlighterColors.DOC_COMMENT);

    ATTRIBUTES.put(BAD_CHARACTER, DartSyntaxHighlighterColors.BAD_CHARACTER);
  }

  @Override
  @NotNull
  public Lexer getHighlightingLexer() {
    return new DartLexer();
  }

  @Override
  @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return pack(ATTRIBUTES.get(tokenType));
  }

  @NotNull
  @Override
  public MultiMap<IElementType, TextAttributesKey> getEmbeddedTokenAttributes() {
    MultiMap<IElementType, TextAttributesKey> map = MultiMap.create();
    map.putAllValues(ATTRIBUTES);
    return map;
  }

}
