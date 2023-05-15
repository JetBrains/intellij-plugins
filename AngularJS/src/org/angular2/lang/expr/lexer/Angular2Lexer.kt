// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.lexer

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lexer.*
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.lexer.Angular2TokenTypes.Companion.STRING_PART_SPECIAL_SEQ

class Angular2Lexer : MergingLexerAdapterBase(FlexAdapter(_Angular2Lexer(null))) {

  private var myMergeFunction: MyMergeFunction? = null

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    super.start(buffer, startOffset, endOffset, initialState)
    myMergeFunction = MyMergeFunction(false)
  }

  override fun getMergeFunction(): MergeFunction? {
    return myMergeFunction
  }

  override fun restore(position: LexerPosition) {
    val pos = position as MyLexerPosition
    myMergeFunction = MyMergeFunction(pos.isPrevTokenEscapeSequence)
    super.restore(pos.original)
  }

  override fun getCurrentPosition(): LexerPosition {
    return MyLexerPosition(super.getCurrentPosition(), myMergeFunction!!.isPrevTokenEscapeSequence)
  }

  private class MyLexerPosition(val original: LexerPosition, val isPrevTokenEscapeSequence: Boolean) : LexerPosition {

    override fun getOffset(): Int {
      return original.offset
    }

    override fun getState(): Int {
      return original.state
    }
  }

  private class MyMergeFunction(prevTokenEscapeSequence: Boolean) : MergeFunction {

    var isPrevTokenEscapeSequence: Boolean = false
      private set

    init {
      this.isPrevTokenEscapeSequence = prevTokenEscapeSequence
    }

    override fun merge(type: IElementType, originalLexer: Lexer): IElementType? {
      if (type != JSTokenTypes.STRING_LITERAL_PART) {
        isPrevTokenEscapeSequence = STRING_PART_SPECIAL_SEQ.contains(type)
        return type
      }
      while (true) {
        val tokenType = originalLexer.tokenType
        if (tokenType != JSTokenTypes.STRING_LITERAL_PART) {
          if (isPrevTokenEscapeSequence || STRING_PART_SPECIAL_SEQ.contains(tokenType)) {
            isPrevTokenEscapeSequence = false
            return JSTokenTypes.STRING_LITERAL_PART
          }
          else {
            return JSTokenTypes.STRING_LITERAL
          }
        }
        originalLexer.advance()
      }
    }
  }
}
