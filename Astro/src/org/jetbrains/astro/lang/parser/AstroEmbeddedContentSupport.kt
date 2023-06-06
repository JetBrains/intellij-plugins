// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.parser

import com.intellij.html.embedding.HtmlEmbeddedContentProvider
import com.intellij.html.embedding.HtmlEmbeddedContentSupport
import com.intellij.html.embedding.HtmlEmbeddedContentSupport.Companion.getStyleTagEmbedmentInfo
import com.intellij.html.embedding.HtmlEmbedmentInfo
import com.intellij.html.embedding.HtmlTagEmbeddedContentProvider
import com.intellij.lang.Language
import com.intellij.lang.css.CSSLanguage
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lexer.BaseHtmlLexer
import com.intellij.xml.util.HtmlUtil
import org.intellij.plugins.postcss.PostCssLanguage
import org.jetbrains.astro.codeInsight.ASTRO_INLINE_DIRECTIVE
import org.jetbrains.astro.lang.highlighting.AstroHighlightingLexer
import org.jetbrains.astro.lang.lexer.AstroLexerImpl

class AstroEmbeddedContentSupport : HtmlEmbeddedContentSupport {
  override fun isEnabled(lexer: BaseHtmlLexer): Boolean = lexer is AstroLexerImpl || lexer is AstroHighlightingLexer

  override fun createEmbeddedContentProviders(lexer: BaseHtmlLexer): List<HtmlEmbeddedContentProvider> =
    listOf(AstroTagEmbeddedContentProvider(lexer))
}

class AstroTagEmbeddedContentProvider(lexer: BaseHtmlLexer) : HtmlTagEmbeddedContentProvider(lexer) {
  private val interestingTags: List<String> = listOf(HtmlUtil.SCRIPT_TAG_NAME, HtmlUtil.STYLE_TAG_NAME)

  override fun isInterestedInTag(tagName: CharSequence): Boolean =
    interestingTags.any { namesEqual(tagName, it) }

  override fun isInterestedInAttribute(attributeName: CharSequence): Boolean =
    namesEqual(tagName, HtmlUtil.SCRIPT_TAG_NAME) &&
    (namesEqual(attributeName, ASTRO_INLINE_DIRECTIVE) || namesEqual(attributeName, HtmlUtil.TYPE_ATTRIBUTE_NAME))

  override fun createEmbedmentInfo(): HtmlEmbedmentInfo? {
    val tagName = tagName ?: return null
    val attributeValue = attributeValue?.trim()?.toString()
    return when {
      namesEqual(tagName, HtmlUtil.STYLE_TAG_NAME) -> styleLanguage(attributeValue)?.let { getStyleTagEmbedmentInfo(it) }
                                                      ?: HtmlEmbeddedContentProvider.RAW_TEXT_EMBEDMENT
      namesEqual(tagName, HtmlUtil.SCRIPT_TAG_NAME) -> createScriptEmbedmentInfo()
      else -> null
    }
  }

  private fun createScriptEmbedmentInfo(): HtmlEmbedmentInfo? {
    val attributeName = attributeName?.trim()
    return when {
      namesEqual(attributeName, ASTRO_INLINE_DIRECTIVE) ->
        HtmlEmbeddedContentSupport.getScriptTagEmbedmentInfo(JavaScriptSupportLoader.ECMA_SCRIPT_6)
      namesEqual(attributeName, HtmlUtil.TYPE_ATTRIBUTE_NAME) -> {
        val language = attributeValue?.trim()?.toString() ?: return null
        Language.findInstancesByMimeType(language)
          .asSequence()
          .plus(Language.findInstancesByMimeType("text/$language"))
          .map {
            HtmlEmbeddedContentSupport.getScriptTagEmbedmentInfo(it)
          }.firstOrNull()
      }
      else ->
        HtmlEmbeddedContentSupport.getScriptTagEmbedmentInfo(JavaScriptSupportLoader.TYPESCRIPT)
    }
  }

  companion object {
    fun styleLanguage(styleLang: String?): Language? {
      val cssLanguage = CSSLanguage.INSTANCE
      if (styleLang != null) {
        if (styleLang.equals("text/css", ignoreCase = true)) return cssLanguage
        cssLanguage
          .dialects
          .firstOrNull { dialect ->
            dialect.id.equals(styleLang, ignoreCase = true)
            || dialect.mimeTypes.any { it.equals(styleLang, ignoreCase = true) }
          }
          ?.let { return it }
      }
      return PostCssLanguage.INSTANCE
    }
  }
}