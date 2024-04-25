package com.intellij.dts.pp.lang.lexer

import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.dts.pp.lang.parser.PpAdHocParser
import com.intellij.dts.pp.lang.psi.PpStatement
import com.intellij.dts.pp.lang.psi.PpToken
import com.intellij.lexer.Lexer
import com.intellij.lexer.LexerUtil
import com.intellij.lexer.LookAheadLexer
import com.intellij.openapi.util.TextRange

abstract class PpLexerAdapterBase(protected val tokenTypes: PpTokenTypes, baseLexer: Lexer) : LookAheadLexer(baseLexer) {
  private val ppLexer = PpLexer(null, tokenTypes)
  private val ppParser = PpAdHocParser(tokenTypes)

  final override fun lookAhead(baseLexer: Lexer) {
    if (baseLexer.tokenType != tokenTypes.statementMarker) {
      advanceLexer(baseLexer)
    }
    else {
      processMarker(baseLexer)
    }
  }

  protected abstract fun processMarker(baseLexer: Lexer)

  protected fun tokenizeStatement(baseLexer: Lexer): List<PpToken> {
    assert(baseLexer.tokenType == tokenTypes.statementMarker)

    val content = LexerUtil.getTokenText(baseLexer)
    ppLexer.reset(content, 0, content.length, PpLexer.YYINITIAL)

    return buildList {
      while (true) {
        val token = ppLexer.advance() ?: break
        add(PpToken(token, ppLexer.yytext(), TextRange(ppLexer.tokenStart, ppLexer.tokenEnd)))
      }

      add(PpToken(tokenTypes.statementEnd, "", TextRange.from(content.length, 0)))
    }
  }

  protected fun parseStatement(tokens: List<PpToken>): PpStatement {
    return ppParser.parse(tokens)
  }

  protected fun addStatementsTokens(startOffset: Int, tokens: List<PpToken>) {
    var offset = 0
    for (token in tokens) {
      offset += token.text.length
      addToken(startOffset + offset, token.type)
    }
  }
}