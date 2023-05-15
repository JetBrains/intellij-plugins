// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.ecmascript6.parsing.TypeScriptExpressionParser
import com.intellij.lang.javascript.ecmascript6.parsing.TypeScriptParser
import com.intellij.lang.javascript.parsing.JavaScriptParserBase
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeInfo

class VueTSParser(builder: PsiBuilder) : TypeScriptParser(builder), VueExprParser {

  private val extraParser = VueJSExtraParser(this, ::parseExpressionOptional, ::parseFilterArgumentList, ::parseScriptGeneric)

  init {
    myExpressionParser = VueTSExpressionParser(this, extraParser)
  }

  override fun parseEmbeddedExpression(root: IElementType, attributeInfo: VueAttributeInfo?) {
    extraParser.parseEmbeddedExpression(root, attributeInfo, VueJSStubElementTypes.EMBEDDED_EXPR_CONTENT_TS)
  }

  private fun parseExpressionOptional() = expressionParser.parseExpressionOptional()
  private fun parseFilterArgumentList() = expressionParser.parseFilterArgumentList()

  private fun parseScriptGeneric() {
    val typeArgumentList = builder.mark()
    var first = true
    while (!builder.eof()) {
      if (!first) {
        JavaScriptParserBase.checkMatches(builder, JSTokenTypes.COMMA, "javascript.parser.message.expected.comma")
      }
      if (builder.tokenType === JSTokenTypes.COMMA) {
        if (first) first = false
        builder.error(JavaScriptBundle.message("javascript.parser.message.expected.type"))
        //parse Foo<,,,,>
        continue
      }
      if (!typeParser.parseTypeParameter()) {
        builder.advanceLexer()
      }
      first = false
    }
    typeArgumentList.done(VueJSStubElementTypes.SCRIPT_SETUP_TYPE_PARAMETER_LIST)
  }

  override fun getExpressionParser(): VueTSExpressionParser {
    // Let's hack around TypeScriptParser type parameters
    return super.getExpressionParser() as VueTSExpressionParser
  }

  class VueTSExpressionParser(parser: VueTSParser, private val extraParser: VueJSExtraParser) : TypeScriptExpressionParser(parser) {
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
