// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.ecmascript6.parsing.ES6ExpressionParser
import com.intellij.lang.ecmascript6.parsing.ES6FunctionParser
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.ecmascript6.parsing.ES6StatementParser
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.javascript.parsing.JSPsiTypeParser
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.VueBundle.message
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.*
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes.FILTER_ARGUMENTS_LIST
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes.FILTER_EXPRESSION
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes.FILTER_LEFT_SIDE_ARGUMENT
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes.FILTER_REFERENCE_EXPRESSION

class VueJSParser(builder: PsiBuilder, private val isJavaScript: Boolean)
  : ES6Parser<VueJSParser.VueJSExpressionParser, VueJSParser.VueJSStatementParser, ES6FunctionParser<*>,
  JSPsiTypeParser<JavaScriptParser<*, *, *, *>>>(builder) {

  constructor(builder: PsiBuilder) : this(builder, true)

  companion object {
    fun parseEmbeddedExpression(builder: PsiBuilder, root: IElementType, attributeInfo: VueAttributeInfo?) {
      val rootMarker = builder.mark()
      val statementMarker = builder.mark()
      val parseAction: (VueJSStatementParser) -> Unit =
        when (attributeInfo?.kind) {
          VueAttributeKind.DIRECTIVE ->
            when ((attributeInfo as VueDirectiveInfo).directiveKind) {
              VueDirectiveKind.FOR -> VueJSStatementParser::parseVFor
              VueDirectiveKind.BIND -> VueJSStatementParser::parseVBind
              VueDirectiveKind.ON -> VueJSStatementParser::parseVOn
              VueDirectiveKind.SLOT -> VueJSStatementParser::parseSlotPropsExpression
              else -> VueJSStatementParser::parseRegularExpression
            }
          VueAttributeKind.SLOT_SCOPE -> VueJSStatementParser::parseSlotPropsExpression
          VueAttributeKind.SCOPE -> VueJSStatementParser::parseSlotPropsExpression
          VueAttributeKind.SCRIPT_SETUP -> VueJSStatementParser::parseScriptSetupExpression
          else -> VueJSStatementParser::parseRegularExpression
        }
      VueJSParser(builder, false).statementParser.let {
        parseAction(it)
        // we need to consume rest of the tokens, even if they are invalid
        it.parseRest()
      }
      statementMarker.done(VueJSElementTypes.EMBEDDED_EXPR_STATEMENT)
      rootMarker.done(root)
    }

    fun parseInterpolation(builder: PsiBuilder, root: IElementType) {
      parseEmbeddedExpression(builder, root, null)
    }

    fun parseJS(builder: PsiBuilder, root: IElementType) {
      VueJSParser(builder).parseJS(root)
    }
  }

  init {
    myStatementParser = VueJSStatementParser(this)
    myExpressionParser = VueJSExpressionParser(this)
  }

  inner class VueJSStatementParser(parser: VueJSParser) : ES6StatementParser<VueJSParser>(parser) {

    fun parseRegularExpression() {
      if (!myExpressionParser.parseFilterOptional() && !builder.eof()) {
        val mark = builder.mark()
        builder.advanceLexer()
        mark.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"))
        parseRest(true)
      }
    }

    fun parseVOn() {
      while (!builder.eof()) {
        if (builder.tokenType === JSTokenTypes.SEMICOLON) {
          builder.advanceLexer()
        }
        else if (!parseExpressionStatement()) {
          builder.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"))
          if (!builder.eof()) {
            builder.advanceLexer()
          }
        }
      }
    }

    fun parseVBind() {
      if (!myExpressionParser.parseFilterOptional()) {
        val mark = builder.mark()
        if (!builder.eof()) {
          builder.advanceLexer()
        }
        mark.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"))
        parseRest(true)
      }
    }

    fun parseVFor() {
      val vForExpr = builder.mark()
      if (builder.tokenType == JSTokenTypes.LPAR) {
        parseVForVariables()
      }
      else if (!parseVariableStatement(VueJSStubElementTypes.V_FOR_VARIABLE)) {
        val marker = builder.mark()
        if (!builder.eof()
            && builder.tokenType !== JSTokenTypes.IN_KEYWORD
            && builder.tokenType !== JSTokenTypes.OF_KEYWORD) {
          builder.advanceLexer()
        }
        marker.error(JavaScriptBundle.message("javascript.parser.message.expected.identifier"))
      }
      if (builder.tokenType !== JSTokenTypes.IN_KEYWORD && builder.tokenType !== JSTokenTypes.OF_KEYWORD) {
        builder.error(message("vue.parser.message.expected.in.or.of"))
      }
      else {
        builder.advanceLexer()
      }
      myExpressionParser.parseExpression()
      vForExpr.done(VueJSElementTypes.V_FOR_EXPRESSION)
    }

    fun parseSlotPropsExpression() {
      parseParametersExpression(VueJSElementTypes.SLOT_PROPS_EXPRESSION, VueJSStubElementTypes.SLOT_PROPS_PARAMETER)
    }

    fun parseScriptSetupExpression() {
      parseParametersExpression(VueJSElementTypes.SCRIPT_SETUP_EXPRESSION, VueJSStubElementTypes.SCRIPT_SETUP_PARAMETER)
    }

    private fun parseParametersExpression(exprType: IElementType, paramType: IElementType) {
      val parametersList = builder.mark()
      val functionParser = object : ES6FunctionParser<VueJSParser>(this@VueJSParser) {
        override fun getParameterType(): IElementType = paramType
      }
      var first = true
      while (!builder.eof()) {
        if (first) {
          first = false
        }
        else {
          if (builder.tokenType === JSTokenTypes.COMMA) {
            builder.advanceLexer()
          }
          else {
            builder.error(message("vue.parser.message.expected.comma.or.end.of.expression"))
            break
          }
        }
        val parameter = builder.mark()
        if (builder.tokenType === JSTokenTypes.DOT_DOT_DOT) {
          builder.advanceLexer()
        }
        else if (builder.tokenType === JSTokenTypes.DOT) {
          // incomplete ...args
          builder.error(JavaScriptBundle.message("javascript.parser.message.expected.parameter.name"))
          while (builder.tokenType === JSTokenTypes.DOT) {
            builder.advanceLexer()
          }
        }
        functionParser.parseSingleParameter(parameter)
      }
      parametersList.done(JSStubElementTypes.PARAMETER_LIST)
      parametersList.precede().done(exprType)
    }

    internal fun parseRest(initialReported: Boolean = false) {
      var reported = initialReported
      while (!builder.eof()) {
        if (builder.tokenType === JSTokenTypes.SEMICOLON) {
          val mark = builder.mark()
          builder.advanceLexer()
          mark.error(message("vue.parser.message.statements.not.allowed"))
          reported = true
        }
        else {
          var justReported = false
          if (!reported) {
            builder.error(message("vue.parser.message.expected.end.of.expression"))
            reported = true
            justReported = true
          }
          if (!myExpressionParser.parseExpressionOptional()) {
            if (reported && !justReported) {
              val mark = builder.mark()
              builder.advanceLexer()
              mark.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"))
            }
            else {
              builder.advanceLexer()
            }
          }
          else {
            reported = false
          }
        }
      }
    }

    private fun parseVariableStatement(elementType: IElementType): Boolean {
      val statement = builder.mark()
      if (parseVariable(elementType)) {
        statement.done(JSStubElementTypes.VAR_STATEMENT)
        return true
      }
      else {
        statement.drop()
        return false
      }
    }

    private fun parseVariable(elementType: IElementType): Boolean {
      if (isIdentifierToken(builder.tokenType)) {
        buildTokenElement(elementType)
        return true
      }
      else if (myFunctionParser.willParseDestructuringAssignment()) {
        myExpressionParser.parseDestructuringElement(VueJSStubElementTypes.V_FOR_VARIABLE, false, false)
        return true
      }
      return false
    }

    private val EXTRA_VAR_COUNT = 2
    private fun parseVForVariables() {
      val parenthesis = builder.mark()
      builder.advanceLexer() //LPAR
      val varStatement = builder.mark()
      if (parseVariable(VueJSStubElementTypes.V_FOR_VARIABLE)) {
        var i = 0
        while (builder.tokenType == JSTokenTypes.COMMA && i < EXTRA_VAR_COUNT) {
          builder.advanceLexer()
          if (isIdentifierToken(builder.tokenType)) {
            buildTokenElement(VueJSStubElementTypes.V_FOR_VARIABLE)
          }
          i++
        }
      }
      if (builder.tokenType != JSTokenTypes.RPAR) {
        builder.error(JavaScriptBundle.message("javascript.parser.message.expected.rparen"))
        while (!builder.eof()
               && builder.tokenType != JSTokenTypes.RPAR
               && builder.tokenType != JSTokenTypes.IN_KEYWORD
               && builder.tokenType != JSTokenTypes.OF_KEYWORD) {
          builder.advanceLexer()
        }
        if (builder.tokenType != JSTokenTypes.RPAR) {
          varStatement.done(JSStubElementTypes.VAR_STATEMENT)
          parenthesis.done(JSElementTypes.PARENTHESIZED_EXPRESSION)
          return
        }
      }
      varStatement.done(JSStubElementTypes.VAR_STATEMENT)
      builder.advanceLexer()
      parenthesis.done(JSElementTypes.PARENTHESIZED_EXPRESSION)
    }
  }

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
