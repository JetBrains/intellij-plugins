package org.intellij.prisma.lang.psi

import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.tree.TokenSet.create
import com.intellij.psi.tree.TokenSet.orSet
import org.intellij.prisma.lang.PrismaLanguage
import org.intellij.prisma.lang.psi.PrismaElementTypes.BLOCK_ATTRIBUTE
import org.intellij.prisma.lang.psi.PrismaElementTypes.BLOCK_COMMENT
import org.intellij.prisma.lang.psi.PrismaElementTypes.DATASOURCE
import org.intellij.prisma.lang.psi.PrismaElementTypes.DATASOURCE_DECLARATION
import org.intellij.prisma.lang.psi.PrismaElementTypes.DOUBLE_COMMENT
import org.intellij.prisma.lang.psi.PrismaElementTypes.ENUM
import org.intellij.prisma.lang.psi.PrismaElementTypes.ENUM_DECLARATION
import org.intellij.prisma.lang.psi.PrismaElementTypes.ENUM_DECLARATION_BLOCK
import org.intellij.prisma.lang.psi.PrismaElementTypes.ENUM_VALUE_DECLARATION
import org.intellij.prisma.lang.psi.PrismaElementTypes.FIELD_DECLARATION
import org.intellij.prisma.lang.psi.PrismaElementTypes.FIELD_DECLARATION_BLOCK
import org.intellij.prisma.lang.psi.PrismaElementTypes.FIELD_TYPE
import org.intellij.prisma.lang.psi.PrismaElementTypes.GENERATOR
import org.intellij.prisma.lang.psi.PrismaElementTypes.GENERATOR_DECLARATION
import org.intellij.prisma.lang.psi.PrismaElementTypes.KEY_VALUE
import org.intellij.prisma.lang.psi.PrismaElementTypes.KEY_VALUE_BLOCK
import org.intellij.prisma.lang.psi.PrismaElementTypes.LBRACE
import org.intellij.prisma.lang.psi.PrismaElementTypes.LBRACKET
import org.intellij.prisma.lang.psi.PrismaElementTypes.LEGACY_LIST_TYPE
import org.intellij.prisma.lang.psi.PrismaElementTypes.LEGACY_REQUIRED_TYPE
import org.intellij.prisma.lang.psi.PrismaElementTypes.LIST_TYPE
import org.intellij.prisma.lang.psi.PrismaElementTypes.LPAREN
import org.intellij.prisma.lang.psi.PrismaElementTypes.MODEL
import org.intellij.prisma.lang.psi.PrismaElementTypes.MODEL_DECLARATION
import org.intellij.prisma.lang.psi.PrismaElementTypes.NUMERIC_LITERAL
import org.intellij.prisma.lang.psi.PrismaElementTypes.OPTIONAL_TYPE
import org.intellij.prisma.lang.psi.PrismaElementTypes.RBRACE
import org.intellij.prisma.lang.psi.PrismaElementTypes.RBRACKET
import org.intellij.prisma.lang.psi.PrismaElementTypes.RPAREN
import org.intellij.prisma.lang.psi.PrismaElementTypes.SINGLE_TYPE
import org.intellij.prisma.lang.psi.PrismaElementTypes.STRING_LITERAL
import org.intellij.prisma.lang.psi.PrismaElementTypes.TRIPLE_COMMENT
import org.intellij.prisma.lang.psi.PrismaElementTypes.TYPE
import org.intellij.prisma.lang.psi.PrismaElementTypes.TYPE_ALIAS
import org.intellij.prisma.lang.psi.PrismaElementTypes.TYPE_DECLARATION
import org.intellij.prisma.lang.psi.PrismaElementTypes.UNSUPPORTED_OPTIONAL_LIST_TYPE
import org.intellij.prisma.lang.psi.PrismaElementTypes.VIEW
import org.intellij.prisma.lang.psi.PrismaElementTypes.VIEW_DECLARATION

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
