package com.intellij.dts.pp.lang.lexer

import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.dts.pp.lang.psi.PpIfStatement
import com.intellij.dts.pp.lang.psi.PpStatementType.*
import com.intellij.dts.pp.lang.psi.PpToken
import com.intellij.dts.pp.lang.psi.identifier
import com.intellij.lexer.*

/**
 * Handles if statements depending on the defines. If the body of an if statements is not
 * active all tokens inside the body are joined to one [PpTokenTypes.inactive] token.
 *
 * This lexer should only be used for parsing since keeping track of the current defines
 * requires the entire file to be processed at once. For highlighting use [PpHighlightingLexerAdapter].
 */
open class PpParserLexerAdapter(tokenTypes: PpTokenTypes, baseLexer: Lexer) : PpLexerAdapterBase(tokenTypes, baseLexer) {
  private val defines = mutableListOf("I_AM_DEFINED")

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    assert(startOffset == 0)

    super.start(buffer, startOffset, endOffset, initialState)
  }

  override fun processMarker(baseLexer: Lexer) {
    val tokens = tokenizeStatement(baseLexer)

    // forward tokens of the statement
    addStatementsTokens(baseLexer.tokenStart, tokens)
    baseLexer.advance()

    // TODO: handle elsif

    // map sections to inactive depending on the statement
    val statement = parseStatement(tokens)
    when {
      statement.type == Else -> {
        processInactiveSection(baseLexer)
      }
      statement is PpIfStatement && !statement.evaluate(defines) -> {
        processInactiveSection(baseLexer)
      }
      statement.type == Define -> {
        statement.identifier?.let { defines.add(it.text.toString()) }
      }
      statement.type == Undef -> {
        statement.identifier?.let { defines.remove(it.text.toString()) }
      }
    }
  }

  private fun processInactiveSection(baseLexer: Lexer) {
    var tokens: List<PpToken>
    var nestedIfs = 0

    while (true) {
      // advance until next preprocessor statement
      while (baseLexer.tokenType != null && baseLexer.tokenType != tokenTypes.statementMarker) {
        baseLexer.advance()
      }

      // reached the end of the file before the inactive section was closed
      if (baseLexer.tokenType == null) {
        addToken(baseLexer.bufferEnd, tokenTypes.inactive)
        return
      }

      tokens = tokenizeStatement(baseLexer)
      val statement = parseStatement(tokens)

      // TODO: handle elsif

      // find the end of the inactive code section, but mind nested ifs
      when (statement.type) {
        If, IfDef, IfNdef -> nestedIfs += 1
        Endif -> if (nestedIfs == 0) break else nestedIfs -= 1
        Else -> if (nestedIfs == 0) break
        else -> {}
      }

      baseLexer.advance()
    }

    // emit one token for the inactive code section
    addToken(baseLexer.tokenStart, tokenTypes.inactive)

    // forward tokens of last statement, it is no longer part of the inactive code section
    addStatementsTokens(baseLexer.tokenStart, tokens)
    baseLexer.advance()
  }
}
