package org.jetbrains.vuejs.language

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

class VueInterpolationLexer(val prefix:String, val suffix:String, val type:IElementType) : LexerBase() {
  private var buffer: CharSequence? = null
  private var state = 0
  private var element:IElementType? = null
  private var start = 0
  private var end = 0

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    this.buffer = buffer
    state = 0
    start = 0
    end = 0
  }

  override fun getState() = state
  override fun getTokenType() = element
  override fun getTokenStart() = start
  override fun getTokenEnd() = end

  override fun advance() {
    if (end == buffer!!.length) {
      element = null
      return
    }
    start = end
    when (state) {
      0 -> {
        val prefixStart = buffer!!.indexOf(prefix, end)
        if (prefixStart >= 0) {
          end = prefixStart + prefix.length
          state = 1
        } else {
          end = buffer!!.length
        }
        element = type
      }
      1 -> {
        val suffixStart = buffer!!.indexOf(suffix, end)
        if (suffixStart >= 0) {
          end = suffixStart
          state = 0
        } else {
          end = buffer!!.length
        }
        element = VueElementTypes.EMBEDDED_JS
      }
    }
  }

  override fun getBufferSequence() = buffer!!
  override fun getBufferEnd() = buffer!!.length
}