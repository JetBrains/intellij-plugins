// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.highlighting

import com.intellij.lang.javascript.JSDocTokenTypes
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.dialects.ECMAL4LanguageDialect
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.StringEscapesTokenTypes
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType

/**
 * Allows to have separate highlighting settings for ECMAScript L4 (aka ActionScript).
 */
class ECMAL4Highlighter : JSHighlighter(ECMAL4LanguageDialect.DIALECT_OPTION_HOLDER) {
  override fun getTokenHighlights(tokenType: IElementType): Array<out TextAttributesKey> {
    if (ourDocAttributeMap.containsKey(tokenType)) {
      return pack(
        ourAttributeMap[JSDocTokenTypes.DOC_DESCRIPTION],
        ourDocAttributeMap[tokenType])
    }
    if (ourAttributeMap.containsKey(tokenType)) {
      return pack(ourAttributeMap[tokenType])
    }
    return super.getTokenHighlights(tokenType)
  }

  override fun getMappedKey(original: TextAttributesKey): TextAttributesKey {
    return ourJsToEcmaKeyMap.getOrDefault(original, original)
  }

  override fun getKeywords(): TokenSet =
    TokenSet.EMPTY // use ECMAL4_KEYWORD

  companion object {
    private val ourAttributeMap: MutableMap<IElementType, TextAttributesKey> = HashMap()
    private val ourDocAttributeMap: MutableMap<IElementType, TextAttributesKey> = HashMap()
    private val ourJsToEcmaKeyMap: MutableMap<TextAttributesKey, TextAttributesKey> = HashMap()

    @JvmField
    val ECMAL4_KEYWORD: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.KEYWORD",
        JS_KEYWORD
      )

    @JvmField
    val ECMAL4_STRING: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.STRING",
        JS_STRING
      )

    @JvmField
    val ECMAL4_NUMBER: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.NUMBER",
        JS_NUMBER
      )

    @JvmField
    val ECMAL4_REGEXP: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.REGEXP",
        JS_REGEXP
      )

    @JvmField
    val ECMAL4_LINE_COMMENT: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.LINE_COMMENT",
        JS_LINE_COMMENT
      )

    @JvmField
    val ECMAL4_BLOCK_COMMENT: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.BLOCK_COMMENT",
        JS_BLOCK_COMMENT
      )

    @JvmField
    val ECMAL4_DOC_COMMENT: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.DOC_COMMENT",
        JS_DOC_COMMENT
      )

    @JvmField
    val ECMAL4_OPERATION_SIGN: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.OPERATION_SIGN",
        JS_OPERATION_SIGN
      )

    @JvmField
    val ECMAL4_PARENTHS: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.PARENTHS",
        JS_PARENTHS
      )

    @JvmField
    val ECMAL4_BRACKETS: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.BRACKETS",
        JS_BRACKETS
      )

    @JvmField
    val ECMAL4_BRACES: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.BRACES",
        JS_BRACES
      )

    @JvmField
    val ECMAL4_COMMA: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.COMMA",
        JS_COMMA
      )

    @JvmField
    val ECMAL4_DOT: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.DOT",
        JS_DOT
      )

    @JvmField
    val ECMAL4_SEMICOLON: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.SEMICOLON",
        JS_SEMICOLON
      )

    @JvmField
    val ECMAL4_BAD_CHARACTER: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.BADCHARACTER",
        JS_BAD_CHARACTER
      )

    @JvmField
    val ECMAL4_DOC_TAG: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.DOC_TAG",
        JS_DOC_TAG
      )

    @JvmField
    val ECMAL4_VALID_STRING_ESCAPE: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.VALID_STRING_ESCAPE",
        JS_VALID_STRING_ESCAPE
      )

    @JvmField
    val ECMAL4_INVALID_STRING_ESCAPE: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.INVALID_STRING_ESCAPE",
        JS_INVALID_STRING_ESCAPE
      )

    @JvmField
    val ECMAL4_LOCAL_VARIABLE: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.AL4.LOCAL_VARIABLE",
        JS_LOCAL_VARIABLE
      )

    @JvmField
    val ECMAL4_PARAMETER: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.PARAMETER",
        JS_PARAMETER
      )

    @JvmField
    val ECMAL4_INSTANCE_MEMBER_VARIABLE: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.INSTANCE_MEMBER_VARIABLE",
        JS_INSTANCE_MEMBER_VARIABLE
      )

    @JvmField
    val ECMAL4_STATIC_MEMBER_VARIABLE: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.STATIC_MEMBER_VARIABLE",
        JS_STATIC_MEMBER_VARIABLE
      )

    @JvmField
    val ECMAL4_GLOBAL_VARIABLE: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.GLOBAL_VARIABLE",
        JS_GLOBAL_VARIABLE
      )

    @JvmField
    val ECMAL4_GLOBAL_FUNCTION: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.GLOBAL_FUNCTION",
        JS_GLOBAL_FUNCTION
      )

    @JvmField
    val ECMAL4_STATIC_MEMBER_FUNCTION: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.STATIC_MEMBER_FUNCTION",
        JS_STATIC_MEMBER_FUNCTION
      )

    @JvmField
    val ECMAL4_INSTANCE_MEMBER_FUNCTION: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.INSTANCE_MEMBER_FUNCTION",
        JS_INSTANCE_MEMBER_FUNCTION
      )

    @JvmField
    val ECMAL4_METADATA: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.ATTRIBUTE",
        DefaultLanguageHighlighterColors.METADATA
      )

    @JvmField
    val ECMAL4_CLASS: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.CLASS",
        DefaultLanguageHighlighterColors.CLASS_NAME
      )

    @JvmField
    val ECMAL4_INTERFACE: TextAttributesKey =
      TextAttributesKey.createTextAttributesKey(
        "ECMAL4.INTERFACE",
        DefaultLanguageHighlighterColors.INTERFACE_NAME
      )

    init {
      fillMap(ourAttributeMap, JSHighlighter.OPERATORS_LIKE, ECMAL4_OPERATION_SIGN)
      fillMap(ourAttributeMap, JSKeywordSets.AS_KEYWORDS, ECMAL4_KEYWORD)

      ourAttributeMap[StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN] = ECMAL4_VALID_STRING_ESCAPE
      ourAttributeMap[StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN] = ECMAL4_INVALID_STRING_ESCAPE
      ourAttributeMap[StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN] = ECMAL4_INVALID_STRING_ESCAPE

      ourAttributeMap[JSTokenTypes.NUMERIC_LITERAL] = ECMAL4_NUMBER
      ourAttributeMap[JSTokenTypes.STRING_LITERAL] = ECMAL4_STRING
      ourAttributeMap[JSTokenTypes.SINGLE_QUOTE_STRING_LITERAL] = ECMAL4_STRING
      ourAttributeMap[JSTokenTypes.REGEXP_LITERAL] = ECMAL4_REGEXP

      ourAttributeMap[JSTokenTypes.LPAR] = ECMAL4_PARENTHS
      ourAttributeMap[JSTokenTypes.RPAR] = ECMAL4_PARENTHS

      ourAttributeMap[JSTokenTypes.LBRACE] = ECMAL4_BRACES
      ourAttributeMap[JSTokenTypes.RBRACE] = ECMAL4_BRACES

      ourAttributeMap[JSTokenTypes.LBRACKET] = ECMAL4_BRACKETS
      ourAttributeMap[JSTokenTypes.RBRACKET] = ECMAL4_BRACKETS

      ourAttributeMap[JSTokenTypes.COMMA] = ECMAL4_COMMA
      ourAttributeMap[JSTokenTypes.DOT] = ECMAL4_DOT
      ourAttributeMap[JSTokenTypes.SEMICOLON] = ECMAL4_SEMICOLON

      ourAttributeMap[JSTokenTypes.C_STYLE_COMMENT] = ECMAL4_BLOCK_COMMENT
      ourAttributeMap[JSTokenTypes.XML_STYLE_COMMENT] = ECMAL4_BLOCK_COMMENT
      ourAttributeMap[JSTokenTypes.DOC_COMMENT] = ECMAL4_DOC_COMMENT
      ourAttributeMap[JSTokenTypes.END_OF_LINE_COMMENT] = ECMAL4_LINE_COMMENT
      ourAttributeMap[JSTokenTypes.BAD_CHARACTER] = ECMAL4_BAD_CHARACTER

      val javadoc = JSDocTokenTypes.enumerateJSDocElementTypes()

      for (type in javadoc) {
        ourAttributeMap[type] = ECMAL4_DOC_COMMENT
      }

      ourAttributeMap[JSDocTokenTypes.DOC_DESCRIPTION] = ECMAL4_DOC_COMMENT
      ourAttributeMap[JSDocTokenTypes.DOC_TAG_TYPE] = ECMAL4_DOC_COMMENT
      ourAttributeMap[JSDocTokenTypes.DOC_TAG_NAMEPATH] = ECMAL4_DOC_COMMENT
      ourDocAttributeMap[JSDocTokenTypes.DOC_TAG_NAME] = ECMAL4_DOC_TAG

      val javaDocMarkup2 = arrayOf(
        XmlTokenType.XML_DATA_CHARACTERS,
        XmlTokenType.XML_REAL_WHITE_SPACE,
        XmlTokenType.TAG_WHITE_SPACE,
      )

      fillMap(ourDocAttributeMap, TokenSet.create(*javaDocMarkup2), ECMAL4_DOC_COMMENT)

      ourJsToEcmaKeyMap[JS_PARAMETER] = ECMAL4_PARAMETER
      ourJsToEcmaKeyMap[JS_INSTANCE_MEMBER_VARIABLE] = ECMAL4_INSTANCE_MEMBER_VARIABLE
      ourJsToEcmaKeyMap[JS_LOCAL_VARIABLE] = ECMAL4_LOCAL_VARIABLE
      ourJsToEcmaKeyMap[JS_GLOBAL_VARIABLE] = ECMAL4_GLOBAL_VARIABLE
      ourJsToEcmaKeyMap[JS_GLOBAL_FUNCTION] = ECMAL4_GLOBAL_FUNCTION
      ourJsToEcmaKeyMap[JS_INSTANCE_MEMBER_FUNCTION] = ECMAL4_INSTANCE_MEMBER_FUNCTION
      ourJsToEcmaKeyMap[JS_STATIC_MEMBER_FUNCTION] = ECMAL4_STATIC_MEMBER_FUNCTION
      ourJsToEcmaKeyMap[JS_STATIC_MEMBER_VARIABLE] = ECMAL4_STATIC_MEMBER_VARIABLE
      ourJsToEcmaKeyMap[JS_CLASS] = ECMAL4_CLASS
      ourJsToEcmaKeyMap[JS_INTERFACE] = ECMAL4_INTERFACE
    }
  }
}
