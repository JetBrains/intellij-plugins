package com.intellij.dts.lang.parser

import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.lang.PsiBuilderUtil
import com.intellij.dts.pp.lang.parser.PpBuildAdapter
import com.intellij.dts.pp.lang.parser.PpParserUtil
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

        PpParserUtil.rollbackPreprocessorStatements(builder as PpBuildAdapter, marker)
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
}