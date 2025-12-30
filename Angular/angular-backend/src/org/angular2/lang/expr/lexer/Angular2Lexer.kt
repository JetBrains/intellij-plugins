// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.lexer

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lexer.*
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.lexer.Angular2TokenTypes.Companion.STRING_PART_SPECIAL_SEQ
import org.angular2.lang.html.Angular2TemplateSyntax

class Angular2Lexer(config: Config) : MergingLexerAdapterBase(FlexAdapter(_Angular2Lexer(config))), RestartableLexer {

  private var myMergeFunction: MyMergeFunction? = null

  private val flexLexer: _Angular2Lexer = (delegate as FlexAdapter).flex as _Angular2Lexer

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    super.start(buffer, startOffset, endOffset, initialState)
    myMergeFunction = MyMergeFunction(false)
    flexLexer.clearState()
  }

  override fun getStartState(): Int =
    _Angular2Lexer.YYINITIAL

  override fun getState(): Int {
    val state = super.getState()
    if (state == _Angular2Lexer.YYINITIAL || state == _Angular2Lexer.YYEXPRESSION) {
      val flex = flexLexer
      if (!flex.isRestartableState) {
        return _Angular2Lexer.YYINITIAL_WITH_NONEMPTY_STATE_STACK
      }
    }
    return state
  }

  override fun isRestartableState(state: Int): Boolean =
    state == _Angular2Lexer.YYINITIAL || state == _Angular2Lexer.YYEXPRESSION

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int, tokenIterator: TokenIterator?) {
    start(buffer, startOffset, endOffset, initialState)
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

  sealed interface Config {
    val syntax: Angular2TemplateSyntax
  }

  data class RegularBinding(
    override val syntax: Angular2TemplateSyntax,
  ) : Config

  data class BlockParameter(
    override val syntax: Angular2TemplateSyntax,
    val name: String,
    val index: Int,
  ) : Config

  private class MyMergeFunction(prevTokenEscapeSequence: Boolean) : MergeFunction {

    var isPrevTokenEscapeSequence: Boolean = prevTokenEscapeSequence
      private set

    override fun merge(type: IElementType, originalLexer: Lexer): IElementType {
      when (type) {
        JSTokenTypes.STRING_LITERAL_PART -> while (true) {
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
        JSTokenTypes.STRING_TEMPLATE_PART -> while (originalLexer.tokenType === type) {
          originalLexer.advance()
        }
        else -> {
          isPrevTokenEscapeSequence = STRING_PART_SPECIAL_SEQ.contains(type)
        }
      }
      return type
    }
  }
}
