// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.ecmascript6.parsing.ES6ExpressionParser
import com.intellij.lang.ecmascript6.parsing.ES6FunctionParser
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.ecmascript6.parsing.ES6StatementParser
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JSPsiTypeParser
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.VueBundle.message
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.*
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes.FILTER_ARGUMENTS_LIST
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes.FILTER_EXPRESSION
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes.FILTER_LEFT_SIDE_ARGUMENT
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes.FILTER_REFERENCE_EXPRESSION

class VueJSParser(builder: PsiBuilder)
  : ES6Parser<VueJSParser.VueJSExpressionParser, ES6StatementParser<*>, ES6FunctionParser<*>,
  JSPsiTypeParser<JavaScriptParser<*, *, *, *>>>(builder) {

  companion object {
    fun parseEmbeddedExpression(builder: PsiBuilder, root: IElementType, attributeInfo: VueAttributeInfo?) {
      VueJSParser(builder).parseEmbeddedExpression(root, attributeInfo)
    }

    fun parseInterpolation(builder: PsiBuilder, root: IElementType) {
      parseEmbeddedExpression(builder, root, null)
    }

    fun parseJS(builder: PsiBuilder, root: IElementType) {
      VueJSParser(builder).parseJS(root)
    }
  }

  init {
    myExpressionParser = VueJSExpressionParser(this)
  }

  protected val extraParser = VueJSExtraParser(this, ::parseFilterOptional)

  fun parseEmbeddedExpression(root: IElementType, attributeInfo: VueAttributeInfo?) {
    val rootMarker = builder.mark()
    val statementMarker = builder.mark()
    extraParser.parseEmbeddedExpression(attributeInfo)
    // we need to consume rest of the tokens, even if they are not valid
    extraParser.parseRest()
    statementMarker.done(VueJSElementTypes.EMBEDDED_EXPR_STATEMENT)
    rootMarker.done(root)
  }

  private fun parseFilterOptional() = expressionParser.parseFilterOptional()

  class VueJSExpressionParser(parser: VueJSParser) : ES6ExpressionParser<VueJSParser>(parser) {

    private var expressionNestingLevel: Int = 0

    override fun parseScriptExpression() {
      throw UnsupportedOperationException()
    }

    //regex, curly, square, paren

    fun parseFilterOptional(): Boolean {
      var pipe: PsiBuilder.Marker = builder.mark()
      var firstParam: PsiBuilder.Marker = builder.mark()
      expressionNestingLevel = 0
      if (!parseExpressionOptional()) {
        firstParam.drop()
        pipe.drop()
        return false
      }

      while (builder.tokenType === JSTokenTypes.OR) {
        firstParam.done(FILTER_LEFT_SIDE_ARGUMENT)
        builder.advanceLexer()
        if (isIdentifierToken(builder.tokenType)) {
          val pipeName = builder.mark()
          builder.advanceLexer()
          pipeName.done(FILTER_REFERENCE_EXPRESSION)
        }
        else {
          builder.error(message("vue.parser.message.expected.identifier.or.string"))
        }
        if (builder.tokenType === JSTokenTypes.LPAR) {
          val params = builder.mark()
          expressionNestingLevel = 2
          parseArgumentListNoMarker()
          params.done(FILTER_ARGUMENTS_LIST)
          if (builder.tokenType !== JSTokenTypes.OR && !builder.eof()) {
            val err = builder.mark()
            builder.advanceLexer()
            err.error(message("vue.parser.message.expected.pipe.or.end.of.expression"))
            while (builder.tokenType !== JSTokenTypes.OR && !builder.eof()) {
              builder.advanceLexer()
            }
          }
        }
        else if (builder.tokenType !== JSTokenTypes.OR && !builder.eof()) {
          val err = builder.mark()
          builder.advanceLexer()
          err.error(message("vue.parser.message.expected.lparen.pipe.or.end.of.expression"))
          while (builder.tokenType !== JSTokenTypes.OR && !builder.eof()) {
            builder.advanceLexer()
          }
        }
        pipe.done(FILTER_EXPRESSION)
        firstParam = pipe.precede()
        pipe = firstParam.precede()
      }
      firstParam.drop()
      pipe.drop()
      return true
    }

    override fun parseAssignmentExpression(allowIn: Boolean): Boolean {
      expressionNestingLevel++
      try {
        return super.parseAssignmentExpression(allowIn)
      }
      finally {
        expressionNestingLevel--
      }
    }

    override fun getCurrentBinarySignPriority(allowIn: Boolean, advance: Boolean): Int {
      return if (builder.tokenType === JSTokenTypes.OR && expressionNestingLevel <= 1) {
        -1
      }
      else super.getCurrentBinarySignPriority(allowIn, advance)
    }
  }
}
