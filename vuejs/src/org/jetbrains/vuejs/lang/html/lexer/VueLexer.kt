// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.lexer

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.*
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType.*
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_END
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_EXPR
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_START

class VueLexer(private val languageLevel: JSLanguageLevel, private val interpolationConfig: Pair<String, String>?)
  : HtmlLexer(VueMergingLexer(FlexAdapter(_VueLexer(interpolationConfig))), true) {

  private val helper: VueLexerHelper = VueLexerHelper(object : VueLexerHandle {

    override var scriptType: String?
      get() = this@VueLexer.scriptType
      set(value) {
        this@VueLexer.scriptType = value
      }

    override var seenTag: Boolean
      get() = this@VueLexer.seenTag
      set(value) {
        this@VueLexer.seenTag = value
      }

    override var seenStyleType: Boolean
      get() = this@VueLexer.seenStylesheetType
      set(value) {
        this@VueLexer.seenStylesheetType = value
      }

    override var seenScriptType: Boolean
      get() = this@VueLexer.seenContentType
      set(value) {
        this@VueLexer.seenContentType = value
      }
    override var seenScript: Boolean
      get() = this@VueLexer.seenScript
      set(value) {
        this@VueLexer.seenScript = value
      }

    override val seenStyle: Boolean get() = this@VueLexer.seenStyle
    override val styleType: String? get() = this@VueLexer.styleType
    override val inTagState: Boolean get() = (state and HtmlHighlightingLexer.BASE_STATE_MASK) == _VueLexer.START_TAG_NAME
    override val interpolationConfig: Pair<String, String>? get() = this@VueLexer.interpolationConfig

    override fun registerHandler(elementType: IElementType, value: TokenHandler) {
      this@VueLexer.registerHandler(elementType, value)
    }
  })

  override fun findScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? =
    helper.findScriptContentProviderVue(mimeType, { super.findScriptContentProvider(mimeType) }, languageLevel)

  override fun getStyleLanguage(): Language? = helper.styleViaLang(CSSLanguage.INSTANCE) ?: super.getStyleLanguage()

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    super.start(buffer, startOffset, endOffset, helper.start(initialState))
  }

  override fun isHtmlTagState(state: Int): Boolean {
    return state == _VueLexer.START_TAG_NAME || state == _VueLexer.END_TAG_NAME
  }

  override fun getState(): Int = helper.getState(super.getState())

  override fun getTokenType(): IElementType? = helper.getTokenType(super.getTokenType())

  override fun endOfTheEmbeddment(name: String?): Boolean {
    return super.endOfTheEmbeddment(name) || helper.endOfTheEmbeddment(name)
  }

  open class VueMergingLexer(original: FlexAdapter) : MergingLexerAdapterBase(original) {

    override fun getMergeFunction(): MergeFunction {
      return MergeFunction { type, originalLexer -> this.merge(type, originalLexer) }
    }

    protected open fun merge(type: IElementType?, originalLexer: Lexer): IElementType? {
      var tokenType = type
      val next = originalLexer.tokenType
      if (tokenType === INTERPOLATION_START
          && next !== INTERPOLATION_EXPR
          && next !== INTERPOLATION_END) {
        tokenType = if (next === XML_ATTRIBUTE_VALUE_TOKEN || next === XML_ATTRIBUTE_VALUE_END_DELIMITER)
          XML_ATTRIBUTE_VALUE_TOKEN
        else
          XML_DATA_CHARACTERS
      }
      if (!TOKENS_TO_MERGE.contains(tokenType)) {
        return tokenType
      }
      while (true) {
        val nextTokenType = originalLexer.tokenType
        if (nextTokenType !== tokenType) {
          break
        }
        originalLexer.advance()
      }
      return tokenType
    }

    companion object {

      private val TOKENS_TO_MERGE = TokenSet.create(XML_COMMENT_CHARACTERS, XML_WHITE_SPACE, XML_REAL_WHITE_SPACE,
                                                    XML_ATTRIBUTE_VALUE_TOKEN, XML_DATA_CHARACTERS, XML_TAG_CHARACTERS)

      fun isLexerWithinUnterminatedInterpolation(state: Int): Boolean {
        return getBaseLexerState(state) == _VueLexer.UNTERMINATED_INTERPOLATION
      }

      fun getBaseLexerState(state: Int): Int {
        return state and BaseHtmlLexer.BASE_STATE_MASK
      }
    }
  }
}

