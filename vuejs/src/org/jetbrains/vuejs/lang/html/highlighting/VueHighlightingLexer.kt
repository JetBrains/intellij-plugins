// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.highlighting

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer._HtmlLexer
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.vuejs.lang.expr.VueElementTypes
import org.jetbrains.vuejs.lang.html.lexer.*

class VueHighlightingLexer(private val languageLevel: JSLanguageLevel) : HtmlHighlightingLexer(), VueHandledLexer {
  private var seenTemplate: Boolean = false
  private var seenVueAttribute: Boolean = false

  init {
    registerHandler(XmlTokenType.XML_NAME, VueLangAttributeHandler())
    registerHandler(XmlTokenType.XML_NAME, VueTemplateTagHandler())
    registerHandler(XmlTokenType.XML_NAME, VueAttributesHandler())
    registerHandler(XmlTokenType.XML_TAG_END, VueTagClosedHandler())
    val scriptCleaner = VueTemplateCleaner()
    registerHandler(XmlTokenType.XML_END_TAG_START, scriptCleaner)
    registerHandler(XmlTokenType.XML_EMPTY_ELEMENT_END, scriptCleaner)
  }

  override fun getTokenType(): IElementType? {
    val type = super.getTokenType()
    if (type == XmlTokenType.TAG_WHITE_SPACE && baseState() == 0) return XmlTokenType.XML_REAL_WHITE_SPACE
    if (seenVueAttribute && type == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) return VueElementTypes.EMBEDDED_JS
    return type
  }

  override fun findScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? =
    findScriptContentProviderVue(mimeType, { super.findScriptContentProvider(mimeType) }, languageLevel)

  override fun getStyleLanguage(): Language? {
    return styleViaLang(ourDefaultStyleLanguage) ?: super.getStyleLanguage()
  }

  override fun seenScript(): Boolean = seenScript
  override fun seenStyle(): Boolean = seenStyle
  override fun seenTemplate(): Boolean = seenTemplate
  override fun seenTag(): Boolean = seenTag
  override fun seenAttribute(): Boolean = seenAttribute
  override fun seenVueAttribute(): Boolean = seenVueAttribute
  override fun getScriptType(): String? = scriptType
  override fun getStyleType(): String? = styleType
  override fun inTagState(): Boolean = baseState() == _HtmlLexer.START_TAG_NAME

  override fun setSeenScriptType() {
    seenContentType = true
  }

  override fun setSeenScript() {
    seenScript = true
  }

  override fun setSeenStyleType() {
    seenStylesheetType = true
  }

  override fun setSeenTemplate(template: Boolean) {
    seenTemplate = template
  }

  override fun setSeenTag(tag: Boolean) {
    seenTag = tag
  }

  override fun setSeenAttribute(attribute: Boolean) {
    seenAttribute = attribute
  }

  override fun setSeenVueAttribute(value: Boolean) {
    seenVueAttribute = value
  }

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    seenTemplate = initialState and VueTemplateTagHandler.SEEN_TEMPLATE != 0
    seenVueAttribute = initialState and VueLexer.SEEN_VUE_ATTRIBUTE != 0
    super.start(buffer, startOffset, endOffset, initialState)
  }

  override fun getState(): Int {
    val state = super.getState()
    return state or when {
      seenTemplate -> VueTemplateTagHandler.SEEN_TEMPLATE
      seenVueAttribute -> VueLexer.SEEN_VUE_ATTRIBUTE
      else -> 0
    }
  }

  override fun endOfTheEmbeddment(name: String?): Boolean {
    return super.endOfTheEmbeddment(name) ||
           seenTemplate && "template" == name
  }

  private fun baseState() = state and BASE_STATE_MASK
}

