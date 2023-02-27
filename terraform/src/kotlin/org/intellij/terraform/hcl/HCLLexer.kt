// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl

import com.intellij.lexer.FlexAdapter
import java.util.*

class HCLLexer(private val capabilities: EnumSet<HCLCapability> = EnumSet.noneOf(HCLCapability::class.java)) : FlexAdapter(_HCLLexer(capabilities)) {

  companion object {
    private const val STRING_START_MASK: Int = 0xFFFF shl 0x10 // 0xFFFF0000
    private const val IN_SINGLE_QUOTED_STRING = 1 shl 15
    private const val IN_STRING = 1 shl 14
    private const val HIL_MASK = 0x00003F00 // 8-13

    private const val HEREDOC_MARKER_LENGTH: Int = 0x7F00 // 7bit positive number (0-127)
    private const val HEREDOC_MARKER_INDENTED: Int = 0x8000 // boolean
    private val HEREDOC_MARKER_WEAK_HASH: Int = STRING_START_MASK // half of int

    private const val JFLEX_STATE_MASK: Int = 0xFF
  }

  override fun getFlex(): _HCLLexer {
    return super.getFlex() as _HCLLexer
  }

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, state: Int) {
    val lexer = flex
    if (isLexerInHereDocLineState(state)) {
      lexer.myHereDocMarkerLength = (state and HEREDOC_MARKER_LENGTH) ushr 0x8
      lexer.myHereDocIndented = (state and HEREDOC_MARKER_INDENTED) != 0
      lexer.myHereDocMarkerWeakHash = (state and HEREDOC_MARKER_WEAK_HASH) ushr 0x10
    } else if (capabilities.contains(HCLCapability.INTERPOLATION_LANGUAGE)) {
      if (!isLexerInStringOrHILState(state) || state and IN_STRING == 0) {
        lexer.stringType = _HCLLexer.StringType.None
        lexer.stringStart = -1
        lexer.hil = 0
      } else {
        lexer.stringType = if (state and IN_SINGLE_QUOTED_STRING == 0) _HCLLexer.StringType.DoubleQ else _HCLLexer.StringType.SingleQ
        lexer.stringStart = (state and STRING_START_MASK) ushr 0x10
        lexer.hil = (state and HIL_MASK) ushr 8
      }
    }
    super.start(buffer, startOffset, endOffset, state and JFLEX_STATE_MASK)
  }

  private fun isLexerInHereDocLineState(state: Int): Boolean {
    return state and JFLEX_STATE_MASK == _HCLLexer.S_HEREDOC_LINE
  }

  private fun isLexerInStringOrHILState(state: Int): Boolean {
    return when (state and JFLEX_STATE_MASK) {
      _HCLLexer.S_STRING -> true
      _HCLLexer.D_STRING -> true
      _HCLLexer.HIL_EXPRESSION -> true
      else -> false
    }
  }

  override fun getState(): Int {
    val lexer = flex
    var state = super.getState()
    assert(state and (JFLEX_STATE_MASK.inv()) == 0) { "State outside JFLEX_STATE_MASK ($JFLEX_STATE_MASK) should not be used by JFLex lexer" }
    state = state and JFLEX_STATE_MASK
    if (isLexerInHereDocLineState(state)) {
      state = state or (((lexer.myHereDocMarkerLength and 0x7F) shl 0x8 ) and HEREDOC_MARKER_LENGTH)
      state = state or (((if (lexer.myHereDocIndented) 0x80 else 0x0) shl 0x8 ) and HEREDOC_MARKER_LENGTH)
      state = state or (((lexer.myHereDocMarkerWeakHash and 0xFFFF) shl 0x10 ) and HEREDOC_MARKER_WEAK_HASH)
    } else if (capabilities.contains(HCLCapability.INTERPOLATION_LANGUAGE)) {
      val type = lexer.stringType!!
      if (type != _HCLLexer.StringType.None) {
        state = state or IN_STRING
        if (type == _HCLLexer.StringType.SingleQ) {
          state = state or IN_SINGLE_QUOTED_STRING
        }
      }
      state = state or (((lexer.hil and 0x3f) shl 8) and HIL_MASK)
    }
    return state
  }
}
