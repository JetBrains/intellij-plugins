// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.highlighting

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.*
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.annotations.NonNls
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.highlighting.VueJSSyntaxHighlighter
import org.jetbrains.vuejs.lang.html.lexer.VueLexerHandle
import org.jetbrains.vuejs.lang.html.lexer.VueLexerHelper

class VueHighlightingLexer(private val languageLevel: JSLanguageLevel) : HtmlHighlightingLexer() {
  companion object {
    @NonNls
    val EXPRESSION_WHITE_SPACE = IElementType("VueJS:EXPRESSION_WHITE_SPACE", VueJSLanguage.INSTANCE)
  }

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

    override fun registerHandler(elementType: IElementType, value: TokenHandler) {
      this@VueHighlightingLexer.registerHandler(elementType, value)
    }
  })

  init {
    registerHandler(XmlTokenType.XML_NAME, HtmlAttributeNameHandler())
  }

  override fun findScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? =
    helper.findScriptContentProviderVue(mimeType, { super.findScriptContentProvider(mimeType) }, languageLevel)

  override fun getStyleLanguage(): Language? = helper.styleViaLang(HtmlLexer.ourDefaultStyleLanguage) ?: super.getStyleLanguage()

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    super.start(buffer, startOffset, endOffset, helper.start(initialState))
  }

  override fun getState(): Int = helper.getState(super.getState())

  override fun endOfTheEmbeddment(name: String?): Boolean {
    return super.endOfTheEmbeddment(name) || helper.endOfTheEmbeddment(name)
  }

  override fun getTokenType(): IElementType? {
    val type = helper.getTokenType(super.getTokenType())
    if (type == XmlTokenType.TAG_WHITE_SPACE && baseState() == 0)
      return XmlTokenType.XML_REAL_WHITE_SPACE
    if (type === XmlTokenType.XML_WHITE_SPACE && hasSeenScript() && hasSeenAttribute())
      return EXPRESSION_WHITE_SPACE
    return type
  }

  override fun getInlineScriptHighlightingLexer(): Lexer? {
    return object : MergingLexerAdapterBase(VueJSSyntaxHighlighter().highlightingLexer) {
      override fun getMergeFunction(): MergeFunction {
        return MergeFunction { type, _ -> if (type === JSTokenTypes.WHITE_SPACE) EXPRESSION_WHITE_SPACE else type }
      }
    }
  }

  override fun createELLexer(newLexer: Lexer): Lexer? {
    return inlineScriptHighlightingLexer
  }

  private fun baseState() = state and (BASE_STATE_MASK or (0x1 shl BaseHtmlLexer.BASE_STATE_SHIFT))

  private inner class HtmlAttributeNameHandler : TokenHandler {
    override fun handleElement(lexer: Lexer) {
      if (lexer.state and BaseHtmlLexer.BASE_STATE_MASK == _HtmlLexer.TAG_ATTRIBUTES) {
        val info = VueAttributeNameParser.parse(tokenText, null)
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
}

