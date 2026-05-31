// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.lexer

import com.intellij.html.embedding.HtmlEmbeddedContentProvider
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.HtmlLexer
import com.intellij.lexer.HtmlScriptStyleEmbeddedContentProvider
import com.intellij.lexer.Lexer
import com.intellij.lexer.MergeFunction
import com.intellij.lexer.MergingLexerAdapterBase
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType
import org.jetbrains.vuejs.lang.LangMode

class VueLexer(
    val languageLevel: JSLanguageLevel,
    val project: Project?,
    val interpolationConfig: Pair<String, String>?,
    val htmlCompatMode: Boolean,
    highlightMode: Boolean,
    val langMode: LangMode = LangMode.PENDING,
) : HtmlLexer(VueMergingLexer(FlexAdapter(_VueLexer(interpolationConfig)), highlightMode), true, highlightMode) {

  var lexedLangMode: LangMode = LangMode.PENDING

  override fun isHtmlTagState(state: Int): Boolean {
    return state == _VueLexer.START_TAG_NAME || state == _VueLexer.END_TAG_NAME
  }

  override fun acceptEmbeddedContentProvider(provider: HtmlEmbeddedContentProvider): Boolean {
    return provider !is HtmlScriptStyleEmbeddedContentProvider
  }

  override fun createAttributeEmbedmentTokenSet(): TokenSet {
    return TokenSet.orSet(super.createAttributeEmbedmentTokenSet(), ATTRIBUTE_TOKENS)
  }

  override fun createTagEmbedmentStartTokenSet(): TokenSet {
    return TokenSet.orSet(super.createTagEmbedmentStartTokenSet(), TAG_TOKENS)
  }

  override fun isPossiblyCustomTagName(tagName: CharSequence): Boolean {
    return !htmlCompatMode && isPossiblyComponentTag(tagName)
  }


  override fun getTokenType(): IElementType? {
    val type = super.getTokenType()
    if (isHighlighting
        && (type === XmlTokenType.TAG_WHITE_SPACE && (VueMergingLexer.getBaseLexerState(state) == 0
                                         || VueMergingLexer.isLexerWithinUnterminatedInterpolation(state)))) {
      return XmlTokenType.XML_REAL_WHITE_SPACE
    }
    return type
  }

  companion object {
    internal val ATTRIBUTE_TOKENS = TokenSet.create(
        VueTokenTypes.INTERPOLATION_START,
        VueTokenTypes.INTERPOLATION_EXPR,
        VueTokenTypes.INTERPOLATION_END
    )
    internal val TAG_TOKENS = TokenSet.create(VueTokenTypes.INTERPOLATION_START)

    /**
    There are heavily-used Vue components called like 'Col', 'Input' or 'Title'.
    Unlike HTML tags <col> and <input> Vue components do have closing tags and <Title> component can have content.
    This condition is a little bit hacky but it's rather tricky to solve the problem in a better way at parser level.
     */
    fun isPossiblyComponentTag(tagName: CharSequence): Boolean =
      tagName.length >= 3
      && tagName[0].isUpperCase()
      && !tagName.all { it.isUpperCase() }

  }

  private class VueMergingLexer(original: FlexAdapter, private val highlightMode: Boolean) : MergingLexerAdapterBase(original) {

    override fun getMergeFunction(): MergeFunction {
      return MergeFunction { type, originalLexer -> this.merge(type, originalLexer) }
    }

    private fun merge(type: IElementType?, originalLexer: Lexer): IElementType? {
      var tokenType = type
      val next = originalLexer.tokenType
      if (tokenType === VueTokenTypes.INTERPOLATION_START
          && next !== VueTokenTypes.INTERPOLATION_EXPR
          && next !== VueTokenTypes.INTERPOLATION_END
      ) {
        tokenType = if (next === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN || next === XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER)
            XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
        else
            XmlTokenType.XML_DATA_CHARACTERS
      }
      if (TOKENS_TO_MERGE.contains(tokenType)) {
        while (true) {
          val nextTokenType = originalLexer.tokenType
          if (nextTokenType !== tokenType) {
            break
          }
          originalLexer.advance()
        }
      }
      if (highlightMode && tokenType === XmlTokenType.XML_CHAR_ENTITY_REF) {
        while (originalLexer.tokenType === XmlTokenType.XML_CHAR_ENTITY_REF) {
          originalLexer.advance()
        }
        if (originalLexer.tokenType === XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) {
          return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
        }
      }
      return tokenType
    }

    companion object {
      private val TOKENS_TO_MERGE = TokenSet.create(
          XmlTokenType.XML_COMMENT_CHARACTERS,
          XmlTokenType.XML_WHITE_SPACE,
          XmlTokenType.XML_REAL_WHITE_SPACE,
          XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN,
          XmlTokenType.XML_DATA_CHARACTERS,
          XmlTokenType.XML_TAG_CHARACTERS,
      )

      fun isLexerWithinUnterminatedInterpolation(state: Int): Boolean {
        return getBaseLexerState(state) == _VueLexer.UNTERMINATED_INTERPOLATION
      }

      fun getBaseLexerState(state: Int): Int {
        return state and BASE_STATE_MASK
      }
    }
  }
}