package com.intellij.dts.lang.parser

import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderUtil
import com.intellij.psi.TokenType
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

            val nexLineBrake = lookUpLineBrake(builder, 1)
            val endToken = builder.tokenType in invalidEntryEndTokens

            builder.advanceLexer()

            if (nexLineBrake || endToken) break
        }

        marker.error("Invalid entry")

        return true
    }

    @JvmStatic
    fun parsePpMacro(builder: PsiBuilder, level: Int): Boolean {
        if (!consumeToken(builder, DtsTypes.NAME)) return false

        val validTokens = TokenSet.create(
            DtsTypes.LPAREN,
            DtsTypes.RPAREN,
            DtsTypes.COMMA
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
}