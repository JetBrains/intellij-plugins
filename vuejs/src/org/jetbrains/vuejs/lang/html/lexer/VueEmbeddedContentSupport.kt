// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.lexer

import com.intellij.html.embedding.*
import com.intellij.html.embedding.HtmlEmbeddedContentSupport.Companion.getStyleTagEmbedmentInfo
import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlElementType.HTML_EMBEDDED_CONTENT
import com.intellij.psi.xml.XmlTokenType
import com.intellij.xml.util.HtmlUtil
import com.intellij.xml.util.HtmlUtil.TYPE_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.LANG_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.VueScriptLangs
import org.jetbrains.vuejs.lang.expr.parser.VueJSEmbeddedExprTokenType
import org.jetbrains.vuejs.lang.html.highlighting.VueHighlightingLexer
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_EXPR
import org.jetbrains.vuejs.lang.html.parser.VueElementTypes

class VueEmbeddedContentSupport : HtmlEmbeddedContentSupport {
  override fun isEnabled(lexer: BaseHtmlLexer): Boolean = lexer is VueLexer

  override fun createEmbeddedContentProviders(lexer: BaseHtmlLexer): List<HtmlEmbeddedContentProvider> =
    listOf(
      VueAttributeEmbeddedContentProvider(lexer),
      VueTagEmbeddedContentProvider(lexer),
      HtmlTokenEmbeddedContentProvider(
        lexer,
        INTERPOLATION_EXPR,
        { VueScriptLangs.createExprHighlightingLexer((lexer as VueLexer).langMode) },
        { VueJSEmbeddedExprTokenType.createInterpolationExpression((lexer as VueLexer).langMode, (lexer as VueLexer).project) }
      )
    )
}

class VueAttributeEmbeddedContentProvider(lexer: BaseHtmlLexer) : HtmlAttributeEmbeddedContentProvider(lexer) {

  private var injectEmpty: Boolean = false
  private val project get() = (lexer as VueLexer).project

  override fun handleToken(tokenType: IElementType, range: TextRange) {
    super.handleToken(tokenType, range)
    when (tokenType) {
      XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER -> injectEmpty = true
      XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER -> Unit
      else -> injectEmpty = false
    }
  }

  override fun isInterestedInTag(tagName: CharSequence): Boolean = true

  override fun isInterestedInAttribute(attributeName: CharSequence): Boolean = true

  override fun isStartOfEmbedment(tokenType: IElementType): Boolean =
    super.isStartOfEmbedment(tokenType)
    || (tagName != null && injectEmpty && tokenType == XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER)

  override fun createEmbedmentInfo(): HtmlEmbedmentInfo? =
    VueAttributeNameParser.parse(attributeName!!, tagName?.toString())
      .takeIf { it.injectJS }
      ?.let { attributeInfo ->
        injectEmpty = false
        VueEmbeddedExpressionInfo(attributeInfo, (lexer as VueLexer).langMode, project)
      }

  private class VueEmbeddedExpressionInfo(val attributeInfo: VueAttributeNameParser.VueAttributeInfo,
                                          val langMode: LangMode,
                                          val project: Project?) : HtmlEmbedmentInfo {
    override fun getElementType(): IElementType =
      VueJSEmbeddedExprTokenType.createEmbeddedExpression(attributeInfo, langMode, project)

    override fun createHighlightingLexer(): Lexer =
      VueScriptLangs.createExprHighlightingLexer(langMode)
  }
}

class VueTagEmbeddedContentProvider(lexer: BaseHtmlLexer) : HtmlTagEmbeddedContentProvider(lexer) {

  private val languageLevel get() = (lexer as VueLexer).languageLevel
  private val langMode get() = (lexer as VueLexer).langMode
  private val project get() = (lexer as VueLexer).project
  private val interpolationConfig get() = (lexer as VueLexer).interpolationConfig

  private val interestingTags: List<String> = listOf(HtmlUtil.TEMPLATE_TAG_NAME, HtmlUtil.SCRIPT_TAG_NAME, HtmlUtil.STYLE_TAG_NAME)

  override fun isInterestedInTag(tagName: CharSequence): Boolean =
    interestingTags.any { namesEqual(tagName, it) }

  override fun isInterestedInAttribute(attributeName: CharSequence): Boolean =
    namesEqual(attributeName, LANG_ATTRIBUTE_NAME)
    || (namesEqual(attributeName, TYPE_ATTRIBUTE_NAME) && namesEqual(tagName, HtmlUtil.SCRIPT_TAG_NAME))

  override fun createEmbedmentInfo(): HtmlEmbedmentInfo? {
    val tagName = tagName ?: return null
    val attributeName = attributeName?.trim()?.toString()
    val attributeValue = attributeValue?.trim()?.toString()
    return when {
      namesEqual(tagName, HtmlUtil.STYLE_TAG_NAME) -> styleLanguage(attributeValue)?.let { getStyleTagEmbedmentInfo(it) }
                                                      ?: HtmlEmbeddedContentProvider.RAW_TEXT_EMBEDMENT
      (attributeName == null || namesEqual(attributeName, LANG_ATTRIBUTE_NAME))
      && namesEqual(tagName, HtmlUtil.SCRIPT_TAG_NAME) -> getScriptLangTagInfo(attributeValue)
      namesEqual(tagName, HtmlUtil.SCRIPT_TAG_NAME)
      || namesEqual(tagName, HtmlUtil.TEMPLATE_TAG_NAME) -> getClassicScriptOrTemplateTagInfo(tagName, attributeValue)
      else -> null
    }
  }

  private fun getClassicScriptOrTemplateTagInfo(tagName: CharSequence, lang: String?): HtmlEmbedmentInfo? {
    if (namesEqual(tagName, HtmlUtil.TEMPLATE_TAG_NAME)) {
      if (lang == null || lang.equals("html", ignoreCase = true)) return null
    }

    return findEmbedmentInfo(lang)
    }


  private fun getScriptLangTagInfo(lang: String?): HtmlEmbedmentInfo {
    // lang can be null if the whole attribute is missing, or "lang" if the attribute value is missing
    if (!LangMode.knownAttrValues.contains(lang)) {
      // it's neither JS nor TS, can be either something custom, or unfinished typing
      // let's parse the script in a classic, local way, and template expressions will default to JS
      return findEmbedmentInfo(lang)
    }

    // otherwise, let's not mix JS & TS in one file
    if (lexer is VueLexerImpl) {
      // we're lexing for parsing
      val langMode = saveLangModeIfFirst(lang)
      return langMode.scriptEmbedmentInfo
    }
    else {
      // we're lexing for syntax highlighting
      return findEmbedmentInfo(langMode.canonicalAttrValue)
    }
  }

  private fun saveLangModeIfFirst(lang: String?): LangMode {
    val lexer = lexer as VueLexer
    if (lexer.lexedLangMode == LangMode.PENDING) {
      // It's the first eligible script, let's store the lang and use it everywhere
      lexer.lexedLangMode = LangMode.fromAttrValue(lang)
    }
    return lexer.lexedLangMode
  }

  class VueScriptEmbedmentInfo(private val elementType: IElementType) : HtmlEmbedmentInfo {
    override fun getElementType(): IElementType = elementType
    override fun createHighlightingLexer(): Lexer = error("VueEmbeddedContentSupport did something unexpected")
  }

  private fun findEmbedmentInfo(language: String?): HtmlEmbedmentInfo = when (language) {
    null -> {
      HtmlEmbeddedContentSupport.getScriptTagEmbedmentInfo(languageLevel.dialect)
    }
    "js" -> { // fast path + special case for VueParserTest
      HtmlEmbeddedContentSupport.getScriptTagEmbedmentInfo(JavaScriptSupportLoader.ECMA_SCRIPT_6)
    }
    "ts" -> { // fast path + special case for VueParserTest
      HtmlEmbeddedContentSupport.getScriptTagEmbedmentInfo(JavaScriptSupportLoader.TYPESCRIPT)
    }
    else -> {
      val languageSequence = Language.findInstancesByMimeType(language)
        .asSequence()
        .plus(Language.findInstancesByMimeType("text/$language"))
        .plus(
          Language.getRegisteredLanguages()
            .asSequence()
            .filter { languageMatches(language, it) }
        )
        .plus(if (StringUtil.containsIgnoreCase(language, "template")) listOf(HTMLLanguage.INSTANCE) else emptyList())

      languageSequence.map {
        HtmlEmbeddedContentSupport.getScriptTagEmbedmentInfo(it)
      }.firstOrNull()
    }
  }.let(::wrapEmbedmentInfo)

  private fun wrapEmbedmentInfo(embedmentInfo: HtmlEmbedmentInfo?): HtmlEmbedmentInfo {
    return when (val elementType = embedmentInfo?.getElementType()) {
      HTML_EMBEDDED_CONTENT -> object : HtmlEmbedmentInfo {
        override fun getElementType(): IElementType = VueElementTypes.VUE_EMBEDDED_CONTENT
        override fun createHighlightingLexer(): Lexer =
          VueHighlightingLexer(languageLevel, langMode, project, interpolationConfig)
      }
      null -> HtmlEmbeddedContentProvider.RAW_TEXT_EMBEDMENT
      else -> object : HtmlEmbedmentInfo {
        // JSElementTypes.toModuleContentType is significant for JSX/TSX
        override fun getElementType(): IElementType? = JSElementTypes.toModuleContentType(elementType)
        override fun createHighlightingLexer(): Lexer? = embedmentInfo.createHighlightingLexer()
      }
    }
  }

  private fun languageMatches(scriptType: String, language: Language): Boolean =
    scriptType.equals(language.id, ignoreCase = true)
    || FileTypeManager.getInstance().getFileTypeByExtension(scriptType) === language.associatedFileType


  companion object {
    fun styleLanguage(styleLang: String?): Language? {
      val cssLanguage = Language.findLanguageByID("CSS")
      if (styleLang != null) {
        if (styleLang.equals("text/css", ignoreCase = true)) return cssLanguage
        cssLanguage
          ?.dialects
          ?.firstOrNull { dialect ->
            dialect.id.equals(styleLang, ignoreCase = true)
            || dialect.mimeTypes.any { it.equals(styleLang, ignoreCase = true) }
          }
          ?.let { return it }
      }
      return Language.findLanguageByID("PostCSS") ?: cssLanguage
    }
  }

}