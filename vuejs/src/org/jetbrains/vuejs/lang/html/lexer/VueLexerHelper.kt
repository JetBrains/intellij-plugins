// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.lexer

import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.LanguageHtmlScriptContentProvider
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTokenType

class VueLexerHelper(private val handle: VueLexerHandle) {

  private var seenTemplate: Boolean = false

  init {
    handle.registerHandler(XmlTokenType.XML_NAME, VueLangAttributeHandler())
    handle.registerHandler(XmlTokenType.XML_NAME, VueTemplateTagHandler())
    handle.registerHandler(XmlTokenType.XML_TAG_END, VueTagClosedHandler())
    val scriptCleaner = VueTemplateCleaner()
    handle.registerHandler(XmlTokenType.XML_END_TAG_START, scriptCleaner)
    handle.registerHandler(XmlTokenType.XML_EMPTY_ELEMENT_END, scriptCleaner)
  }

  fun styleViaLang(default: Language?): Language? = styleViaLang(default, handle.styleType)

  fun findScriptContentProviderVue(mimeType: String?,
                                   delegate: (String) -> HtmlScriptContentProvider?,
                                   languageLevel: JSLanguageLevel): HtmlScriptContentProvider? {
    val provider: HtmlScriptContentProvider?
    if (mimeType != null) {
      provider = delegate(mimeType) ?: scriptContentViaLang()
    }
    else {
      provider = LanguageHtmlScriptContentProvider.getScriptContentProvider(languageLevel.dialect)
    }
    provider ?: return null
    val moduleType = JSElementTypes.toModuleContentType(provider.scriptElementType)
    if (provider.scriptElementType == moduleType) return provider
    return object : HtmlScriptContentProvider {
      override fun getScriptElementType(): IElementType = moduleType
      override fun getHighlightingLexer(): Lexer? = provider.highlightingLexer
    }
  }

  fun start(initialState: Int): Int {
    seenTemplate = (initialState and SEEN_TEMPLATE) != 0
    return initialState
  }

  fun getState(state: Int): Int {
    return state or when {
      seenTemplate -> SEEN_TEMPLATE
      else -> 0
    }
  }

  fun endOfTheEmbeddment(name: String?): Boolean {
    return seenTemplate && "template" == name
  }

  fun getTokenType(tokenType: IElementType?): IElementType? {
    if (seenTemplate && "html".equals(handle.scriptType, true)) {
      handle.seenScriptType = false
      handle.scriptType = null
      handle.seenScript = false
    }
    return tokenType
  }

  private fun scriptContentViaLang(): HtmlScriptContentProvider? {
    return Language.getRegisteredLanguages()
      .asSequence()
      .filter { languageMatches(it) }
      .map { LanguageHtmlScriptContentProvider.getScriptContentProvider(it) }
      .firstOrNull { it != null }
  }

  private fun languageMatches(language: Language): Boolean {
    val scriptType = handle.scriptType ?: return false
    if (scriptType.equals(language.id, ignoreCase = true)) {
      return true
    }
    val fileType = FileTypeManager.getInstance().getFileTypeByExtension(scriptType)
    return fileType === language.associatedFileType
  }

  companion object {
    private const val BASE_STATE_SHIFT = 14
    const val SEEN_TEMPLATE = 0x1 shl BASE_STATE_SHIFT

    fun styleViaLang(default: Language?, style: String?): Language? {
      if (default != null && style != null) {
        default.dialects
          .filter { style.equals(it.id, ignoreCase = true) }
          .forEach { return it }
      }
      return if (style == null) Language.findLanguageByID("PostCSS") else null
    }
  }

  inner class VueLangAttributeHandler : BaseHtmlLexer.TokenHandler {
    override fun handleElement(lexer: Lexer) {
      if (!handle.seenTag && !handle.inTagState) {
        if (handle.seenScript || seenTemplate) {
          if ("lang" == lexer.tokenText) {
            handle.seenScriptType = true
            handle.seenScript = true
          }
        }
        else if (handle.seenStyle) {
          if ("lang" == lexer.tokenText) {
            handle.seenStyleType = true
          }
        }
      }
    }
  }

  inner class VueTagClosedHandler : BaseHtmlLexer.TokenHandler {
    override fun handleElement(lexer: Lexer) {
      if (seenTemplate && handle.seenScript) {
        handle.seenTag = true
      }
    }
  }

  inner class VueTemplateCleaner : BaseHtmlLexer.TokenHandler {
    override fun handleElement(lexer: Lexer) {
      seenTemplate = false
    }
  }

  inner class VueTemplateTagHandler : BaseHtmlLexer.TokenHandler {
    override fun handleElement(lexer: Lexer) {
      if (!handle.seenTag && handle.inTagState && "template" == lexer.tokenText) {
        seenTemplate = true
      }
      if (!handle.inTagState && "template" == lexer.tokenText) {
        handle.seenTag = false
      }
    }
  }
}
