package org.intellij.prisma.lang.psi

import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.tree.TokenSet.create
import com.intellij.psi.tree.TokenSet.orSet
import org.intellij.prisma.lang.PrismaLanguage
import org.intellij.prisma.lang.psi.PrismaElementTypes.*

class PrismaTokenType(debugName: String) : IElementType(debugName, PrismaLanguage)

val PRISMA_STRINGS: TokenSet = create(STRING_LITERAL)
val PRISMA_COMMENTS: TokenSet = create(TRIPLE_COMMENT, DOUBLE_COMMENT, BLOCK_COMMENT, DOC_COMMENT)
val PRISMA_WS: TokenSet = create(WHITE_SPACE)
val PRISMA_KEYWORDS: TokenSet = create(MODEL, TYPE, VIEW, ENUM, GENERATOR, DATASOURCE)
val PRISMA_BRACES: TokenSet = create(LBRACE, RBRACE)
val PRISMA_BRACKETS: TokenSet = create(LBRACKET, RBRACKET)
val PRISMA_PARENTHESES: TokenSet = create(LPAREN, RPAREN)

val PRISMA_BLOCKS: TokenSet = create(FIELD_DECLARATION_BLOCK, KEY_VALUE_BLOCK, ENUM_DECLARATION_BLOCK)
val PRISMA_DECLARATIONS: TokenSet = create(
  MODEL_DECLARATION, TYPE_DECLARATION, VIEW_DECLARATION, DATASOURCE_DECLARATION,
  GENERATOR_DECLARATION, ENUM_DECLARATION, TYPE_ALIAS
)
val PRISMA_TOP_ELEMENTS: TokenSet = orSet(PRISMA_DECLARATIONS, PRISMA_COMMENTS)
val PRISMA_BLOCK_DECLARATIONS: TokenSet = create(FIELD_DECLARATION, BLOCK_ATTRIBUTE, KEY_VALUE, ENUM_VALUE_DECLARATION)

val PRISMA_TYPES: TokenSet = create(
  FIELD_TYPE,
  SINGLE_TYPE,
  UNSUPPORTED_OPTIONAL_LIST_TYPE,
  LIST_TYPE,
  LEGACY_LIST_TYPE,
  OPTIONAL_TYPE,
  LEGACY_REQUIRED_TYPE
)

val PRISMA_LITERALS: TokenSet = create(STRING_LITERAL, NUMERIC_LITERAL)