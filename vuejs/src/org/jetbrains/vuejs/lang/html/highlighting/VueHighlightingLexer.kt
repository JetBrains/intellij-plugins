// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.highlighting

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.*
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType
import com.intellij.psi.xml.XmlTokenType.XML_REAL_WHITE_SPACE
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.lang.expr.highlighting.VueJSSyntaxHighlighter
import org.jetbrains.vuejs.lang.html.lexer.VueLexer
import org.jetbrains.vuejs.lang.html.lexer.VueLexer.VueMergingLexer.Companion.getBaseLexerState
import org.jetbrains.vuejs.lang.html.lexer.VueLexer.VueMergingLexer.Companion.isLexerWithinUnterminatedInterpolation
import org.jetbrains.vuejs.lang.html.lexer.VueLexerHandle
import org.jetbrains.vuejs.lang.html.lexer.VueLexerHelper
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_EXPR
import org.jetbrains.vuejs.lang.html.lexer._VueLexer

class VueHighlightingLexer(private val languageLevel: JSLanguageLevel,
                           private val interpolationConfig: Pair<String, String>?)
  : HtmlHighlightingLexer(VueHighlightingMergingLexer(FlexAdapter(_VueLexer(interpolationConfig))),
                          true, null) {

  private val helper: VueLexerHelper = VueLexerHelper(object : VueLexerHandle {

    override var scriptType: String?
      get() = this@VueHighlightingLexer.scriptType
      set(value) {
        this@VueHighlightingLexer.scriptType = value
      }

    override var seenTag: Boolean
      get() = this@VueHighlightingLexer.seenTag
      set(value) {
        this@VueHighlightingLexer.seenTag = value
      }

    override var seenStyleType: Boolean
      get() = this@VueHighlightingLexer.seenStylesheetType
      set(value) {
        this@VueHighlightingLexer.seenStylesheetType = value
      }

    override var seenScriptType: Boolean
      get() = this@VueHighlightingLexer.seenContentType
      set(value) {
        this@VueHighlightingLexer.seenContentType = value
      }
    override var seenScript: Boolean
      get() = this@VueHighlightingLexer.seenScript
      set(value) {
        this@VueHighlightingLexer.seenScript = value
      }

    override val seenStyle: Boolean get() = this@VueHighlightingLexer.seenStyle
    override val styleType: String? get() = this@VueHighlightingLexer.styleType
    override val inTagState: Boolean get() = baseState() == _HtmlLexer.START_TAG_NAME
    override val interpolationConfig: Pair<String, String>? get() = this@VueHighlightingLexer.interpolationConfig

    override fun registerHandler(elementType: IElementType, value: TokenHandler) {
      this@VueHighlightingLexer.registerHandler(elementType, value)
    }
  })

  init {
    registerHandler(INTERPOLATION_EXPR, ElEmbeddmentHandler())
    registerHandler(XmlTokenType.XML_NAME, HtmlAttributeNameHandler())
  }

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

  override fun endOfTheEmbeddment(name: String?): Boolean {
    return super.endOfTheEmbeddment(name) || helper.endOfTheEmbeddment(name)
  }

  override fun getTokenType(): IElementType? {
    val type = helper.getTokenType(super.getTokenType())
    if ((type === XmlTokenType.TAG_WHITE_SPACE
         && (getBaseLexerState(state) == 0 || isLexerWithinUnterminatedInterpolation(state)))) {
      return XML_REAL_WHITE_SPACE
    }
    return type
  }

  override fun getInlineScriptHighlightingLexer(): Lexer? {
    return VueJSSyntaxHighlighter().highlightingLexer
  }

  override fun createELLexer(newLexer: Lexer?): Lexer? {
    return inlineScriptHighlightingLexer
  }

  private fun baseState() = state and (BASE_STATE_MASK or (0x1 shl BaseHtmlLexer.BASE_STATE_SHIFT))

  private inner class HtmlAttributeNameHandler : TokenHandler {
    override fun handleElement(lexer: Lexer) {
      if (lexer.state and BaseHtmlLexer.BASE_STATE_MASK == _HtmlLexer.TAG_ATTRIBUTES) {
        val info = VueAttributeNameParser.parse(tokenText)
        if (info.injectJS) {
          if (seenAttribute) {
            popScriptStyle()
          }
          pushScriptStyle(true, false)
          seenAttribute = true
        }
      }
    }
  }

  private class VueHighlightingMergingLexer internal constructor(original: FlexAdapter)
    : VueLexer.VueMergingLexer(original) {

    override fun merge(type: IElementType?, originalLexer: Lexer): IElementType? {
      val tokenType = super.merge(type, originalLexer)
      if (tokenType === XmlTokenType.XML_CHAR_ENTITY_REF) {
        while (originalLexer.tokenType === XmlTokenType.XML_CHAR_ENTITY_REF) {
          originalLexer.advance()
        }
        if (originalLexer.tokenType === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) {
          return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
        }
      }
      return tokenType
    }
  }

}

