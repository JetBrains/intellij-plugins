package com.intellij.dts.pp.lang.lexer

import com.intellij.dts.pp.lang.PpTokenTypes
import com.intellij.dts.pp.lang.psi.PpElifStatement
import com.intellij.dts.pp.lang.psi.PpIfStatement
import com.intellij.dts.pp.lang.psi.PpStatementType.Endif
import com.intellij.lexer.Lexer
import java.util.Stack

/**
 * This lexer should only be used for parsing since it does not emit inactive tokens.
 */
open class PpHighlightingLexerAdapter(tokenTypes: PpTokenTypes, baseLexer: Lexer) : PpLexerAdapterBase(tokenTypes, baseLexer) {
  private val stateStack = Stack<Int>()

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    stateStack.clear()

    super.start(buffer, startOffset, endOffset, initialState)
  }

  override fun getState(): Int {
    return super.getState() + stateStack.size
  }

  private fun storeState(baseLexer: Lexer) {
    stateStack.push(baseLexer.state)
  }

  private fun applyState(baseLexer: Lexer) {
    if (stateStack.empty()) return
    baseLexer.restoreState(stateStack.peek())
  }

  private fun popState(baseLexer: Lexer) {
    if (stateStack.empty()) return
    baseLexer.restoreState(stateStack.pop())
  }

  override fun processMarker(baseLexer: Lexer) {
    val tokens = tokenizeStatement(baseLexer)

    // forward tokens of the statement
    addStatementsTokens(baseLexer.tokenStart, tokens)

    // store the lexer state before an if statement and restore it after an else statement
    val statement = parseStatement(tokens)
    when {
      statement is PpIfStatement -> storeState(baseLexer)
      statement is PpElifStatement -> applyState(baseLexer)
      statement.type == Endif -> popState(baseLexer)
    }

    // advance only after the lexer state has been restored
    baseLexer.advance()
  }
}