package com.intellij.pp.lang.lexer

import com.intellij.lexer.Lexer
import com.intellij.lexer.LexerUtil
import com.intellij.lexer.LookAheadLexer
import com.intellij.pp.lang.PpTokenTypes

open class PpLexerAdapter(private val tokenTypes: PpTokenTypes, baseLexer: Lexer) : LookAheadLexer(baseLexer) {
    private val ppLexer = PpLexer(null, tokenTypes)

    override fun lookAhead(baseLexer: Lexer) {
        if (baseLexer.tokenType != tokenTypes.statementMarker) {
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

        addToken(baseLexer.tokenEnd, tokenTypes.statementEnd)
    }
}