package org.intellij.prisma.ide.highlighting

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey

object PrismaColors {
  val TRIPLE_COMMENT = createTextAttributesKey("PRISMA_TRIPLE_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
  val DOUBLE_COMMENT = createTextAttributesKey("PRISMA_DOUBLE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
  val BLOCK_COMMENT = createTextAttributesKey("PRISMA_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
  val DOC_COMMENT = createTextAttributesKey("PRISMA_DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
  val STRING_LITERAL = createTextAttributesKey("PRISMA_STRING_LITERAL", DefaultLanguageHighlighterColors.STRING)
  val KEYWORD = createTextAttributesKey("PRISMA_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
  val IDENTIFIER = createTextAttributesKey("PRISMA_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
  val NUMBER = createTextAttributesKey("PRISMA_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
  val BRACKETS = createTextAttributesKey("PRISMA_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
  val PARENTHESES = createTextAttributesKey("PRISMA_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
  val BRACES = createTextAttributesKey("PRISMA_BRACES", DefaultLanguageHighlighterColors.BRACES)
  val DOT = createTextAttributesKey("PRISMA_DOT", DefaultLanguageHighlighterColors.DOT)
  val COMMA = createTextAttributesKey("PRISMA_COMMA", DefaultLanguageHighlighterColors.COMMA)
  val OPERATION_SIGN =
    createTextAttributesKey("PRISMA_OPERATION_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN)

  val TYPE_NAME = createTextAttributesKey("PRISMA_TYPE_NAME", DefaultLanguageHighlighterColors.CLASS_NAME)
  val TYPE_REFERENCE = createTextAttributesKey("PRISMA_TYPE_REFERENCE", TYPE_NAME)
  val ATTRIBUTE = createTextAttributesKey("PRISMA_ATTRIBUTE", DefaultLanguageHighlighterColors.METADATA)
  val PARAMETER = createTextAttributesKey("PRISMA_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER)
  val FIELD_NAME = createTextAttributesKey("PRISMA_FIELD_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
  val FIELD_REFERENCE = createTextAttributesKey("PRISMA_FIELD_REFERENCE", FIELD_NAME)
  val FUNCTION = createTextAttributesKey("PRISMA_FUNCTION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
}