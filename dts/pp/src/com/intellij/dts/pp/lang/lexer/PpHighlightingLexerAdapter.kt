package com.intellij.dts.pp.lang.lexer

import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.dts.pp.lang.psi.PpIfStatement
import com.intellij.dts.pp.lang.psi.PpStatementType.Else
import com.intellij.lexer.Lexer
import java.util.*

/**
 * This lexer should only be used for parsing since it does not emit inactive tokens.
 */
open class PpHighlightingLexerAdapter(tokenTypes: PpTokenTypes, baseLexer: Lexer) : PpLexerAdapterBase(tokenTypes, baseLexer) {
  private val stateStack = Stack<Int>()

  private var currentBuffer: CharSequence = ""
  private var currentStartOffset: Int = 0
  private var currentEndOffset: Int = 0

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    stateStack.clear()

    currentBuffer = buffer
    currentStartOffset = startOffset
    currentEndOffset = endOffset

    super.start(buffer, startOffset, endOffset, initialState)
  }

  override fun getState(): Int {
    return super.getState() + stateStack.size
  }

  private fun storeState(baseLexer: Lexer) {
    stateStack.push(baseLexer.state)
  }

  private fun restoreState(baseLexer: Lexer) {
    if (stateStack.empty()) return

    baseLexer.start(currentBuffer, baseLexer.tokenStart, currentEndOffset, stateStack.pop())
  }

  override fun processMarker(baseLexer: Lexer) {
    val tokens = tokenizeStatement(baseLexer)

    // forward tokens of the statement
    addStatementsTokens(baseLexer.tokenStart, tokens)
    baseLexer.advance()

    // TODO: handle elsif

    // store the lexer state before an if statement and restore it after an else statement
    val statement = parseStatement(tokens)
    when {
      statement is PpIfStatement -> storeState(baseLexer)
      statement.type == Else -> restoreState(baseLexer)
    }
  }
}