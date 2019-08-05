// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.lexer

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.HtmlLexer
import com.intellij.lexer._HtmlLexer
import com.intellij.psi.tree.IElementType

class VueLexer(private val languageLevel: JSLanguageLevel) : HtmlLexer() {

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
    override val inTagState: Boolean get() = (state and HtmlHighlightingLexer.BASE_STATE_MASK) == _HtmlLexer.START_TAG_NAME

    override fun registerHandler(elementType: IElementType, value: TokenHandler) {
      this@VueLexer.registerHandler(elementType, value)
    }
  })

  override fun findScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? =
    helper.findScriptContentProviderVue(mimeType, { super.findScriptContentProvider(mimeType) }, languageLevel)

  override fun getStyleLanguage(): Language? = helper.styleViaLang(ourDefaultStyleLanguage) ?: super.getStyleLanguage()

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    super.start(buffer, startOffset, endOffset, helper.start(initialState))
  }

  override fun getState(): Int = helper.getState(super.getState())

  override fun getTokenType(): IElementType? = helper.getTokenType(super.getTokenType())

  override fun endOfTheEmbeddment(name: String?): Boolean {
    return super.endOfTheEmbeddment(name) || helper.endOfTheEmbeddment(name)
  }

}

