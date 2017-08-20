package org.jetbrains.vuejs.language

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.lexer.HtmlLexer
import com.intellij.lexer.Lexer
import com.intellij.lexer._HtmlLexer
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType

class VueLexer(private val languageLevel: JSLanguageLevel) : HtmlLexer(), VueHandledLexer {
//  companion object {
//    val SEEN_INTERPOLATION:Int = 0x1000
//  }

  private var interpolationLexer: Lexer? = null
  private var interpolationStart = -1
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

  override fun findScriptContentProvider(mimeType: String?): HtmlScriptContentProvider? =
    findScriptContentProviderVue(mimeType, { super.findScriptContentProvider(mimeType) }, languageLevel)

  override fun getStyleLanguage(): Language? = styleViaLang(ourDefaultStyleLanguage) ?: super.getStyleLanguage()

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
    interpolationLexer = null
    interpolationStart = -1
    super.start(buffer, startOffset, endOffset, initialState)
  }

  override fun getState(): Int {
    val state = super.getState()
    return state or
      (if (seenTemplate) VueTemplateTagHandler.SEEN_TEMPLATE else 0)
//     or (if (interpolationLexer != null) SEEN_INTERPOLATION else 0)
  }

  override fun endOfTheEmbeddment(name:String?):Boolean {
    return super.endOfTheEmbeddment(name) ||
           seenTemplate && "template" == name
  }

  override fun getTokenType(): IElementType? {
    if (seenTemplate && "html".equals(scriptType, true)) {
      seenContentType = false
      scriptType = null
      seenScript = false
    }
    val type = super.getTokenType()
    if (seenAttribute && type == JSElementTypes.EMBEDDED_CONTENT) {
      return VueElementTypes.EMBEDDED_JS
    }
//    if (interpolationLexer != null) return interpolationLexer!!.tokenType
    return type
  }

  //  override fun getTokenStart(): Int {
//    if (interpolationLexer != null) {
//      return interpolationStart + interpolationLexer!!.tokenStart
//    }
//    return super.getTokenStart()
//  }
//
//  override fun getTokenEnd(): Int {
//    if (interpolationLexer != null) {
//      return interpolationStart + interpolationLexer!!.tokenEnd
//    }
//    return super.getTokenEnd()
//  }

//  override fun advance() {
//    if (interpolationLexer != null) {
//      interpolationLexer!!.advance()
//      try {
//        if (interpolationLexer!!.tokenType != null) {
//          return
//        }
//      }
//      catch (error: Error) {
//        Logger.getInstance(VueLexer::class.java).error(interpolationLexer!!.bufferSequence)
//      }
//
//      interpolationLexer = null
//      interpolationStart = -1
//      return
//    }
//    super.advance()
//    val originalType = super.getTokenType()
//    if (originalType === XmlTokenType.XML_DATA_CHARACTERS || originalType === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) {
//      var type: IElementType? = originalType!!
//      interpolationStart = super.getTokenStart()
//      val text = StringBuilder()
//      while (type === XmlTokenType.XML_DATA_CHARACTERS ||
//             type === XmlTokenType.XML_REAL_WHITE_SPACE ||
//             type === XmlTokenType.XML_WHITE_SPACE ||
//             type === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN ||
//             type === XmlTokenType.XML_CHAR_ENTITY_REF ||
//             type === XmlTokenType.XML_ENTITY_REF_TOKEN) {
//        text.append(super.getTokenText())
//        super.advance()
//        type = tokenType
//      }
//      val lexer = VueInterpolationLexer("{{", "}}", originalType)
//      lexer.start(text)
//      lexer.advance()
//      interpolationLexer = lexer
//    }
//  }
}
