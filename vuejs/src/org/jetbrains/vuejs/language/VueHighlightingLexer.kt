package org.jetbrains.vuejs.language

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lexer.HtmlHighlightingLexer
import com.intellij.psi.xml.XmlTokenType

class VueHighlightingLexer : HtmlHighlightingLexer(), VueHandledLexer {
  init {
    registerHandler(XmlTokenType.XML_NAME, VueLangAttributeHandler())
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
  override fun seenTag() = seenTag
  override fun getScriptType() = scriptType
  override fun getStyleType() = styleType

  override fun setSeenScriptType() {
    seenContentType = true
  }

  override fun setSeenStyleType() {
    seenStylesheetType = true
  }
}