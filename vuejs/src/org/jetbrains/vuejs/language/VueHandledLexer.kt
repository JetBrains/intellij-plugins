package org.jetbrains.vuejs.language

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.LanguageHtmlScriptContentProvider
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.openapi.fileTypes.FileTypeManager

interface VueHandledLexer {
  fun seenScript():Boolean
  fun setSeenScript()
  fun setSeenScriptType()
  fun seenTemplate(): Boolean
  fun setSeenTemplate(template: Boolean)
  fun seenStyle():Boolean
  fun setSeenStyleType()
  fun seenTag():Boolean
  fun setSeenTag(tag:Boolean)
  fun inTagState():Boolean
  fun seenAttribute():Boolean
  fun setSeenAttribute(attribute:Boolean)
  fun getScriptType(): String?
  fun getStyleType(): String?

  fun scriptContentViaLang(): HtmlScriptContentProvider? {
    return Language.getRegisteredLanguages()
      .filter { languageMatches(it) }
      .map { LanguageHtmlScriptContentProvider.getScriptContentProvider(it) }
      .firstOrNull { it != null }
  }

  fun languageMatches(language: Language): Boolean {
    val scriptType = getScriptType() ?: return false
    if (scriptType.equals(language.id, ignoreCase = true)) {
      return true
    }
    val fileType = FileTypeManager.getInstance().getFileTypeByExtension(scriptType)
    return fileType == language.associatedFileType
  }

  fun styleViaLang(default: Language?): Language? = Companion.styleViaLang(default, getStyleType())

  fun findScriptContentProviderVue(mimeType: String?, delegate: (String) -> HtmlScriptContentProvider?,
                                languageLevel: JSLanguageLevel): HtmlScriptContentProvider? {
    if (mimeType != null) {
      return delegate(mimeType) ?: scriptContentViaLang()
    }
    else {
      return LanguageHtmlScriptContentProvider.getScriptContentProvider(languageLevel.dialect)
    }
  }

  companion object {
    fun styleViaLang(default: Language?, style: String?): Language? {
      if (default != null && style != null) {
        default.dialects
          .filter { style.equals(it.id, ignoreCase = true) }
          .forEach { return it }
      }
      return if (style == null) Language.findLanguageByID("PostCSS") else null
    }
  }
}