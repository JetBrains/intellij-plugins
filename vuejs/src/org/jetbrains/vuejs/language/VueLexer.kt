package org.jetbrains.vuejs.language

import com.intellij.lang.Language
import com.intellij.lang.LanguageHtmlScriptContentProvider
import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.HtmlLexer
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType

class VueLexer : HtmlLexer(), VueHandledLexer {
  init {
    registerHandler(XmlTokenType.XML_NAME, VueLangAttributeHandler())
  }

  override fun getCurrentScriptElementType(): IElementType? {
    if (scriptType == null) {
      val provider = BaseHtmlLexer.findScriptContentProvider("text/ecmascript-6")
      return provider?.scriptElementType
    }
    val type = super.getCurrentScriptElementType()
    if (type != null) return type
    for (language in Language.getRegisteredLanguages()) {
      if (scriptType!!.equals(language.id, ignoreCase = true)) {
        val scriptContentProvider = LanguageHtmlScriptContentProvider.getScriptContentProvider(language)
        if (scriptContentProvider != null) {
          return scriptContentProvider.scriptElementType
        }
      }
    }
    return null
  }

  override fun getStyleLanguage(): Language? {
    if (ourDefaultStyleLanguage != null && styleType != null) {
      for (language in ourDefaultStyleLanguage!!.dialects) {
        if (styleType.equals(language.id, ignoreCase = true)) {
          return language
        }
      }
    }
    return super.getStyleLanguage()
  }

  override fun seenScript(): Boolean {
    return seenScript
  }

  override fun seenStyle(): Boolean {
    return seenStyle
  }

  override fun setSeenScriptType() {
    seenContentType = true
  }

  override fun setSeenStyleType() {
    seenStylesheetType = true
  }

  override fun seenTag(): Boolean {
    return seenTag
  }
}
