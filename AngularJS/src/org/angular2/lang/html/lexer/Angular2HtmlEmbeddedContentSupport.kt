// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.lexer

import com.intellij.html.embedding.*
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lexer.BaseHtmlLexer
import com.intellij.lexer.Lexer
import com.intellij.lexer.MergeFunction
import com.intellij.lexer.MergingLexerAdapterBase
import com.intellij.psi.tree.IElementType
import org.angular2.lang.expr.highlighting.Angular2SyntaxHighlighter
import org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType
import org.angular2.lang.html.highlighting.Angular2HtmlHighlightingLexer
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import java.util.*

class Angular2HtmlEmbeddedContentSupport : HtmlEmbeddedContentSupport {
  object Holder {
    val NG_EL_ATTRIBUTES: EnumSet<Angular2AttributeType> = EnumSet.of(
      Angular2AttributeType.EVENT, Angular2AttributeType.BANANA_BOX_BINDING,
      Angular2AttributeType.PROPERTY_BINDING, Angular2AttributeType.TEMPLATE_BINDINGS)
  }

  override fun isEnabled(lexer: BaseHtmlLexer): Boolean {
    return lexer is Angular2HtmlLexer || lexer is Angular2HtmlHighlightingLexer
  }

  override fun createEmbeddedContentProviders(lexer: BaseHtmlLexer): List<HtmlEmbeddedContentProvider> {
    return listOf(
      HtmlTokenEmbeddedContentProvider(lexer, Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR, { Angular2EmbeddedHighlightingLexer() }),
      Angular2AttributeContentProvider(lexer))
  }

  class Angular2AttributeContentProvider internal constructor(lexer: BaseHtmlLexer) : HtmlAttributeEmbeddedContentProvider(lexer) {
    override fun createEmbedmentInfo(): HtmlEmbedmentInfo? {
      val attributeName = attributeName ?: return null
      val info = Angular2AttributeNameParser.parse(
        attributeName.toString(), tagName!!.toString())
      return if (info.type != Angular2AttributeType.REGULAR
                 && Holder.NG_EL_ATTRIBUTES.contains(info.type)) {
        object : HtmlEmbedmentInfo {
          override fun createHighlightingLexer(): Lexer {
            return Angular2EmbeddedHighlightingLexer()
          }

          override fun getElementType(): IElementType? {
            return null
          }
        }
      }
      else null
    }

    override fun isInterestedInTag(tagName: CharSequence): Boolean {
      return lexer is Angular2HtmlHighlightingLexer
    }

    override fun isInterestedInAttribute(attributeName: CharSequence): Boolean {
      return true
    }
  }

  class Angular2EmbeddedHighlightingLexer internal constructor() : MergingLexerAdapterBase(Angular2SyntaxHighlighter().highlightingLexer) {
    override fun getMergeFunction(): MergeFunction {
      return MergeFunction { type, _ ->
        if (type === JSTokenTypes.WHITE_SPACE)
          Angular2HtmlHighlightingLexer.EXPRESSION_WHITE_SPACE
        else
          type
      }
    }
  }
}