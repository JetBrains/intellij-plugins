package com.intellij.dts.formatting

import com.intellij.dts.lang.DtsLanguage
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.settings.DtsCodeStyleSettings
import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType
import kotlin.math.max
import kotlin.math.min

class DtsSpacingBuilder(settings: CodeStyleSettings) {
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
            .betweenInside(DtsTypes.BYTE, DtsTypes.BYTE, DtsTypes.BYTE_ARRAY).spaceIf(custom.SPACE_BETWEEN_BYTES)
            .betweenInside(byteArrayValues, byteArrayValues, DtsTypes.BYTE_ARRAY).spaces(1)
            .betweenInside(cellArrayValues, cellArrayValues, DtsTypes.CELL_ARRAY).spaces(1)
            .betweenInside(DtsTypes.SLASH, DtsTypes.NAME, DtsTypes.PATH).none()
            .betweenInside(DtsTypes.NAME, DtsTypes.SLASH, DtsTypes.PATH).none()
            .withinPair(DtsTypes.LBRAC, DtsTypes.RBRAC).spaceIf(common.SPACE_WITHIN_BRACKETS)
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

    private fun getStatementLineFeeds(statement: DtsStatement): Pair<Int, Int>? {
        return when (statement) {
            is DtsStatement.CompilerDirective -> null
            is DtsStatement.Node -> return Pair(custom.MIN_BLANK_LINES_AROUND_NODE, custom.MAX_BLANK_LINES_AROUND_NODE)
            is DtsStatement.Property -> return Pair(custom.MIN_BLANK_LINES_AROUND_PROPERTY, custom.MAX_BLANK_LINES_AROUND_PROPERTY)
        }
    }

    private fun getEntrySpacing(entry1: DtsEntry, entry2: DtsEntry): Spacing {
        val statement1 = entry1.dtsStatement
        val statement2 = entry2.dtsStatement

        var minBlankLines = 0
        var maxBlankLines = common.KEEP_BLANK_LINES_IN_CODE

        val update = { it: Pair<Int, Int> ->
            minBlankLines = max(minBlankLines, it.first)
            maxBlankLines = max(minBlankLines, min(maxBlankLines, it.second))
        }

        getStatementLineFeeds(statement1)?.let(update)
        getStatementLineFeeds(statement2)?.let(update)

        return spacing(minLineFeeds = minBlankLines + 1, maxBlankLines = maxBlankLines)
    }

    fun getSpacing(parent: Block?, child1: Block?, child2: Block?): Spacing? {
        val child1Element = ASTBlock.getPsiElement(child1) ?: return null
        val child2Element = ASTBlock.getPsiElement(child2) ?: return null
        val parentElement = ASTBlock.getPsiElement(parent) ?: return null

        if (child1Element is DtsEntry && child2Element is DtsEntry) {
            return getEntrySpacing(child1Element, child2Element)
        }

        val type1 = child1Element.elementType
        val type2 = child2Element.elementType

        return when {
            type1 in DtsTokenSets.comments || type2 in DtsTokenSets.comments -> null
            type1 in DtsTokenSets.compilerDirectives && type2 == DtsTypes.SEMICOLON -> spacing()
            type1 != DtsTypes.LBRACE && type2 == DtsTypes.RBRACE && parentElement is DtsNode -> spacing(minLineFeeds = 1)
            else -> builder.getSpacing(parent, child1, child2)
        }
    }

    private fun spacing(minLineFeeds: Int = 0, maxBlankLines: Int? = null): Spacing {
        val keepBlankLines = maxBlankLines ?: common.KEEP_BLANK_LINES_IN_CODE

        return Spacing.createSpacing(0, 0, minLineFeeds, common.KEEP_LINE_BREAKS, keepBlankLines)
    }
}