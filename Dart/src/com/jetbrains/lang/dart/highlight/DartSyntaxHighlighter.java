// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

import static com.jetbrains.lang.dart.DartTokenTypes.COLON;
import static com.jetbrains.lang.dart.DartTokenTypes.COMMA;
import static com.jetbrains.lang.dart.DartTokenTypes.DOT;
import static com.jetbrains.lang.dart.DartTokenTypes.DOT_DOT;
import static com.jetbrains.lang.dart.DartTokenTypes.EXPRESSION_BODY_DEF;
import static com.jetbrains.lang.dart.DartTokenTypes.HEX_NUMBER;
import static com.jetbrains.lang.dart.DartTokenTypes.LBRACE;
import static com.jetbrains.lang.dart.DartTokenTypes.LBRACKET;
import static com.jetbrains.lang.dart.DartTokenTypes.LONG_TEMPLATE_ENTRY_END;
import static com.jetbrains.lang.dart.DartTokenTypes.LONG_TEMPLATE_ENTRY_START;
import static com.jetbrains.lang.dart.DartTokenTypes.LPAREN;
import static com.jetbrains.lang.dart.DartTokenTypes.NUMBER;
import static com.jetbrains.lang.dart.DartTokenTypes.QUEST;
import static com.jetbrains.lang.dart.DartTokenTypes.QUEST_DOT;
import static com.jetbrains.lang.dart.DartTokenTypes.QUEST_DOT_DOT;
import static com.jetbrains.lang.dart.DartTokenTypes.RBRACE;
import static com.jetbrains.lang.dart.DartTokenTypes.RBRACKET;
import static com.jetbrains.lang.dart.DartTokenTypes.RPAREN;
import static com.jetbrains.lang.dart.DartTokenTypes.SEMICOLON;
import static com.jetbrains.lang.dart.DartTokenTypes.SHORT_TEMPLATE_ENTRY_START;
import static com.jetbrains.lang.dart.DartTokenTypesSets.ASSIGNMENT_OPERATORS;
import static com.jetbrains.lang.dart.DartTokenTypesSets.BAD_CHARACTER;
import static com.jetbrains.lang.dart.DartTokenTypesSets.BINARY_OPERATORS;
import static com.jetbrains.lang.dart.DartTokenTypesSets.MULTI_LINE_COMMENT;
import static com.jetbrains.lang.dart.DartTokenTypesSets.MULTI_LINE_DOC_COMMENT;
import static com.jetbrains.lang.dart.DartTokenTypesSets.RESERVED_WORDS;
import static com.jetbrains.lang.dart.DartTokenTypesSets.SINGLE_LINE_COMMENT;
import static com.jetbrains.lang.dart.DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT;
import static com.jetbrains.lang.dart.DartTokenTypesSets.STRINGS;
import static com.jetbrains.lang.dart.DartTokenTypesSets.UNARY_OPERATORS;

public final class DartSyntaxHighlighter extends SyntaxHighlighterBase implements EmbeddedTokenHighlighter {
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
    ATTRIBUTES.put(QUEST_DOT_DOT, DartSyntaxHighlighterColors.DOT);
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
  public @NotNull Lexer getHighlightingLexer() {
    return new DartLexer();
  }

  @Override
  public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
    return pack(ATTRIBUTES.get(tokenType));
  }

  @Override
  public @NotNull MultiMap<IElementType, TextAttributesKey> getEmbeddedTokenAttributes() {
    MultiMap<IElementType, TextAttributesKey> map = MultiMap.create();
    map.putAllValues(ATTRIBUTES);
    return map;
  }

}
