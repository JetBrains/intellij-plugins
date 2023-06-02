// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.actionscript.highlighting;

import com.intellij.lang.javascript.JSDocElementType;
import com.intellij.lang.javascript.JSDocTokenTypes;
import com.intellij.lang.javascript.JSKeywordSets;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.dialects.ECMAL4LanguageDialect;
import com.intellij.lang.javascript.highlighting.JSHighlighter;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows to have separate highlighting settings for ECMAScript L4 (aka ActionScript).
 */
public class ECMAL4Highlighter extends JSHighlighter {
  private static final Map<IElementType, TextAttributesKey> ourAttributeMap = new HashMap<>();
  private static final Map<IElementType, TextAttributesKey> ourDocAttributeMap = new HashMap<>();
  private static final Map<TextAttributesKey, TextAttributesKey> ourJsToEcmaKeyMap = new HashMap<>();

  public static final TextAttributesKey ECMAL4_KEYWORD = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.KEYWORD", JSHighlighter.JS_KEYWORD
  );

  public static final TextAttributesKey ECMAL4_STRING = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.STRING", JSHighlighter.JS_STRING
  );

  public static final TextAttributesKey ECMAL4_NUMBER = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.NUMBER", JSHighlighter.JS_NUMBER
  );

  public static final TextAttributesKey ECMAL4_REGEXP = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.REGEXP", JSHighlighter.JS_REGEXP
  );

  public static final TextAttributesKey ECMAL4_LINE_COMMENT = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.LINE_COMMENT", JSHighlighter.JS_LINE_COMMENT
  );

  public static final TextAttributesKey ECMAL4_BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.BLOCK_COMMENT", JSHighlighter.JS_BLOCK_COMMENT
  );

  public static final TextAttributesKey ECMAL4_DOC_COMMENT = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.DOC_COMMENT", JSHighlighter.JS_DOC_COMMENT
  );

  public static final TextAttributesKey ECMAL4_OPERATION_SIGN = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.OPERATION_SIGN", JSHighlighter.JS_OPERATION_SIGN
  );

  public static final TextAttributesKey ECMAL4_PARENTHS = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.PARENTHS", JSHighlighter.JS_PARENTHS
  );

  public static final TextAttributesKey ECMAL4_BRACKETS = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.BRACKETS", JSHighlighter.JS_BRACKETS
  );

  public static final TextAttributesKey ECMAL4_BRACES = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.BRACES", JSHighlighter.JS_BRACES
  );

  public static final TextAttributesKey ECMAL4_COMMA = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.COMMA", JSHighlighter.JS_COMMA
  );

  public static final TextAttributesKey ECMAL4_DOT = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.DOT", JSHighlighter.JS_DOT
  );

  public static final TextAttributesKey ECMAL4_SEMICOLON = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.SEMICOLON", JSHighlighter.JS_SEMICOLON
  );

  public static final TextAttributesKey ECMAL4_BAD_CHARACTER = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.BADCHARACTER", JSHighlighter.JS_BAD_CHARACTER
  );
  public static final TextAttributesKey ECMAL4_DOC_TAG = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.DOC_TAG", JSHighlighter.JS_DOC_TAG
  );
  public static final TextAttributesKey ECMAL4_VALID_STRING_ESCAPE = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.VALID_STRING_ESCAPE", JSHighlighter.JS_VALID_STRING_ESCAPE
  );
  public static final TextAttributesKey ECMAL4_INVALID_STRING_ESCAPE = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.INVALID_STRING_ESCAPE", JSHighlighter.JS_INVALID_STRING_ESCAPE
  );
  public static final TextAttributesKey ECMAL4_LOCAL_VARIABLE = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.AL4.LOCAL_VARIABLE", JSHighlighter.JS_LOCAL_VARIABLE
  );
  public static final TextAttributesKey ECMAL4_PARAMETER = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.PARAMETER", JSHighlighter.JS_PARAMETER
  );
  public static final TextAttributesKey ECMAL4_INSTANCE_MEMBER_VARIABLE = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.INSTANCE_MEMBER_VARIABLE", JSHighlighter.JS_INSTANCE_MEMBER_VARIABLE
  );
  public static final TextAttributesKey ECMAL4_STATIC_MEMBER_VARIABLE = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.STATIC_MEMBER_VARIABLE", JSHighlighter.JS_STATIC_MEMBER_VARIABLE
  );
  public static final TextAttributesKey ECMAL4_GLOBAL_VARIABLE = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.GLOBAL_VARIABLE", JSHighlighter.JS_GLOBAL_VARIABLE
  );
  public static final TextAttributesKey ECMAL4_GLOBAL_FUNCTION = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.GLOBAL_FUNCTION", JSHighlighter.JS_GLOBAL_FUNCTION
  );
  public static final TextAttributesKey ECMAL4_STATIC_MEMBER_FUNCTION = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.STATIC_MEMBER_FUNCTION", JSHighlighter.JS_STATIC_MEMBER_FUNCTION
  );
  public static final TextAttributesKey ECMAL4_INSTANCE_MEMBER_FUNCTION = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.INSTANCE_MEMBER_FUNCTION", JSHighlighter.JS_INSTANCE_MEMBER_FUNCTION
  );
  public static final TextAttributesKey ECMAL4_METADATA = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.ATTRIBUTE", DefaultLanguageHighlighterColors.METADATA
  );
  public static final TextAttributesKey ECMAL4_CLASS = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.CLASS", DefaultLanguageHighlighterColors.CLASS_NAME
  );
  public static final TextAttributesKey ECMAL4_INTERFACE = TextAttributesKey.createTextAttributesKey(
    "ECMAL4.INTERFACE", DefaultLanguageHighlighterColors.INTERFACE_NAME
  );

  static {

    fillMap(ourAttributeMap, JSHighlighter.OPERATORS_LIKE, ECMAL4_OPERATION_SIGN);
    fillMap(ourAttributeMap, JSKeywordSets.AS_KEYWORDS, ECMAL4_KEYWORD);

    ourAttributeMap.put(StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, ECMAL4_VALID_STRING_ESCAPE);
    ourAttributeMap.put(StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, ECMAL4_INVALID_STRING_ESCAPE);
    ourAttributeMap.put(StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN, ECMAL4_INVALID_STRING_ESCAPE);

    ourAttributeMap.put(JSTokenTypes.NUMERIC_LITERAL, ECMAL4_NUMBER);
    ourAttributeMap.put(JSTokenTypes.STRING_LITERAL, ECMAL4_STRING);
    ourAttributeMap.put(JSTokenTypes.SINGLE_QUOTE_STRING_LITERAL, ECMAL4_STRING);
    ourAttributeMap.put(JSTokenTypes.REGEXP_LITERAL, ECMAL4_REGEXP);

    ourAttributeMap.put(JSTokenTypes.LPAR, ECMAL4_PARENTHS);
    ourAttributeMap.put(JSTokenTypes.RPAR, ECMAL4_PARENTHS);

    ourAttributeMap.put(JSTokenTypes.LBRACE, ECMAL4_BRACES);
    ourAttributeMap.put(JSTokenTypes.RBRACE, ECMAL4_BRACES);

    ourAttributeMap.put(JSTokenTypes.LBRACKET, ECMAL4_BRACKETS);
    ourAttributeMap.put(JSTokenTypes.RBRACKET, ECMAL4_BRACKETS);

    ourAttributeMap.put(JSTokenTypes.COMMA, ECMAL4_COMMA);
    ourAttributeMap.put(JSTokenTypes.DOT, ECMAL4_DOT);
    ourAttributeMap.put(JSTokenTypes.SEMICOLON, ECMAL4_SEMICOLON);

    ourAttributeMap.put(JSTokenTypes.C_STYLE_COMMENT, ECMAL4_BLOCK_COMMENT);
    ourAttributeMap.put(JSTokenTypes.XML_STYLE_COMMENT, ECMAL4_BLOCK_COMMENT);
    ourAttributeMap.put(JSTokenTypes.DOC_COMMENT, ECMAL4_DOC_COMMENT);
    ourAttributeMap.put(JSTokenTypes.END_OF_LINE_COMMENT, ECMAL4_LINE_COMMENT);
    ourAttributeMap.put(JSTokenTypes.BAD_CHARACTER, ECMAL4_BAD_CHARACTER);

    IElementType[] javadoc = IElementType.enumerate(type -> type instanceof JSDocElementType);

    for (IElementType type : javadoc) {
      ourAttributeMap.put(type, ECMAL4_DOC_COMMENT);
    }

    ourAttributeMap.put(JSDocTokenTypes.DOC_DESCRIPTION, ECMAL4_DOC_COMMENT);
    ourAttributeMap.put(JSDocTokenTypes.DOC_TAG_TYPE, ECMAL4_DOC_COMMENT);
    ourAttributeMap.put(JSDocTokenTypes.DOC_TAG_NAMEPATH, ECMAL4_DOC_COMMENT);
    ourDocAttributeMap.put(JSDocTokenTypes.DOC_TAG_NAME, ECMAL4_DOC_TAG);

    IElementType[] javaDocMarkup2 = {
      XmlTokenType.XML_DATA_CHARACTERS, XmlTokenType.XML_REAL_WHITE_SPACE, XmlTokenType.TAG_WHITE_SPACE
    };

    fillMap(ourDocAttributeMap, TokenSet.create(javaDocMarkup2), ECMAL4_DOC_COMMENT);

    ourJsToEcmaKeyMap.put(JS_PARAMETER, ECMAL4_PARAMETER);
    ourJsToEcmaKeyMap.put(JS_INSTANCE_MEMBER_VARIABLE, ECMAL4_INSTANCE_MEMBER_VARIABLE);
    ourJsToEcmaKeyMap.put(JS_LOCAL_VARIABLE, ECMAL4_LOCAL_VARIABLE);
    ourJsToEcmaKeyMap.put(JS_GLOBAL_VARIABLE, ECMAL4_GLOBAL_VARIABLE);
    ourJsToEcmaKeyMap.put(JS_GLOBAL_FUNCTION, ECMAL4_GLOBAL_FUNCTION);
    ourJsToEcmaKeyMap.put(JS_INSTANCE_MEMBER_FUNCTION, ECMAL4_INSTANCE_MEMBER_FUNCTION);
    ourJsToEcmaKeyMap.put(JS_STATIC_MEMBER_FUNCTION, ECMAL4_STATIC_MEMBER_FUNCTION);
    ourJsToEcmaKeyMap.put(JS_STATIC_MEMBER_VARIABLE, ECMAL4_STATIC_MEMBER_VARIABLE);
    ourJsToEcmaKeyMap.put(JS_CLASS, ECMAL4_CLASS);
    ourJsToEcmaKeyMap.put(JS_INTERFACE, ECMAL4_INTERFACE);
  }

  public ECMAL4Highlighter() {
    super(ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER, false);
  }

  @Override
  public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
    if (ourDocAttributeMap.containsKey(tokenType)) {
      return pack(ourAttributeMap.get(JSDocTokenTypes.DOC_DESCRIPTION), ourDocAttributeMap.get(tokenType));
    }
    if (ourAttributeMap.containsKey(tokenType)) {
      return pack(ourAttributeMap.get(tokenType));
    }
    return super.getTokenHighlights(tokenType);
  }

  @NotNull
  @Override
  public TextAttributesKey getMappedKey(@NotNull TextAttributesKey original) {
    return ourJsToEcmaKeyMap.getOrDefault(original, original);
  }

  @NotNull
  @Override
  public TokenSet getKeywords() {
    return TokenSet.EMPTY; // use ECMAL4_KEYWORD
  }
}
