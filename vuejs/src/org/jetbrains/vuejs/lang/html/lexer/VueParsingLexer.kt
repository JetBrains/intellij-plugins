// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.lexer

import com.intellij.lexer.DelegateLexer
import com.intellij.lexer.RestartableLexer
import com.intellij.lexer.TokenIterator
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.html.parser.VueParsing

/**
 * Emits zero-length [VueLangModeMarkerElementType] as a last element of the token stream
 * to be used by [VueParsing] unless [parentLangMode] is not null.
 */
class VueParsingLexer(private val delegateLexer: VueLexerImpl, private val parentLangMode: LangMode? = null)
  : DelegateLexer(delegateLexer), RestartableLexer {

  private var additionalState = BASE_LEXING

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    additionalState = initialState and 0b11
    delegateLexer.lexedLangMode = parentLangMode ?: LangMode.values()[initialState shr SHIFT_1 and 0b11]
    super.start(buffer, startOffset, endOffset, initialState shr SHIFT_2)

    if (additionalState != BASE_LEXING) return
    checkPendingLangMode()
  }

  override fun getState(): Int {
    val delegateState = super.getState()
    val langModeState = delegateLexer.lexedLangMode.ordinal
    return (delegateState shl SHIFT_2) or (langModeState shl SHIFT_1) or additionalState
  }

  override fun advance() {
    if (additionalState == ADDITIONAL_TOKEN_LEXING) {
      additionalState = ADDITIONAL_TOKEN_LEXED
    }
    if (additionalState != BASE_LEXING) return

    super.advance()
    checkPendingLangMode()
  }

  private fun checkPendingLangMode() {
    if (parentLangMode != null) return // do not emit additional token for nested lexers

    val baseToken = super.getTokenType()
    if (baseToken == null) {
      // delegate lexer has just finished lexing

      if (delegateLexer.lexedLangMode == LangMode.PENDING) {
        delegateLexer.lexedLangMode = LangMode.NO_TS
      }

      additionalState = ADDITIONAL_TOKEN_LEXING
    }
  }

  override fun getTokenType(): IElementType? {
    if (additionalState == ADDITIONAL_TOKEN_LEXING) {
      return delegateLexer.lexedLangMode.astMarkerToken
    }

    return super.getTokenType()
  }

  val lexedLangMode: LangMode
    get() {
      if (parentLangMode != null) return parentLangMode

      if (additionalState != ADDITIONAL_TOKEN_LEXED) error("can't use lexedLangMode before lexing the whole sequence")
      return delegateLexer.lexedLangMode
    }

  override fun getStartState(): Int {
    return 0
  }

  override fun isRestartableState(state: Int): Boolean {
    return delegateLexer.isRestartableState(state shr SHIFT_2)
  }

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int, tokenIterator: TokenIterator?) {
    start(buffer, startOffset, endOffset, initialState)
  }

  companion object {
    private const val SHIFT_1 = 2
    private const val SHIFT_2 = SHIFT_1 + 2

    private const val BASE_LEXING = 0
    private const val ADDITIONAL_TOKEN_LEXING = 1
    private const val ADDITIONAL_TOKEN_LEXED = 2
  }
}