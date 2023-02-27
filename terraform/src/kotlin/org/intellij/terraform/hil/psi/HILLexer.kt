/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.hil.psi

import com.intellij.lexer.FlexAdapter
import org.intellij.terraform.hil.psi._HILLexer.INTERPOLATION
import org.intellij.terraform.hil.psi._HILLexer.STRING

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
