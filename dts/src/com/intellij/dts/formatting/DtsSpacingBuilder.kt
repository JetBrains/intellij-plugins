package com.intellij.dts.formatting

import com.intellij.dts.lang.DtsLanguage
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.settings.DtsCodeStyleSettings
import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType
import kotlin.math.max
import kotlin.math.min

class DtsSpacingBuilder(settings: CodeStyleSettings) {
  private data class BlankLinesRang(val lineFeeds: Int, val blankLines: Int) {
    companion object {
      fun fromSettings(minBlankLines: Int, maxBlankLines: Int) = BlankLinesRang(minBlankLines + 1, maxBlankLines)
    }

    fun getSpacing(): Spacing = Spacing.createSpacing(0, Int.MAX_VALUE, lineFeeds, true, blankLines)
  }

  private val common = settings.getCommonSettings(DtsLanguage)
  private val custom = settings.getCustomSettings(DtsCodeStyleSettings::class.java)

  private val builder: SpacingBuilder

  init {
    val relationalOperators = TokenSet.create(
      DtsTypes.LES,
      DtsTypes.GRT,
      DtsTypes.LEQ,
      DtsTypes.GEQ,
      DtsTypes.EQ,
      DtsTypes.NEQ,
    )
    val bitWiseOperators = TokenSet.create(
      DtsTypes.AND,
      DtsTypes.OR,
      DtsTypes.XOR,
    )
    val additiveOperators = TokenSet.create(
      DtsTypes.ADD,
      DtsTypes.SUB,
    )
    val multiplicativeOperators = TokenSet.create(
      DtsTypes.MUL,
      DtsTypes.DIV,
      DtsTypes.MOD,
    )
    val shiftOperators = TokenSet.create(
      DtsTypes.LSH,
      DtsTypes.RSH,
    )
    val logicalOperators = TokenSet.create(
      DtsTypes.L_AND,
      DtsTypes.L_OR,
    )
    val byteArrayValues = TokenSet.create(
      DtsTypes.BYTE,
      DtsTypes.PP_MACRO,
    )
    val cellArrayValues = TokenSet.create(
      DtsTypes.INT,
      DtsTypes.CHAR,
      DtsTypes.EXPR_VALUE,
      DtsTypes.P_HANDLE,
      DtsTypes.PP_MACRO,
    )

    builder = SpacingBuilder(settings, DtsLanguage)
      .around(DtsTypes.ASSIGN).spaceIf(common.SPACE_AROUND_ASSIGNMENT_OPERATORS)
      .around(relationalOperators).spaceIf(common.SPACE_AROUND_RELATIONAL_OPERATORS)
      .around(additiveOperators).spaceIf(common.SPACE_AROUND_ADDITIVE_OPERATORS)
      .around(multiplicativeOperators).spaceIf(common.SPACE_AROUND_MULTIPLICATIVE_OPERATORS)
      .around(bitWiseOperators).spaceIf(common.SPACE_AROUND_BITWISE_OPERATORS)
      .around(shiftOperators).spaceIf(common.SPACE_AROUND_SHIFT_OPERATORS)
      .around(logicalOperators).spaceIf(common.SPACE_AROUND_LOGICAL_OPERATORS)
      .between(DtsTypes.HANDLE, DtsTypes.NAME).none()
      .between(DtsTypes.HANDLE, DtsTypes.LBRACE).none()
      .between(DtsTypes.NODE_CONTENT, DtsTypes.RBRACE).lineBreakInCode()
      .betweenInside(DtsTypes.BYTE, DtsTypes.BYTE, DtsTypes.BYTE_ARRAY).spaceIf(custom.SPACE_BETWEEN_BYTES)
      .betweenInside(byteArrayValues, byteArrayValues, DtsTypes.BYTE_ARRAY).spaces(1)
      .betweenInside(cellArrayValues, cellArrayValues, DtsTypes.CELL_ARRAY).spaces(1)
      .betweenInside(DtsTypes.SLASH, DtsTypes.NAME, DtsTypes.PATH).none()
      .betweenInside(DtsTypes.NAME, DtsTypes.SLASH, DtsTypes.PATH).none()
      .withinPair(DtsTypes.LBRACKET, DtsTypes.RBRACKET).spaceIf(common.SPACE_WITHIN_BRACKETS)
      .withinPair(DtsTypes.LPAREN, DtsTypes.RPAREN).spaceIf(common.SPACE_WITHIN_PARENTHESES)
      .withinPair(DtsTypes.LANGL, DtsTypes.RANGL).spaceIf(custom.SPACE_WITHIN_ANGULAR_BRACKETS)
      .withinPairInside(DtsTypes.LBRACE, DtsTypes.RBRACE, DtsTypes.ROOT_NODE).spaceIf(custom.SPACE_WITHIN_EMPTY_NODE)
      .withinPairInside(DtsTypes.LBRACE, DtsTypes.RBRACE, DtsTypes.SUB_NODE).spaceIf(custom.SPACE_WITHIN_EMPTY_NODE)
      .withinPairInside(DtsTypes.LBRACE, DtsTypes.RBRACE, DtsTypes.P_HANDLE).none()
      .after(DtsTypes.COMMA).spaceIf(common.SPACE_AFTER_COMMA)
      .after(DtsTypes.LABEL).spaceIf(custom.SPACE_AFTER_LABEL)
      .after(DtsTokenSets.compilerDirectives).spaces(1)
      .before(DtsTypes.COMMA).spaceIf(common.SPACE_BEFORE_COMMA)
      .before(DtsTypes.LBRACE).spaces(1)
      .before(DtsTypes.SEMICOLON).none()
      .before(DtsTypes.SEMICOLON).lineBreakInCode()
  }

  private fun getEntryLinesRange(element: PsiElement): BlankLinesRang? {
    if (element !is DtsEntry) return null

    return when (element.dtsStatement) {
      is DtsStatement.CompilerDirective -> BlankLinesRang(0, common.KEEP_BLANK_LINES_IN_CODE)
      is DtsStatement.Node -> BlankLinesRang.fromSettings(
        custom.MIN_BLANK_LINES_BETWEEN_NODES,
        custom.MAX_BLANK_LINES_BETWEEN_NODES,
      )
      is DtsStatement.Property -> BlankLinesRang.fromSettings(
        custom.MIN_BLANK_LINES_BETWEEN_PROPERTIES,
        custom.MAX_BLANK_LINES_BETWEEN_PROPERTIES,
      )
    }
  }

  private fun getBlankLinesRange(child1: PsiElement, child2: PsiElement): BlankLinesRang? {
    if (child1.elementType in DtsTokenSets.comments || child2.elementType in DtsTokenSets.comments) {
      return BlankLinesRang(0, common.KEEP_BLANK_LINES_IN_CODE)
    }

    val range1 = getEntryLinesRange(child1)
    val range2 = getEntryLinesRange(child2)

    if (range1 == null) return range2
    if (range2 == null) return range1

    return BlankLinesRang(
      max(range1.lineFeeds, range2.lineFeeds),
      min(range1.blankLines, range2.blankLines),
    )
  }

  fun getSpacing(parent: Block?, child1: Block?, child2: Block?): Spacing? {
    val child1Element = ASTBlock.getPsiElement(child1) ?: return null
    val child2Element = ASTBlock.getPsiElement(child2) ?: return null

    val range = getBlankLinesRange(child1Element, child2Element)
    if (range != null) return range.getSpacing()

    return builder.getSpacing(parent, child1, child2)
  }
}

