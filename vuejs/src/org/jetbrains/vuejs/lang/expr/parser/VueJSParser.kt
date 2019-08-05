// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.ecmascript6.parsing.ES6ExpressionParser
import com.intellij.lang.ecmascript6.parsing.ES6FunctionParser
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.ecmascript6.parsing.ES6StatementParser
import com.intellij.lang.javascript.JSBundle
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JSPsiTypeParser
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser

class VueJSParser(builder: PsiBuilder) : ES6Parser<ES6ExpressionParser<*>, ES6StatementParser<*>,
  ES6FunctionParser<*>, JSPsiTypeParser<JavaScriptParser<*, *, *, *>>>(builder) {

  companion object {
    fun parseEmbeddedExpression(builder: PsiBuilder, root: IElementType, attributeInfo: VueAttributeNameParser.VueAttributeInfo) {
      // TODO - specialize parsing according to attribute info
      val rootMarker = builder.mark()
      val statementMarker = builder.mark()
      while (!builder.eof()) {
        VueJSParser(builder).parseExpectedExpression(builder, false)
      }
      statementMarker.done(VueJSElementTypes.EMBEDDED_EXPR_STATEMENT)
      rootMarker.done(root)
    }

    fun parseInterpolation(builder: PsiBuilder, root: IElementType) {
      // TODO - specialize parsing
      val rootMarker = builder.mark()
      val statementMarker = builder.mark()
      while (!builder.eof()) {
        VueJSParser(builder).parseExpectedExpression(builder, false)
      }
      statementMarker.done(VueJSElementTypes.EMBEDDED_EXPR_STATEMENT)
      rootMarker.done(root)
    }

    fun parseJS(builder: PsiBuilder, root: IElementType) {
      VueJSParser(builder).parseJS(root)
    }
  }

  init {
    myStatementParser = object : ES6StatementParser<VueJSParser>(this) {
      override fun parseSourceElement() {
        if (builder.currentOffset != 0 || !parseExpectedExpression(builder, false)) {
          super.parseSourceElement()
        }
      }
    }
  }

  private fun parseVForLoopVariableStatement(): Boolean {
    val statement = builder.mark()
    if (parseVForLoopVariable()) {
      statement.done(JSStubElementTypes.VAR_STATEMENT)
      return true
    }
    else {
      statement.drop()
      return false
    }
  }

  private fun parseVForLoopVariable(): Boolean {
    if (isIdentifierToken(builder.tokenType)) {
      buildTokenElement(VueJSElementTypes.V_FOR_VARIABLE)
      return true
    }
    else if (myFunctionParser.willParseDestructuringAssignment()) {
      myExpressionParser.parseDestructuringElement(VueJSElementTypes.V_FOR_VARIABLE, false, false)
      return true
    }
    return false
  }

  private fun parseVForContents(): Boolean {
    val vForExpr = builder.mark()
    if (builder.tokenType == JSTokenTypes.LPAR) {
      parseVForVariables()
    }
    else if (!parseVForLoopVariableStatement()) {
      builder.error("identifier(s) expected")
      builder.advanceLexer()
    }
    if (builder.tokenType !== JSTokenTypes.IN_KEYWORD && builder.tokenType !== JSTokenTypes.OF_KEYWORD) {
      vForExpr.rollbackTo()
      return false
    }
    else {
      builder.advanceLexer()
    }
    if (parseExpectedExpression(builder, true)) {
      vForExpr.done(VueJSElementTypes.V_FOR_EXPRESSION)
    }
    else {
      vForExpr.rollbackTo()
      return false
    }
    return true
  }

  private val EXTRA_VAR_COUNT = 2
  private fun parseVForVariables(): Boolean {
    val parenthesis = builder.mark()
    builder.advanceLexer() //LPAR
    val varStatement = builder.mark()
    if (parseVForLoopVariable()) {
      var i = 0
      while (builder.tokenType == JSTokenTypes.COMMA && i < EXTRA_VAR_COUNT) {
        builder.advanceLexer()
        if (isIdentifierToken(builder.tokenType)) {
          buildTokenElement(VueJSElementTypes.V_FOR_VARIABLE)
        }
        i++
      }
    }
    if (builder.tokenType != JSTokenTypes.RPAR) {
      builder.error("closing parenthesis expected")
      while (!builder.eof() && builder.tokenType != JSTokenTypes.RPAR &&
             builder.tokenType != JSTokenTypes.IN_KEYWORD &&
             builder.tokenType != JSTokenTypes.OF_KEYWORD) {
        builder.advanceLexer()
      }
      if (builder.tokenType != JSTokenTypes.RPAR) {
        varStatement.done(JSStubElementTypes.VAR_STATEMENT)
        parenthesis.done(JSElementTypes.PARENTHESIZED_EXPRESSION)
        return false
      }
    }
    varStatement.done(JSStubElementTypes.VAR_STATEMENT)
    builder.advanceLexer()
    parenthesis.done(JSElementTypes.PARENTHESIZED_EXPRESSION)
    return true
  }

  private fun parseExpectedExpression(builder: PsiBuilder, isOnlyStandardJS: Boolean): Boolean {
    if (!isOnlyStandardJS && parseVForContents()) return true
    if (!myExpressionParser.parseExpressionOptional()) {
      builder.error(JSBundle.message("javascript.parser.message.expected.expression"))
      builder.advanceLexer()
      return false
    }
    return true
  }
}
