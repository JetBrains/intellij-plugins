package org.jetbrains.vuejs.language

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.LanguageHtmlScriptContentProvider

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
  fun getScriptType(): String?
  fun getStyleType(): String?

  fun scriptContentViaLang(): HtmlScriptContentProvider? {
    return Language.getRegisteredLanguages()
      .filter { getScriptType()!!.equals(it.id, ignoreCase = true) ||
                (getScriptType()!!.equals("pug", ignoreCase = true) && "Jade" == it.id)}
      .map { LanguageHtmlScriptContentProvider.getScriptContentProvider(it) }
      .firstOrNull { it != null }
  }

  fun styleViaLang(default: Language?): Language? {
    if (default != null && getStyleType() != null) {
      default.dialects
        .filter { getStyleType().equals(it.id, ignoreCase = true) }
        .forEach { return it }
    }
    return null
  }

}