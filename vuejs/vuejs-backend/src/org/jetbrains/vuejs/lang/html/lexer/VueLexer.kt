// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.lexer

import com.intellij.html.embedding.HtmlEmbeddedContentProvider
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.*
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType.TAG_WHITE_SPACE
import com.intellij.psi.xml.XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER
import com.intellij.psi.xml.XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
import com.intellij.psi.xml.XmlTokenType.XML_CHAR_ENTITY_REF
import com.intellij.psi.xml.XmlTokenType.XML_COMMENT_CHARACTERS
import com.intellij.psi.xml.XmlTokenType.XML_DATA_CHARACTERS
import com.intellij.psi.xml.XmlTokenType.XML_REAL_WHITE_SPACE
import com.intellij.psi.xml.XmlTokenType.XML_TAG_CHARACTERS
import com.intellij.psi.xml.XmlTokenType.XML_WHITE_SPACE
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_END
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_EXPR
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_START

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
        && (type === TAG_WHITE_SPACE && (VueMergingLexer.getBaseLexerState(state) == 0
                                         || VueMergingLexer.isLexerWithinUnterminatedInterpolation(state)))) {
      return XML_REAL_WHITE_SPACE
    }
    return type
  }

  companion object {
    internal val ATTRIBUTE_TOKENS = TokenSet.create(INTERPOLATION_START, INTERPOLATION_EXPR, INTERPOLATION_END)
    internal val TAG_TOKENS = TokenSet.create(INTERPOLATION_START)

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
      if (tokenType === INTERPOLATION_START
          && next !== INTERPOLATION_EXPR
          && next !== INTERPOLATION_END) {
        tokenType = if (next === XML_ATTRIBUTE_VALUE_TOKEN || next === XML_ATTRIBUTE_VALUE_END_DELIMITER)
          XML_ATTRIBUTE_VALUE_TOKEN
        else
          XML_DATA_CHARACTERS
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
      if (highlightMode && tokenType === XML_CHAR_ENTITY_REF) {
        while (originalLexer.tokenType === XML_CHAR_ENTITY_REF) {
          originalLexer.advance()
        }
        if (originalLexer.tokenType === XML_ATTRIBUTE_VALUE_TOKEN) {
          return XML_ATTRIBUTE_VALUE_TOKEN
        }
      }
      return tokenType
    }

    companion object {
      private val TOKENS_TO_MERGE = TokenSet.create(
        XML_COMMENT_CHARACTERS,
        XML_WHITE_SPACE,
        XML_REAL_WHITE_SPACE,
        XML_ATTRIBUTE_VALUE_TOKEN,
        XML_DATA_CHARACTERS,
        XML_TAG_CHARACTERS,
      )

      fun isLexerWithinUnterminatedInterpolation(state: Int): Boolean {
        return getBaseLexerState(state) == _VueLexer.UNTERMINATED_INTERPOLATION
      }

      fun getBaseLexerState(state: Int): Int {
        return state and BaseHtmlLexer.BASE_STATE_MASK
      }
    }
  }
}

