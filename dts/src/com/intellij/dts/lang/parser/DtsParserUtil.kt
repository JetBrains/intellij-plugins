package com.intellij.dts.lang.parser

import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.lang.LighterASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.lang.PsiBuilderUtil
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

object DtsParserUtil : DtsJavaParserUtil() {
    private val invalidEntryEndTokens = TokenSet.create(
        DtsTypes.SEMICOLON,
        DtsTypes.RBRACE,
    )

    private val propertyNameFollowSet = TokenSet.create(
        DtsTypes.SEMICOLON,
        DtsTypes.ASSIGN,
    )

    private val nodeFirstSet = TokenSet.create(
        DtsTypes.NAME,
        DtsTypes.HANDLE,
        DtsTypes.OMIT_NODE,
    )

    private val ppMacroNameRx = Regex("[a-zA-Z_][0-9a-zA-Z_]*")

    /**
     * Rolls back all trailing preprocessor statements. Therefore, this function
     * considers all trailing children of the last production and all consecutive
     * preprocessor statements. But does not consider productions after the exit
     * marker. This is useful in case when a rule ends with an error. Like a
     * missing semicolon:
     *
     * property = "value"
     * /include/ "file"
     *
     * Without calling this function the following psi tree would be generated:
     *
     * DTS File
     *   PsiElement(PROPERTY)
     *     ...
     *     PsiElement(INCLUDE)
     *       ...
     *     PsiErrorElement
     *
     * This function rolls back the preprocessor statement at the end and would
     * yield the following psi tree:
     *
     * DTS File
     *   PsiElement(PROPERTY)
     *     ...
     *     PsiErrorElement
     *   PsiElement(INCLUDE)
     *     ...
     *
     * To move the psi error, all preprocessor statements also need to be rolled
     * back when an error is inserted.
     */
    fun rollbackPreprocessorStatements(builder: PsiBuilder, exitMarker: Marker) {
        val trailingProductions = collectTrailingProductions(builder, exitMarker)

        // collect all markers that need to be adjusted, all markers that are
        // after the preprocessor statement
        val adjustProductions = trailingProductions.dropLastWhile { it.tokenType !in DtsTokenSets.preprocessorStatements }
        if (adjustProductions.isEmpty()) return

        // check if all productions that need to be adjusted are actually marker
        if (adjustProductions.any { it !is Marker}) return

        val backups = mutableListOf<Pair<Marker, IElementType>?>()
        for (marker in adjustProductions) {
            if (marker.tokenType in DtsTokenSets.preprocessorStatements) {
                (marker as Marker).rollbackTo()
            } else {
                backups.add(backupMarker(marker))
            }
        }

        backups.reversed().filterNotNull().forEach { (marker, type) -> marker.done(type) }
    }

    /**
     * Collects all trailing productions that potentially need to be rolled
     * back. Considers all trailing children of the last production and all
     * consecutive preprocessor statements. This includes nested children but
     * does not consider productions after the exit marker.
     *
     * Returns an empty list if a trailing productions is of type node content.
     */
    private fun collectTrailingProductions(builder: PsiBuilder, exitMarker: Marker): List<PsiBuilderImpl.ProductionMarker> {
        if (builder !is Builder) return emptyList()
        val productions = builder.productions.reversed()

        var last: PsiBuilderImpl.ProductionMarker? = null
        var maxEndIndex = Int.MAX_VALUE

        val result = mutableListOf<PsiBuilderImpl.ProductionMarker>()
        for (current in productions) {
            // stop collecting if the exit marker was reached
            if (current == exitMarker) break

            // node content should not be adjusted, abort
            if (current.tokenType == DtsTypes.NODE_CONTENT) return emptyList()

            // skip children that are after the current max end index, used to
            // skip preprocessor statement children
            if (current.endIndex > maxEndIndex) continue

            // if the last was set, check if current is child of last
            if (last == null || current.startIndex >= last.startIndex && current.endIndex <= last.endIndex) {
                result.add(current)
            } else {
                break
            }

            // if the current production is a preprocessor statement, skip all
            // children and consider productions after the statement
            if (current.tokenType in DtsTokenSets.preprocessorStatements) {
                last = null
                maxEndIndex = current.startIndex
            } else {
                last = current
            }
        }

        return result
    }

    /**
     * Checks if the marker could have a preprocessor statement as its child by
     * checking the last end index of the last parsed preprocessor statement.
     * Returns true if it is not possible to determine.
     */
    fun couldContainPreprocessorStatement(builder: PsiBuilder, marker: Marker): Boolean {
        if (marker !is PsiBuilderImpl.ProductionMarker) return true

        val lastIndex = builder.getUserData(DtsBuildAdapter.lastParsedStatementIndex) ?: return true

        return lastIndex >= marker.startIndex
    }

    /**
     * Creates a backup of the given marker and drops the marker. The backup
     * is a preceding marker. Return null if it is not possible to create a
     * backup.
     */
    fun backupMarker(marker: LighterASTNode?): Pair<Marker, IElementType>? {
        if (marker !is Marker) return null

        val result = Pair(marker.precede(), marker.tokenType)
        marker.drop()

        return result
    }

    private fun lookUpLineBrake(builder: PsiBuilder, steps: Int): Boolean {
        if (builder.rawLookup(steps) != TokenType.WHITE_SPACE) return false

        val whitespace = PsiBuilderUtil.rawTokenText(builder, steps)
        return whitespace.contains('\n')
    }

    @JvmStatic
    fun parseInvalidEntry(builder: PsiBuilder, level: Int): Boolean {
        if (builder.eof()) return false
        if (builder.tokenType in invalidEntryEndTokens) return false

        val marker = builder.mark()

        consume@ while (!builder.eof()) {
            while (builder.tokenType == DtsTypes.HANDLE && DtsParser.pHandle(builder, level + 1)) {
                if (builder.eof() || lookUpLineBrake(builder, -1)) break@consume
            }

            val nextLineBrake = lookUpLineBrake(builder, 1)
            val endToken = builder.tokenType in invalidEntryEndTokens

            builder.advanceLexer()

            if (nextLineBrake || endToken) break
        }

        rollbackPreprocessorStatements(builder, marker)
        marker.error(DtsBundle.message("parser.invalid_entry"))

        return true
    }

    private fun consumePpMacroName(builder: PsiBuilder): Boolean {
        if (builder.tokenType != DtsTypes.NAME) return false

        val text = builder.tokenText ?: return false
        if (!ppMacroNameRx.matches(text)) return false

        builder.advanceLexer()
        return true
    }

    @JvmStatic
    fun parsePpMacro(builder: PsiBuilder, level: Int): Boolean {
        if (!consumePpMacroName(builder)) return false

        val validTokens = TokenSet.create(
            DtsTypes.LPAREN,
            DtsTypes.RPAREN,
            DtsTypes.COMMA,
        )

        // none function macro
        if (builder.tokenType != DtsTypes.LPAREN) return true
        builder.advanceLexer()

        var parenCount = 1
        while (!builder.eof()) {
            while (builder.tokenType == DtsTypes.NAME && DtsParser.ppMacro(builder, level + 1)) {
                if (builder.eof()) break
            }

            if (!validTokens.contains(builder.tokenType)) {
                builder.remapCurrentToken(DtsTypes.PP_MACRO_ARG)
            }

            if (builder.tokenType == DtsTypes.LPAREN) {
                parenCount++
            } else if (builder.tokenType == DtsTypes.RPAREN) {
                parenCount--
            }

            builder.advanceLexer()

            if (parenCount == 0) break
        }

        return true
    }

    @JvmStatic
    fun parseAfterLineBreak(builder: PsiBuilder, @Suppress("UNUSED_PARAMETER") level: Int): Boolean {
        return lookUpLineBrake(builder, -1)
    }

    @JvmStatic
    fun parsePropertyName(builder: PsiBuilder, @Suppress("UNUSED_PARAMETER") level: Int): Boolean {
        if (!consumeToken(builder, DtsTypes.NAME)) return false

        return builder.tokenType in propertyNameFollowSet
    }

    /**
     * Parsers trailing labels. All labels are consumed if there is no possibility
     * to parse them as leading labels. Otherwise, only labels until the next
     * line break are consumed.
     *
     * Because trailing labels are used rarely and are most likely intended as
     * leading labels for the next entry. For example:
     *
     * prop = "value" // missing semicolon
     *
     * label: node { };
     *
     * In this case it would be valid to parse the label as a trailing label for
     * the property and error at the node name. However, in theses cases the
     * label is intended for the node.
     */
    @JvmStatic
    fun parseTrailingLabels(builder: PsiBuilder, @Suppress("UNUSED_PARAMETER") level: Int): Boolean {
        if (builder.tokenType != DtsTypes.LABEL) return false

        var marker: Marker? = null
        while (builder.tokenType == DtsTypes.LABEL) {
            if (marker == null && lookUpLineBrake(builder, -1)) {
                marker = builder.mark()
            }

            consumeToken(builder, DtsTypes.LABEL)
        }

        if (builder.tokenType in nodeFirstSet) {
            marker?.rollbackTo()
        } else {
            marker?.drop()
        }

        return true
    }

    @JvmStatic
    fun quickLookaheadImpl(builder: PsiBuilder, @Suppress("UNUSED_PARAMETER") level: Int, vararg lookahead: IElementType): Boolean {
        return builder.tokenType in lookahead
    }

    /**
     * Performs a lookahead but also skips labels.
     */
    @JvmStatic
    fun labelLookaheadImpl(builder: PsiBuilder, @Suppress("UNUSED_PARAMETER") level: Int, vararg lookahead: IElementType): Boolean {
        if (builder.tokenType != DtsTypes.LABEL) {
            return builder.tokenType in lookahead
        }

        val marker = builder.mark()

        while (builder.tokenType == DtsTypes.LABEL) {
            builder.advanceLexer()
        }
        val result = builder.tokenType in lookahead

        marker.rollbackTo()

        return result
    }
}