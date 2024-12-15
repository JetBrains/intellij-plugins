// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.ecmascript6.parsing.ES6ExpressionParser
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeInfo

class VueJSParser(
  builder: PsiBuilder,
) : ES6Parser(builder),
    VueExprParser {

  private val extraParser = VueJSExtraParser(this, ::parseExpressionOptional, ::parseFilterArgumentList, ::parseScriptGeneric)

  override val expressionParser: VueJSExpressionParser =
    VueJSExpressionParser(this, extraParser)

  override fun parseEmbeddedExpression(root: IElementType, attributeInfo: VueAttributeInfo?) {
    extraParser.parseEmbeddedExpression(root, attributeInfo, VueJSStubElementTypes.EMBEDDED_EXPR_CONTENT_JS)
  }

  private fun parseExpressionOptional() = expressionParser.parseExpressionOptional()
  private fun parseFilterArgumentList() = expressionParser.parseFilterArgumentList()
  private fun parseScriptGeneric() {
    val typeArgumentList = builder.mark()
    while (!builder.eof()) {
      builder.advanceLexer()
    }
    typeArgumentList.error(VueBundle.message("vue.parser.message.generic.component.parameters.only.with.typescript"))
  }

  class VueJSExpressionParser(parser: VueJSParser, private val extraParser: VueJSExtraParser) : ES6ExpressionParser<VueJSParser>(parser) {

    fun parseFilterArgumentList() {
      parseArgumentListNoMarker()
    }

    override fun parseScriptExpression() {
      throw UnsupportedOperationException()
    }

    override fun parseAssignmentExpression(allowIn: Boolean): Boolean {
      extraParser.expressionNestingLevel++
      try {
        return super.parseAssignmentExpression(allowIn)
      }
      finally {
        extraParser.expressionNestingLevel--
      }
    }

    override fun getCurrentBinarySignPriority(allowIn: Boolean, advance: Boolean): Int {
      return if (builder.tokenType === JSTokenTypes.OR && extraParser.expressionNestingLevel <= 1) {
        -1
      }
      else super.getCurrentBinarySignPriority(allowIn, advance)
    }
  }
}
