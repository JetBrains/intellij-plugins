// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.lexer

import com.intellij.html.embedding.HtmlEmbeddedContentProvider
import com.intellij.lang.javascript.dialects.JSLanguageLevel
import com.intellij.lexer.*
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType.*
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_END
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_EXPR
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes.Companion.INTERPOLATION_START

class VueLexerImpl(override val languageLevel: JSLanguageLevel,
                   override val project: Project?,
                   override val interpolationConfig: Pair<String, String>?)
  : HtmlLexer(VueMergingLexer(FlexAdapter(_VueLexer(interpolationConfig))), true), VueLexer {

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

  companion object {
    internal val ATTRIBUTE_TOKENS = TokenSet.create(INTERPOLATION_START, INTERPOLATION_EXPR, INTERPOLATION_END)
    internal val TAG_TOKENS = TokenSet.create(INTERPOLATION_START)
  }

  open class VueMergingLexer(original: FlexAdapter) : MergingLexerAdapterBase(original) {

    override fun getMergeFunction(): MergeFunction {
      return MergeFunction { type, originalLexer -> this.merge(type, originalLexer) }
    }

    protected open fun merge(type: IElementType?, originalLexer: Lexer): IElementType? {
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
      if (!TOKENS_TO_MERGE.contains(tokenType)) {
        return tokenType
      }
      while (true) {
        val nextTokenType = originalLexer.tokenType
        if (nextTokenType !== tokenType) {
          break
        }
        originalLexer.advance()
      }
      return tokenType
    }

    companion object {

      private val TOKENS_TO_MERGE = TokenSet.create(XML_COMMENT_CHARACTERS, XML_WHITE_SPACE, XML_REAL_WHITE_SPACE,
                                                    XML_ATTRIBUTE_VALUE_TOKEN, XML_DATA_CHARACTERS, XML_TAG_CHARACTERS)

      fun isLexerWithinUnterminatedInterpolation(state: Int): Boolean {
        return getBaseLexerState(state) == _VueLexer.UNTERMINATED_INTERPOLATION
      }

      fun getBaseLexerState(state: Int): Int {
        return state and BaseHtmlLexer.BASE_STATE_MASK
      }
    }
  }
}

