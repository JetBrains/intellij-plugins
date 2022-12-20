package org.intellij.prisma.ide.formatter

import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.formatter.FormatterUtil
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.IElementType
import org.intellij.prisma.lang.parser.PrismaParserDefinition.Companion.DOC_COMMENT
import org.intellij.prisma.lang.parser.PrismaParserDefinition.Companion.LINE_COMMENT
import org.intellij.prisma.lang.psi.*
import org.intellij.prisma.lang.psi.PrismaElementTypes.*
import com.intellij.psi.tree.TokenSet.create as ts

private val ONE_LINE_SPACE_DECLARATIONS = ts(FIELD_DECLARATION, KEY_VALUE, ENUM_VALUE_DECLARATION)

class PrismaSpacingProcessor(private val block: AbstractBlock, context: PrismaFormatBlockContext) {
  private val parent = block.node

  private val spacingBuilder = SpacingBuilder(context.commonSettings)
    .afterInside(PRISMA_TOP_ELEMENTS, PrismaFileElementType).lines(1, 1)
    .withinPair(LBRACE, RBRACE).lines(1, 0)
    .withinPair(LPAREN, RPAREN).noSpace()
    .withinPair(LBRACKET, RBRACKET).noSpace()
    .after(ts(AT, ATAT)).noSpace()
    .before(COMMA).noSpace()
    .after(COMMA).spaces(1, 1)
    .around(EQ).spaces(1, 1)
    .between(FIELD_DECLARATION, FIELD_DECLARATION).lines(1, 1)
    .between(FIELD_DECLARATION, BLOCK_ATTRIBUTE).lines(2, 1)
    .between(ENUM_VALUE_DECLARATION, BLOCK_ATTRIBUTE).lines(2, 1)
    .between(BLOCK_ATTRIBUTE, BLOCK_ATTRIBUTE).lines(1, 0)
    .between(ENUM_VALUE_DECLARATION, ENUM_VALUE_DECLARATION).lines(1, 1)
    .between(KEY_VALUE, KEY_VALUE).lines(1, 1)
    .between(IDENTIFIER, PRISMA_TYPES).spaces(1, 1)
    .between(PRISMA_TYPES, FIELD_ATTRIBUTE).spaces(1, 1)
    .betweenInside(IDENTIFIER, FIELD_ATTRIBUTE, ENUM_VALUE_DECLARATION).spaces(1, 1)
    .between(FIELD_ATTRIBUTE, FIELD_ATTRIBUTE).spaces(1, 1)
    .between(TYPE_REFERENCE, ts(QUEST, LBRACKET)).noSpace()
    .between(PATH_EXPRESSION, ARGUMENTS_LIST).noSpace()
    .between(UNSUPPORTED, LPAREN).noSpace()
    .beforeInside(COLON, NAMED_ARGUMENT).noSpace()
    .afterInside(COLON, NAMED_ARGUMENT).spaces(1, 1)
    .between(PRISMA_COMMENTS, ONE_LINE_SPACE_DECLARATIONS).lines(1, 1)
    .between(ONE_LINE_SPACE_DECLARATIONS, PRISMA_COMMENTS).lines(1, 1)
    .between(PRISMA_COMMENTS, BLOCK_ATTRIBUTE).lines(1, 0)
    .between(BLOCK_ATTRIBUTE, PRISMA_COMMENTS).lines(1, 0)
    .after(PRISMA_KEYWORDS).spaces(1, 1)
    .between(IDENTIFIER, PRISMA_BLOCKS).spaces(1, 1)

  fun createSpacing(child1: Block?, child2: Block): Spacing? {
    val parentType = parent.elementType
    if (parentType == PrismaFileElementType && child1 == null) {
      return none()
    }

    if (child1 is PrismaAnchorBlock) {
      return none()
    }
    if (child2 is PrismaAnchorBlock) {
      return one()
    }

    if (child1 is ASTBlock && child2 is ASTBlock) {
      val node1 = child1.node
      val node2 = child2.node

      if (node1 != null && node2 != null) {
        val type1 = node1.elementType
        val type2 = node2.elementType

        // spaces between a declaration and a trailing comment
        if (type1 in PRISMA_BLOCK_DECLARATIONS && type2 in PRISMA_COMMENTS && isTrailingComment(node2)) {
          return if (child1.subBlocks.lastOrNull() is PrismaAnchorBlock) {
            none()
          }
          else {
            one()
          }

        }

        if (type1 in PRISMA_COMMENTS && isTrailingComment(node1)) {
          val spacing = createSpacingAfterTrailingComment(node1, node2, type2)
          if (spacing != null) {
            return spacing
          }
        }
      }
    }

    return spacingBuilder.getSpacing(block, child1, child2)
  }

  private fun one(): Spacing? = Spacing.createSpacing(1, 1, 0, false, 0)

  private fun none(): Spacing? = Spacing.createSpacing(0, 0, 0, false, 0)

  private fun createSpacingAfterTrailingComment(
    node1: ASTNode?,
    node2: ASTNode?,
    type2: IElementType
  ): Spacing? {
    val prevMeaningfulNode = FormatterUtil.getPrevious(node1, WHITE_SPACE, LINE_COMMENT, DOC_COMMENT)
    val nextMeaningfulNode = FormatterUtil.getNext(node2, WHITE_SPACE, LINE_COMMENT, DOC_COMMENT)
    val prevMeaningfulType = prevMeaningfulNode?.elementType
    val nextMeaningfulType = nextMeaningfulNode?.elementType

    if (type2 in PRISMA_COMMENTS || type2 == BLOCK_ATTRIBUTE) {
      // between groups of field declarations and block attribute declarations
      if (prevMeaningfulType in ONE_LINE_SPACE_DECLARATIONS && (nextMeaningfulType == BLOCK_ATTRIBUTE || type2 == BLOCK_ATTRIBUTE)) {
        return Spacing.createSpacing(0, 0, 2, false, 1)
      }
      // comment after trailing comment inside block attributes list
      if (prevMeaningfulType == BLOCK_ATTRIBUTE && nextMeaningfulType == BLOCK_ATTRIBUTE) {
        return Spacing.createSpacing(0, 0, 1, false, 0)
      }
      // comment after trailing comment in groups of fields, enum values or key values
      if (nextMeaningfulType in ONE_LINE_SPACE_DECLARATIONS) {
        return Spacing.createSpacing(0, 0, 1, false, 1)
      }
    }
    return null
  }

  private fun isTrailingComment(node: ASTNode): Boolean = node
    .takeIf { node.elementType in PRISMA_COMMENTS }
    .let { it?.psi.skipWhitespacesBackwardWithoutNewLines() !is PsiWhiteSpace }

  private fun SpacingBuilder.RuleBuilder.lines(
    minLF: Int,
    keepBlankLines: Int,
    keepLineBreaks: Boolean = false,
  ): SpacingBuilder {
    return spacing(0, 0, minLF, keepLineBreaks, keepBlankLines)
  }

  private fun SpacingBuilder.RuleBuilder.spaces(
    minSpaces: Int,
    maxSpaces: Int,
    keepLineBreaks: Boolean = false
  ): SpacingBuilder {
    return spacing(minSpaces, maxSpaces, 0, keepLineBreaks, 0)
  }

  private fun SpacingBuilder.RuleBuilder.noSpace(): SpacingBuilder {
    return spacing(0, 0, 0, false, 0)
  }
}