package com.jetbrains.lang.makefile

import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*
import com.intellij.openapi.fileTypes.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import com.jetbrains.lang.makefile.psi.*

class MakefileSyntaxHighlighter : SyntaxHighlighterBase() {
  companion object {
    val COMMENT = createTextAttributesKey("MAKEFILE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val DOCCOMMENT = createTextAttributesKey("MAKEFILE_DOCCOMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
    val KEYWORD = createTextAttributesKey("MAKEFILE_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    val TARGET = createTextAttributesKey("MAKEFILE_TARGET", DefaultLanguageHighlighterColors.CLASS_NAME)
    val SPECIAL_TARGET = createTextAttributesKey("MAKEFILE_SPECIAL_TARGET", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL)
    val SEPARATOR = createTextAttributesKey("MAKEFILE_SEPARATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val PREREQUISITE = createTextAttributesKey("MAKEFILE_PREREQUISITE", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
    val VARIABLE = createTextAttributesKey("MAKEFILE_VARIABLE", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE)
    val VARIABLE_VALUE = createTextAttributesKey("MAKEFILE_VARIABLE_VALUE", DefaultLanguageHighlighterColors.STRING)
    val STRING = createTextAttributesKey("MAKEFILE_STRING", DefaultLanguageHighlighterColors.STRING)
    val LINE_SPLIT = createTextAttributesKey("MAKEFILE_LINE_SPLIT", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    val TAB = createTextAttributesKey("MAKEFILE_TAB", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR)
    val FUNCTION = createTextAttributesKey("MAKEFILE_FUNCTION", DefaultLanguageHighlighterColors.KEYWORD)
    val FUNCTION_PARAM = createTextAttributesKey("MAKEFILE_FUNCTION_PARAM", DefaultLanguageHighlighterColors.STRING)
    val BRACES = createTextAttributesKey("MAKEFILE_BRACES", DefaultLanguageHighlighterColors.BRACES)
    val PARENS = createTextAttributesKey("MAKEFILE_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)

/*
    private fun braces(t: TextAttributesKey) {
      t.foregroundColor = Color(0x00, 0x73, 0xbf)
    }
*/

    val BAD_CHARACTER = createTextAttributesKey("MAKEFILE_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

    private val BAD_CHAR_KEYS = arrayOf(BAD_CHARACTER)
    private val SEPARATOR_KEYS = arrayOf(SEPARATOR)
    private val KEYWORD_KEYS = arrayOf(KEYWORD)
    private val TARGET_KEYS = arrayOf(TARGET)
    private val PREREQUISITE_KEYS = arrayOf(PREREQUISITE)
    private val VARIABLE_KEYS = arrayOf(VARIABLE)
    private val VARIABLE_VALUE_KEYS = arrayOf(VARIABLE_VALUE)
    private val STRING_KEYS = arrayOf(STRING)
    private val LINE_SPLIT_KEYS = arrayOf(LINE_SPLIT)
    private val TAB_KEYS = arrayOf(TAB)
    private val COMMENT_KEYS = arrayOf(COMMENT)
    private val DOCCOMMENT_KEYS = arrayOf(DOCCOMMENT)
    private val BRACES_KEYS = arrayOf(BRACES, PARENS)
    private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
  }

  override fun getTokenHighlights(tokenType: IElementType) = when(tokenType) {
    MakefileTypes.DOC_COMMENT -> DOCCOMMENT_KEYS
    MakefileTypes.COMMENT -> COMMENT_KEYS
    MakefileTypes.TARGET -> TARGET_KEYS
    MakefileTypes.COLON, MakefileTypes.ASSIGN, MakefileTypes.SEMICOLON, MakefileTypes.PIPE -> SEPARATOR_KEYS

    MakefileTypes.KEYWORD_INCLUDE, MakefileTypes.KEYWORD_IFEQ, MakefileTypes.KEYWORD_IFNEQ, MakefileTypes.KEYWORD_IFDEF,
    MakefileTypes.KEYWORD_IFNDEF, MakefileTypes.KEYWORD_ELSE, MakefileTypes.KEYWORD_ENDIF, MakefileTypes.KEYWORD_DEFINE,
    MakefileTypes.KEYWORD_ENDEF, MakefileTypes.KEYWORD_UNDEFINE, MakefileTypes.KEYWORD_OVERRIDE, MakefileTypes.KEYWORD_EXPORT,
    MakefileTypes.KEYWORD_PRIVATE, MakefileTypes.KEYWORD_VPATH, MakefileTypes.DOLLAR -> KEYWORD_KEYS

    MakefileTypes.PREREQUISITE -> PREREQUISITE_KEYS
    MakefileTypes.VARIABLE -> VARIABLE_KEYS
    MakefileTypes.VARIABLE_VALUE -> VARIABLE_VALUE_KEYS
    MakefileTypes.SPLIT -> LINE_SPLIT_KEYS
    MakefileTypes.TAB -> TAB_KEYS
    MakefileTypes.STRING -> STRING_KEYS
    MakefileTypes.OPEN_PAREN, MakefileTypes.CLOSE_PAREN, MakefileTypes.OPEN_CURLY, MakefileTypes.CLOSE_CURLY, MakefileTypes.BACKTICK -> BRACES_KEYS
    TokenType.BAD_CHARACTER -> BAD_CHAR_KEYS
    else -> EMPTY_KEYS
  }

  override fun getHighlightingLexer() = MakefileLexerAdapter()
}