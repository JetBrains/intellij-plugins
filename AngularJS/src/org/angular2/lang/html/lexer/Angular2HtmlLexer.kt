// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.lexer

import com.intellij.lexer.*
import com.intellij.openapi.util.Pair
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType
import org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType

class Angular2HtmlLexer(tokenizeExpansionForms: Boolean, interpolationConfig: Pair<String?, String?>?)
  : HtmlLexer(Angular2HtmlMergingLexer(FlexAdapter(_Angular2HtmlLexer(tokenizeExpansionForms, interpolationConfig))), true) {

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int, tokenIterator: TokenIterator?) {
    (delegate as Angular2HtmlMergingLexer).reset()
    super.start(buffer, startOffset, endOffset, initialState, tokenIterator)
  }

  override fun getState(): Int {
    return super.getState() or (if ((delegate as Angular2HtmlMergingLexer).isWithinExpansionForm) IS_WITHIN_EXPANSION_FORM_STATE else 0)
  }

  override fun isRestartableState(state: Int): Boolean {
    return super.isRestartableState(state)
           && (state and IS_WITHIN_EXPANSION_FORM_STATE) == 0
           && state != _Angular2HtmlLexer.EXPANSION_FORM_CONTENT
           && state != _Angular2HtmlLexer.EXPANSION_FORM_CASE_END
  }

  override fun isHtmlTagState(state: Int): Boolean {
    return state == _Angular2HtmlLexer.START_TAG_NAME || state == _Angular2HtmlLexer.END_TAG_NAME
  }

  open class Angular2HtmlMergingLexer(original: FlexAdapter) : MergingLexerAdapterBase(original) {

    private var prevExpansionFormNestingLevel: Int = 0

    val isWithinExpansionForm: Boolean
      get() = prevExpansionFormNestingLevel != 0 || flexLexer.expansionFormNestingLevel != 0

    fun reset() {
      prevExpansionFormNestingLevel = 0
      flexLexer.expansionFormNestingLevel = 0
    }

    override fun advance() {
      prevExpansionFormNestingLevel = flexLexer.expansionFormNestingLevel
      super.advance()
    }

    override fun getMergeFunction(): MergeFunction {
      return MergeFunction { type, originalLexer -> merge(type, originalLexer) }
    }

    private val flexLexer: _Angular2HtmlLexer
      get() = (original as FlexAdapter).flex as _Angular2HtmlLexer

    protected open fun merge(type: IElementType?, originalLexer: Lexer): IElementType? {
      var result = type
      val next = originalLexer.tokenType
      if (result === Angular2HtmlTokenTypes.INTERPOLATION_START
          && next !== Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR
          && next !== Angular2HtmlTokenTypes.INTERPOLATION_END) {
        result = if (next === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
                     || next === XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER)
          XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
        else
          XmlTokenType.XML_DATA_CHARACTERS
      }
      if (!TOKENS_TO_MERGE.contains(result)) {
        return result
      }
      while (true) {
        val tokenType = originalLexer.tokenType
        if (tokenType !== result) {
          break
        }
        originalLexer.advance()
      }
      return result
    }

    companion object {
      fun isLexerWithinInterpolation(state: Int): Boolean {
        return (state and BASE_STATE_MASK) == _Angular2HtmlLexer.INTERPOLATION
      }

      fun isLexerWithinUnterminatedInterpolation(state: Int): Boolean {
        return (state and BASE_STATE_MASK) == _Angular2HtmlLexer.UNTERMINATED_INTERPOLATION
      }

      fun isLexerWithinExpansionForm(state: Int): Boolean {
        val toCheck = state and BASE_STATE_MASK
        return (toCheck == _Angular2HtmlLexer.EXPANSION_FORM_CONTENT
                || toCheck == _Angular2HtmlLexer.EXPANSION_FORM_CASE_END)
      }

      fun getBaseLexerState(state: Int): Int {
        return state and BASE_STATE_MASK
      }

    }
  }

  companion object {
    const val IS_WITHIN_EXPANSION_FORM_STATE = 0x1 shl 28

    private val TOKENS_TO_MERGE = TokenSet.create(XmlTokenType.XML_COMMENT_CHARACTERS, XmlTokenType.XML_WHITE_SPACE,
                                                  XmlTokenType.XML_REAL_WHITE_SPACE,
                                                  XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, XmlTokenType.XML_DATA_CHARACTERS,
                                                  XmlTokenType.XML_TAG_CHARACTERS)
  }
}