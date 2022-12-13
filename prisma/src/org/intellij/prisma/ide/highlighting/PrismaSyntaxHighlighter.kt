package org.intellij.prisma.ide.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import org.intellij.prisma.lang.lexer.PrismaLexer
import org.intellij.prisma.lang.parser.PrismaParserDefinition
import org.intellij.prisma.lang.psi.*

class PrismaSyntaxHighlighter : SyntaxHighlighterBase() {
  override fun getHighlightingLexer(): Lexer = PrismaLexer()

  override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
    pack(attributes[tokenType])

  companion object {
    private val attributes = buildMap<IElementType, TextAttributesKey>() {
      put(PrismaParserDefinition.DOC_COMMENT, PrismaColors.DOC_COMMENT)
      put(PrismaParserDefinition.LINE_COMMENT, PrismaColors.LINE_COMMENT)
      put(PrismaElementTypes.STRING_LITERAL, PrismaColors.STRING_LITERAL)
      put(PrismaElementTypes.IDENTIFIER, PrismaColors.IDENTIFIER)
      put(PrismaElementTypes.NUMERIC_LITERAL, PrismaColors.NUMBER)
      put(PrismaElementTypes.COMMA, PrismaColors.COMMA)
      put(PrismaElementTypes.DOT, PrismaColors.DOT)
      put(PrismaElementTypes.EQ, PrismaColors.OPERATION_SIGN)
      put(PrismaElementTypes.COLON, PrismaColors.OPERATION_SIGN)
      put(PrismaElementTypes.QUEST, PrismaColors.OPERATION_SIGN)
      put(PrismaElementTypes.EXCL, PrismaColors.OPERATION_SIGN)
      put(PrismaElementTypes.UNSUPPORTED, PrismaColors.TYPE_REFERENCE)

      fillMap(this, PRISMA_KEYWORDS, PrismaColors.KEYWORD)
      fillMap(this, PRISMA_BRACKETS, PrismaColors.BRACKETS)
      fillMap(this, PRISMA_BRACES, PrismaColors.BRACES)
      fillMap(this, PRISMA_PARENTHESES, PrismaColors.PARENTHESES)
    }
  }
}