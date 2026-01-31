package org.intellij.prisma.ide.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import org.intellij.prisma.lang.lexer.PrismaLexer
import org.intellij.prisma.lang.psi.DOC_COMMENT
import org.intellij.prisma.lang.psi.PRISMA_BRACES
import org.intellij.prisma.lang.psi.PRISMA_BRACKETS
import org.intellij.prisma.lang.psi.PRISMA_KEYWORDS
import org.intellij.prisma.lang.psi.PRISMA_PARENTHESES
import org.intellij.prisma.lang.psi.PrismaElementTypes.BLOCK_COMMENT
import org.intellij.prisma.lang.psi.PrismaElementTypes.COLON
import org.intellij.prisma.lang.psi.PrismaElementTypes.COMMA
import org.intellij.prisma.lang.psi.PrismaElementTypes.DOT
import org.intellij.prisma.lang.psi.PrismaElementTypes.DOUBLE_COMMENT
import org.intellij.prisma.lang.psi.PrismaElementTypes.EQ
import org.intellij.prisma.lang.psi.PrismaElementTypes.EXCL
import org.intellij.prisma.lang.psi.PrismaElementTypes.IDENTIFIER
import org.intellij.prisma.lang.psi.PrismaElementTypes.NUMERIC_LITERAL
import org.intellij.prisma.lang.psi.PrismaElementTypes.QUEST
import org.intellij.prisma.lang.psi.PrismaElementTypes.STRING_LITERAL
import org.intellij.prisma.lang.psi.PrismaElementTypes.TRIPLE_COMMENT
import org.intellij.prisma.lang.psi.PrismaElementTypes.UNSUPPORTED

class PrismaSyntaxHighlighter : SyntaxHighlighterBase() {
  override fun getHighlightingLexer(): Lexer = PrismaLexer()

  override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
    pack(ATTRIBUTES[tokenType])
}

private val ATTRIBUTES = buildMap<IElementType, TextAttributesKey> {
  put(TRIPLE_COMMENT, PrismaColors.TRIPLE_COMMENT)
  put(DOUBLE_COMMENT, PrismaColors.DOUBLE_COMMENT)
  put(BLOCK_COMMENT, PrismaColors.BLOCK_COMMENT)
  put(DOC_COMMENT, PrismaColors.DOC_COMMENT)
  put(STRING_LITERAL, PrismaColors.STRING_LITERAL)
  put(IDENTIFIER, PrismaColors.IDENTIFIER)
  put(NUMERIC_LITERAL, PrismaColors.NUMBER)
  put(COMMA, PrismaColors.COMMA)
  put(DOT, PrismaColors.DOT)
  put(EQ, PrismaColors.OPERATION_SIGN)
  put(COLON, PrismaColors.OPERATION_SIGN)
  put(QUEST, PrismaColors.OPERATION_SIGN)
  put(EXCL, PrismaColors.OPERATION_SIGN)
  put(UNSUPPORTED, PrismaColors.TYPE_REFERENCE)

  SyntaxHighlighterBase.fillMap(this, PRISMA_KEYWORDS, PrismaColors.KEYWORD)
  SyntaxHighlighterBase.fillMap(this, PRISMA_BRACKETS, PrismaColors.BRACKETS)
  SyntaxHighlighterBase.fillMap(this, PRISMA_BRACES, PrismaColors.BRACES)
  SyntaxHighlighterBase.fillMap(this, PRISMA_PARENTHESES, PrismaColors.PARENTHESES)
}