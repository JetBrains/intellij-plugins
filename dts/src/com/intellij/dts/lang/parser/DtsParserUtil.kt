package com.intellij.dts.lang.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderUtil
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet
import com.intellij.dts.lang.psi.DtsTypes

object DtsParserUtil : GeneratedParserUtilBase() {
    private fun lookUpLineBrake(builder: PsiBuilder, steps: Int): Boolean {
        if (builder.rawLookup(steps) != TokenType.WHITE_SPACE) return false

        val whitespace = PsiBuilderUtil.rawTokenText(builder, steps)
        return whitespace.contains('\n')
    }

    @JvmStatic
    fun parseInvalidEntry(builder: PsiBuilder, level: Int): Boolean {
        if (builder.eof()) return false

        val endTokens = TokenSet.create(
            DtsTypes.SEMICOLON,
            DtsTypes.RBRACE,
        )

        val marker = builder.mark()

        while (!builder.eof()) {
            while (builder.tokenType == DtsTypes.HANDLE && DtsParser.pHandle(builder, level + 1)) {
                if (builder.eof() || lookUpLineBrake(builder, -1)) break
            }

            val nexLineBrake = lookUpLineBrake(builder, 1)
            val endToken = endTokens.contains(builder.tokenType)

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
    fun parseAfterLineBreak(builder: PsiBuilder, level: Int): Boolean {
        return lookUpLineBrake(builder, -1)
    }
}