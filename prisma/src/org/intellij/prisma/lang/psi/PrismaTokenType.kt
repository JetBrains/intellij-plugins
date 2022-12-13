package org.intellij.prisma.lang.psi

import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet.create
import com.intellij.psi.tree.TokenSet.orSet
import org.intellij.prisma.lang.PrismaLanguage
import org.intellij.prisma.lang.parser.PrismaParserDefinition
import org.intellij.prisma.lang.psi.PrismaElementTypes.*

class PrismaTokenType(debugName: String) : IElementType(debugName, PrismaLanguage)

val PRISMA_STRINGS = create(STRING_LITERAL)
val PRISMA_COMMENTS = create(PrismaParserDefinition.DOC_COMMENT, PrismaParserDefinition.LINE_COMMENT)
val PRISMA_WS = create(WHITE_SPACE)
val PRISMA_KEYWORDS = create(MODEL, TYPE, ENUM, GENERATOR, DATASOURCE)
val PRISMA_BRACES = create(LBRACE, RBRACE)
val PRISMA_BRACKETS = create(LBRACKET, RBRACKET)
val PRISMA_PARENTHESES = create(LPAREN, RPAREN)

val PRISMA_BLOCKS = create(FIELD_DECLARATION_BLOCK, KEY_VALUE_BLOCK, ENUM_DECLARATION_BLOCK)
val PRISMA_DECLARATIONS = create(
  MODEL_DECLARATION, TYPE_DECLARATION, DATASOURCE_DECLARATION,
  GENERATOR_DECLARATION, ENUM_DECLARATION, TYPE_ALIAS
)
val PRISMA_TOP_ELEMENTS = orSet(PRISMA_DECLARATIONS, PRISMA_COMMENTS)
val PRISMA_BLOCK_DECLARATIONS = create(FIELD_DECLARATION, BLOCK_ATTRIBUTE, KEY_VALUE, ENUM_VALUE_DECLARATION)

val PRISMA_TYPES = create(
  FIELD_TYPE,
  SINGLE_TYPE,
  UNSUPPORTED_OPTIONAL_LIST_TYPE,
  LIST_TYPE,
  LEGACY_LIST_TYPE,
  OPTIONAL_TYPE,
  LEGACY_REQUIRED_TYPE
)

val PRISMA_LITERALS = create(STRING_LITERAL, NUMERIC_LITERAL)