// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.sfc.lexer

import com.intellij.lexer.*
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType.*
import org.jetbrains.astro.lang.sfc.lexer.AstroTokenTypes.Companion.EXPRESSION
import org.jetbrains.astro.lang.sfc.lexer.AstroTokenTypes.Companion.FRONTMATTER_SCRIPT

class AstroLexerImpl(override val project: Project?)
  : HtmlLexer(AstroMergingLexer(FlexAdapter(_AstroLexer())), true), AstroLexer {

  override fun isHtmlTagState(state: Int): Boolean {
    return state == _AstroLexer.START_TAG_NAME || state == _AstroLexer.END_TAG_NAME
  }

  override fun createAttributeEmbedmentTokenSet(): TokenSet {
    return TokenSet.orSet(super.createAttributeEmbedmentTokenSet(), ATTRIBUTE_TOKENS)
  }

  override fun createTagEmbedmentStartTokenSet(): TokenSet {
    return TokenSet.orSet(super.createTagEmbedmentStartTokenSet(), TAG_TOKENS)
  }

  companion object {
    internal val ATTRIBUTE_TOKENS = TokenSet.create(AstroTokenTypes.EXPRESSION_ATTRIBUTE,
                                                    AstroTokenTypes.SHORTHAND_ATTRIBUTE,
                                                    AstroTokenTypes.TEMPLATE_LITERAL_ATTRIBUTE)
    internal val TAG_TOKENS = TokenSet.create(FRONTMATTER_SCRIPT, EXPRESSION)
  }

  open class AstroMergingLexer(original: FlexAdapter) : MergingLexerAdapterBase(original) {

    override fun getMergeFunction(): MergeFunction {
      return MergeFunction { type, originalLexer -> this.merge(type, originalLexer) }
    }

    protected open fun merge(type: IElementType?, originalLexer: Lexer): IElementType? {
      if (!TOKENS_TO_MERGE.contains(type)) {
        return type
      }
      while (true) {
        val nextTokenType = originalLexer.tokenType
        if (nextTokenType !== type) {
          break
        }
        originalLexer.advance()
      }
      return type
    }

    companion object {

      private val TOKENS_TO_MERGE = TokenSet.create(XML_COMMENT_CHARACTERS, XML_WHITE_SPACE, XML_REAL_WHITE_SPACE,
                                                    XML_ATTRIBUTE_VALUE_TOKEN, XML_DATA_CHARACTERS, XML_TAG_CHARACTERS)

      fun getBaseLexerState(state: Int): Int {
        return state and BaseHtmlLexer.BASE_STATE_MASK
      }
    }
  }
}

