// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.language

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.LanguageHtmlScriptContentProvider
import com.intellij.lang.javascript.JSElementTypes.toModuleContentType
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.psi.tree.IElementType

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
  fun seenVueAttribute(): Boolean
  fun setSeenVueAttribute(value: Boolean)
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
    val provider: HtmlScriptContentProvider?
    if (mimeType != null) {
      provider = delegate(mimeType) ?: scriptContentViaLang()
    }
    else {
      provider = LanguageHtmlScriptContentProvider.getScriptContentProvider(languageLevel.dialect)
    }
    provider ?: return null
    val moduleType = toModuleContentType(provider.scriptElementType)
    if (provider.scriptElementType == moduleType) return provider
    return object: HtmlScriptContentProvider {
      override fun getScriptElementType(): IElementType = moduleType
      override fun getHighlightingLexer(): Lexer? = provider.highlightingLexer
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