// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil

import com.intellij.lexer.FlexAdapter
import org.intellij.terraform.hil._HILLexer.INTERPOLATION
import org.intellij.terraform.hil._HILLexer.STRING

class HILLexer : FlexAdapter(_HILLexer()) {
  companion object {
    private const val STRING_START_MASK: Int = 0xFFFF shl 0x10 // 0xFFFF0000
    private const val IN_STRING = 1 shl 14
    private const val HIL_MASK = 0x00003F00 // 8-13

    private const val JFLEX_STATE_MASK: Int = 0xFF
  }

  override fun getFlex(): _HILLexer {
    return super.getFlex() as _HILLexer
  }

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, state: Int) {
    val lexer = flex
    if (!isLexerInStringOrHILState(state) || state and IN_STRING == 0) {
      lexer.stringStart = -1
      lexer.hil = 0
    } else {
      lexer.stringStart = (state and STRING_START_MASK) ushr 0x10
      lexer.hil = (state and HIL_MASK) ushr 8
    }
    super.start(buffer, startOffset, endOffset, state and JFLEX_STATE_MASK)
  }

  private fun isLexerInStringOrHILState(state: Int): Boolean {
    return when (state and JFLEX_STATE_MASK) {
      STRING -> true
      INTERPOLATION -> true
      else -> false
    }
  }

  override fun getState(): Int {
    val lexer = flex
    var state = super.getState()
    assert(state and (JFLEX_STATE_MASK.inv()) == 0) { "State outside JFLEX_STATE_MASK ($JFLEX_STATE_MASK) should not be used by JFLex lexer" }
    state = state and JFLEX_STATE_MASK
    if (lexer.stringStart != -1) {
      state = state or IN_STRING
    }
    state = state or (((lexer.hil and 0x3f) shl 8) and HIL_MASK)
    return state
  }
}
