// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.lexer

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptHighlightingLexer
import com.intellij.lexer.*
import com.intellij.openapi.util.Pair
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType
import org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType

open class Angular2HtmlLexer(
  highlightMode: Boolean,
  templateSyntax: Angular2TemplateSyntax,
  interpolationConfig: Pair<String, String>?,
)
  : HtmlLexer(Angular2HtmlMergingLexer(Angular2HtmlFlexAdapter(templateSyntax, interpolationConfig), highlightMode),
              true, highlightMode) {

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
           && state != _Angular2HtmlLexer.BLOCK_NAME
           && state != _Angular2HtmlLexer.BLOCK_PARAMETER
           && state != _Angular2HtmlLexer.BLOCK_PARAMETER_END
           && state != _Angular2HtmlLexer.BLOCK_PARAMETERS_START
  }

  override fun isHtmlTagState(state: Int): Boolean {
    return state == _Angular2HtmlLexer.START_TAG_NAME || state == _Angular2HtmlLexer.END_TAG_NAME
  }

  override fun getTokenType(): IElementType? {
    val tokenType = super.getTokenType()
    if (!isHighlighting) return tokenType
    val state = state
    // we need to convert attribute names according to their function
    if (tokenType === XmlTokenType.XML_NAME && state and BASE_STATE_MASK == _Angular2HtmlLexer.TAG_ATTRIBUTES) {
      val info = Angular2AttributeNameParser.parse(tokenText)
      if (info.type != Angular2AttributeType.REGULAR
          && Angular2HtmlEmbeddedContentSupport.Holder.NG_EL_ATTRIBUTES.contains(info.type)) {
        return info.type.elementType
      }
    }
    else if (tokenType != null && Angular2HtmlMergingLexer.isLexerWithinExpansionForm(state)) {
      if (tokenType === XmlTokenType.TAG_WHITE_SPACE
          || tokenType === XmlTokenType.XML_REAL_WHITE_SPACE
          || tokenType === JSTokenTypes.IDENTIFIER
          || tokenType === XmlTokenType.XML_DATA_CHARACTERS) {
        return Angular2HtmlTokenTypes.EXPANSION_FORM_CONTENT
      }
      else if (tokenType === XmlTokenType.XML_COMMA) {
        return Angular2HtmlTokenTypes.EXPANSION_FORM_COMMA
      }
    }
    else if ((tokenType === XmlTokenType.TAG_WHITE_SPACE && Angular2HtmlMergingLexer.isLexerWithinInterpolation(state))
             || (tokenType === XmlTokenType.XML_WHITE_SPACE && embeddedLexer is JavaScriptHighlightingLexer)) {
      return Angular2HtmlTokenTypes.EXPRESSION_WHITE_SPACE
    }
    else if (tokenType === XmlTokenType.TAG_WHITE_SPACE
             && (Angular2HtmlMergingLexer.getBaseLexerState(state) == 0
                 || Angular2HtmlMergingLexer.isLexerWithinUnterminatedInterpolation(state))) {
      return XmlTokenType.XML_REAL_WHITE_SPACE
    }
    else if (tokenType === JSTokenTypes.STRING_LITERAL_PART) {
      return JSTokenTypes.STRING_LITERAL
    }
    return tokenType
  }

  open class Angular2HtmlMergingLexer(original: FlexAdapter, private val highlightMode: Boolean) : MergingLexerAdapterBase(original) {

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
      if (TOKENS_TO_MERGE.contains(result)) {
        while (true) {
          val tokenType = originalLexer.tokenType
          if (tokenType !== result) {
            break
          }
          originalLexer.advance()
        }
      }
      if (highlightMode && result === XmlTokenType.XML_CHAR_ENTITY_REF) {
        while (originalLexer.tokenType === XmlTokenType.XML_CHAR_ENTITY_REF) {
          originalLexer.advance()
        }
        if (originalLexer.tokenType === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) {
          return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
        }
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

  private class Angular2HtmlFlexAdapter(templateSyntax: Angular2TemplateSyntax, interpolationConfig: Pair<String, String>?)
    : FlexAdapter(_Angular2HtmlLexer(templateSyntax, interpolationConfig)) {

    private val flex get() = (super.getFlex() as _Angular2HtmlLexer)

    override fun getCurrentPosition(): LexerPosition =
      flex.let {
        Angular2HtmlFlexAdapterPosition(tokenStart, super.getState(), it.blockName, it.parameterIndex, it.parameterStart,
                                        it.blockParenLevel, it.expansionFormNestingLevel, it.interpolationStartPos)
      }

    override fun restore(position: LexerPosition) {
      flex.apply {
        blockName = (position as Angular2HtmlFlexAdapterPosition).blockName
        parameterIndex = position.parameterIndex
        parameterStart = position.parameterStart
        blockParenLevel = position.blockParenLevel
        expansionFormNestingLevel = position.expansionFormNestingLevel
        interpolationStartPos = position.interpolationStartPos
      }
      super.start(bufferSequence, position.offset, bufferEnd, position.state)
    }

    private class Angular2HtmlFlexAdapterPosition(
      private val offset: Int,
      private val state: Int,
      val blockName: String?,
      val parameterIndex: Int,
      val parameterStart: Int,
      val blockParenLevel: Int,
      val expansionFormNestingLevel: Int,
      val interpolationStartPos: Int,
    ) : LexerPosition {
      override fun getOffset(): Int = offset

      override fun getState(): Int = state

    }
  }

  companion object {
    const val IS_WITHIN_EXPANSION_FORM_STATE: Int = 0x1 shl 28

    private val TOKENS_TO_MERGE = TokenSet.create(XmlTokenType.XML_COMMENT_CHARACTERS, XmlTokenType.XML_WHITE_SPACE,
                                                  XmlTokenType.XML_REAL_WHITE_SPACE,
                                                  XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, XmlTokenType.XML_DATA_CHARACTERS,
                                                  XmlTokenType.XML_TAG_CHARACTERS)

  }
}