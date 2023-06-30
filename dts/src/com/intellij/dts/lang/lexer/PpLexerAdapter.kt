package com.intellij.dts.lang.lexer

import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.lexer.Lexer
import com.intellij.lexer.LexerUtil
import com.intellij.lexer.LookAheadLexer

open class PpLexerAdapter(baseLexer: Lexer) : LookAheadLexer(baseLexer) {
    private val ppLexer = PpLexer(null)

    override fun lookAhead(baseLexer: Lexer) {
        if (baseLexer.tokenType != DtsTypes.PP_STATEMENT_MARKER) {
            advanceLexer(baseLexer)
            return
        }

        processStatementContent(baseLexer)
        baseLexer.advance()
    }

    private fun processStatementContent(baseLexer: Lexer) {
        val content = LexerUtil.getTokenText(baseLexer)

        ppLexer.reset(content, 0, content.length, PpLexer.YYINITIAL)

        while (true) {
            val token = ppLexer.advance() ?: break

            addToken(baseLexer.tokenStart + ppLexer.tokenEnd, token)
        }

        addToken(baseLexer.tokenEnd, DtsTypes.PP_STATEMENT_END)
    }
}