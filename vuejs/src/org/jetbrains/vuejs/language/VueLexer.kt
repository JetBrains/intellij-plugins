package org.jetbrains.vuejs.language

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.HtmlLexer
import com.intellij.lexer._HtmlLexer
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType

class VueLexer : HtmlLexer(), VueHandledLexer {
  private var seenTemplate: Boolean = false

  init {
    registerHandler(XmlTokenType.XML_NAME, VueLangAttributeHandler())
    registerHandler(XmlTokenType.XML_NAME, VueTemplateTagHandler())
    registerHandler(XmlTokenType.XML_NAME, VueAttributesHandler())
    registerHandler(XmlTokenType.XML_TAG_END, VueTagClosedHandler())
    val scriptCleaner = VueTemplateCleaner()
    registerHandler(XmlTokenType.XML_END_TAG_START, scriptCleaner)
    registerHandler(XmlTokenType.XML_EMPTY_ELEMENT_END, scriptCleaner)
  }

  override fun findScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? {
    val type = super.findScriptContentProvider(mimeType ?: "text/ecmascript-6")
    return type ?: scriptContentViaLang()
  }

  override fun getStyleLanguage(): Language? {
    return styleViaLang(ourDefaultStyleLanguage) ?: super.getStyleLanguage()
  }

  override fun seenScript() = seenScript
  override fun seenStyle()= seenStyle
  override fun seenTemplate() = seenTemplate
  override fun seenTag() = seenTag
  override fun seenAttribute() = seenAttribute
  override fun getScriptType() = scriptType
  override fun getStyleType() = styleType
  override fun inTagState(): Boolean = (state and HtmlHighlightingLexer.BASE_STATE_MASK) == _HtmlLexer.START_TAG_NAME

  override fun setSeenScriptType() {
    seenContentType = true
  }

  override fun setSeenScript() {
    seenScript = true
  }

  override fun setSeenStyleType() {
    seenStylesheetType = true
  }

  override fun setSeenTemplate(template:Boolean) {
    seenTemplate = template
  }

  override fun setSeenTag(tag: Boolean) {
    seenTag = tag
  }

  override fun setSeenAttribute(attribute: Boolean) {
    seenAttribute = attribute
  }

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    seenTemplate = initialState and VueTemplateTagHandler.SEEN_TEMPLATE != 0
    super.start(buffer, startOffset, endOffset, initialState)
  }

  override fun getState(): Int {
    val state = super.getState()
    return state or
      (if (seenTemplate) VueTemplateTagHandler.SEEN_TEMPLATE else 0)
  }

  override fun endOfTheEmbeddment(name:String?):Boolean {
    return super.endOfTheEmbeddment(name) ||
           seenTemplate && "template" == name
  }

  override fun getTokenType(): IElementType? {
    val type = super.getTokenType()
    if (seenAttribute && type == JSElementTypes.EMBEDDED_CONTENT) return VueElementTypes.EMBEDDED_JS
    return type
  }
}
