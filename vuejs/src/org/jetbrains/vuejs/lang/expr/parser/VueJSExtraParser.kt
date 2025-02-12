// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.ecmascript6.parsing.ES6FunctionParser
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptParserBundle
import com.intellij.lang.javascript.parsing.JavaScriptParserBase
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeInfo

class VueJSExtraParser(
  parser: ES6Parser,
  private val parseExpressionOptional: () -> Boolean,
  private val parseArgumentListNoMarker: () -> Unit,
  private val parseScriptGeneric: () -> Unit,
) : JavaScriptParserBase<ES6Parser>(parser) {
  private val statementParser get() = parser.statementParser

  fun parseEmbeddedExpression(root: IElementType, attributeInfo: VueAttributeInfo?, expressionContent: IElementType) {
    val rootMarker = builder.mark()
    val statementMarker = builder.mark()
    parseEmbeddedExpressionInner(attributeInfo)
    // we need to consume the rest of the tokens, even if they are not valid
    parseRest()
    statementMarker.done(expressionContent)
    rootMarker.done(root)
  }

  private fun parseEmbeddedExpressionInner(attributeInfo: VueAttributeInfo?) {
    when (attributeInfo?.kind) {
      VueAttributeNameParser.VueAttributeKind.DIRECTIVE -> {
        when ((attributeInfo as VueAttributeNameParser.VueDirectiveInfo).directiveKind) {
          VueAttributeNameParser.VueDirectiveKind.FOR -> parseVFor()
          VueAttributeNameParser.VueDirectiveKind.BIND -> parseVBind()
          VueAttributeNameParser.VueDirectiveKind.ON -> parseVOn()
          VueAttributeNameParser.VueDirectiveKind.SLOT -> parseSlotPropsExpression()
          else -> parseRegularExpression()
        }
      }
      VueAttributeNameParser.VueAttributeKind.SLOT_SCOPE -> parseSlotPropsExpression()
      VueAttributeNameParser.VueAttributeKind.SCOPE -> parseSlotPropsExpression()
      VueAttributeNameParser.VueAttributeKind.SCRIPT_GENERIC -> parseScriptGeneric()
      else -> parseRegularExpression()
    }
  }

  private fun parseRegularExpression() {
    if (!parseFilterOptional() && !builder.eof()) {
      val mark = builder.mark()
      builder.advanceLexer()
      mark.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"))
      parseRest(true)
    }
  }

  private fun parseVOn() {
    while (!builder.eof()) {
      if (builder.tokenType === JSTokenTypes.SEMICOLON) {
        builder.advanceLexer()
      }
      else if (!statementParser.parseExpressionStatement()) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"))
        if (!builder.eof()) {
          builder.advanceLexer()
        }
      }
    }
  }

  private fun parseVBind() {
    if (!parseFilterOptional()) {
      val mark = builder.mark()
      if (!builder.eof()) {
        builder.advanceLexer()
      }
      mark.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"))
      parseRest(true)
    }
  }

  private fun parseVFor() {
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
      marker.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"))
    }
    if (builder.tokenType !== JSTokenTypes.IN_KEYWORD && builder.tokenType !== JSTokenTypes.OF_KEYWORD) {
      builder.error(VueBundle.message("vue.parser.message.expected.in.or.of"))
    }
    else {
      builder.advanceLexer()
    }
    parser.expressionParser.parseExpression()
    vForExpr.done(VueJSElementTypes.V_FOR_EXPRESSION)
  }

  private fun parseSlotPropsExpression() {
    parseParametersExpression(VueJSElementTypes.SLOT_PROPS_EXPRESSION, VueJSStubElementTypes.SLOT_PROPS_PARAMETER)
  }

  private fun parseParametersExpression(exprType: IElementType, @Suppress("SameParameterValue") paramType: IElementType) {
    val parametersList = builder.mark()
    val functionParser = object : ES6FunctionParser<ES6Parser>(parser) {
      override val parameterType: IElementType = paramType
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
          builder.error(VueBundle.message("vue.parser.message.expected.comma.or.end.of.expression"))
          break
        }
      }
      val parameter = builder.mark()
      if (builder.tokenType === JSTokenTypes.DOT_DOT_DOT) {
        builder.advanceLexer()
      }
      else if (builder.tokenType === JSTokenTypes.DOT) {
        // incomplete ...args
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.parameter.name"))
        while (builder.tokenType === JSTokenTypes.DOT) {
          builder.advanceLexer()
        }
      }
      functionParser.parseSingleParameter(parameter)
    }
    parametersList.done(JSStubElementTypes.PARAMETER_LIST)
    parametersList.precede().done(exprType)
  }

  private fun parseRest(initialReported: Boolean = false) {
    var reported = initialReported
    while (!builder.eof()) {
      if (builder.tokenType === JSTokenTypes.SEMICOLON) {
        val mark = builder.mark()
        builder.advanceLexer()
        mark.error(VueBundle.message("vue.parser.message.statements.not.allowed"))
        reported = true
      }
      else {
        var justReported = false
        if (!reported) {
          builder.error(VueBundle.message("vue.parser.message.expected.end.of.expression"))
          reported = true
          justReported = true
        }
        if (!parser.expressionParser.parseExpressionOptional()) {
          if (!justReported) {
            val mark = builder.mark()
            builder.advanceLexer()
            mark.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"))
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

  private fun parseVariableStatement(@Suppress("SameParameterValue") elementType: IElementType): Boolean {
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
      parser.buildTokenElement(elementType)
      return true
    }
    else if (parser.functionParser.willParseDestructuringAssignment()) {
      parser.expressionParser.parseDestructuringElement(VueJSStubElementTypes.V_FOR_VARIABLE, false, false)
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
          parser.buildTokenElement(VueJSStubElementTypes.V_FOR_VARIABLE)
        }
        i++
      }
    }
    if (builder.tokenType != JSTokenTypes.RPAR) {
      builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.rparen"))
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

  internal var expressionNestingLevel: Int = 0

  private fun parseFilterOptional(): Boolean {
    var pipe: PsiBuilder.Marker = builder.mark()
    var firstParam: PsiBuilder.Marker = builder.mark()
    expressionNestingLevel = 0
    if (!parseExpressionOptional()) {
      firstParam.drop()
      pipe.drop()
      return false
    }

    while (builder.tokenType === JSTokenTypes.OR) {
      firstParam.done(VueJSElementTypes.FILTER_LEFT_SIDE_ARGUMENT)
      builder.advanceLexer()
      if (isIdentifierToken(builder.tokenType)) {
        val pipeName = builder.mark()
        builder.advanceLexer()
        pipeName.done(VueJSElementTypes.FILTER_REFERENCE_EXPRESSION)
      }
      else {
        builder.error(VueBundle.message("vue.parser.message.expected.identifier.or.string"))
      }
      if (builder.tokenType === JSTokenTypes.LPAR) {
        val params = builder.mark()
        expressionNestingLevel = 2
        parseArgumentListNoMarker()
        params.done(VueJSElementTypes.FILTER_ARGUMENTS_LIST)
        if (builder.tokenType !== JSTokenTypes.OR && !builder.eof()) {
          val err = builder.mark()
          builder.advanceLexer()
          err.error(VueBundle.message("vue.parser.message.expected.pipe.or.end.of.expression"))
          while (builder.tokenType !== JSTokenTypes.OR && !builder.eof()) {
            builder.advanceLexer()
          }
        }
      }
      else if (builder.tokenType !== JSTokenTypes.OR && !builder.eof()) {
        val err = builder.mark()
        builder.advanceLexer()
        err.error(VueBundle.message("vue.parser.message.expected.lparen.pipe.or.end.of.expression"))
        while (builder.tokenType !== JSTokenTypes.OR && !builder.eof()) {
          builder.advanceLexer()
        }
      }
      pipe.done(VueJSElementTypes.FILTER_EXPRESSION)
      firstParam = pipe.precede()
      pipe = firstParam.precede()
    }
    firstParam.drop()
    pipe.drop()
    return true
  }
}