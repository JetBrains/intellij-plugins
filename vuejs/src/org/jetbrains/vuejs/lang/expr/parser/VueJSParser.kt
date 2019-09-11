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
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.*

class VueJSParser(builder: PsiBuilder, private val isJavaScript: Boolean)
  : ES6Parser<ES6ExpressionParser<*>, VueJSParser.VueJSStatementParser, ES6FunctionParser<*>,
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
              VueDirectiveKind.SLOT -> VueJSStatementParser::parseSlotPropsExpression
              else -> VueJSStatementParser::parseRegularExpression
            }
          VueAttributeKind.SLOT_SCOPE -> VueJSStatementParser::parseSlotPropsExpression
          VueAttributeKind.SCOPE -> VueJSStatementParser::parseSlotPropsExpression
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
  }

  inner class VueJSStatementParser(parser: VueJSParser) : ES6StatementParser<VueJSParser>(parser) {

    fun parseRegularExpression() {
      // TODO support filters
      if (!myExpressionParser.parseExpressionOptional() && !builder.eof()) {
        val mark = builder.mark()
        if (!builder.eof()) {
          builder.advanceLexer()
        }
        mark.error(JSBundle.message("javascript.parser.message.expected.expression"))
        parseRest(true)
      }
    }

    fun parseVBind() {
      // TODO support filters
      if (!myExpressionParser.parseExpressionOptional()) {
        val mark = builder.mark()
        if (!builder.eof()) {
          builder.advanceLexer()
        }
        mark.error(JSBundle.message("javascript.parser.message.expected.expression"))
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
        marker.error(JSBundle.message("javascript.parser.message.expected.identifier"))
      }
      if (builder.tokenType !== JSTokenTypes.IN_KEYWORD && builder.tokenType !== JSTokenTypes.OF_KEYWORD) {
        builder.error("'in' or 'of' expected")
      }
      else {
        builder.advanceLexer()
      }
      myExpressionParser.parseExpression()
      vForExpr.done(VueJSElementTypes.V_FOR_EXPRESSION)
    }

    fun parseSlotPropsExpression() {
      val slotPropsExpression = builder.mark()
      if (builder.eof()) {
        builder.mark().error("Expected slot props variable declaration")
      }
      else {
        parseVariableStatement(VueJSStubElementTypes.SLOT_PROPS_VARIABLE)
        if (!builder.eof()) {
          val mark = builder.mark()
          while (!builder.eof()) {
            builder.advanceLexer()
          }
          mark.error("Unexpected tokens in slot props variable declaration")
        }
      }
      slotPropsExpression.done(VueJSElementTypes.SLOT_PROPS_EXPRESSION)
    }

    internal fun parseRest(initialReported: Boolean = false) {
      var reported = initialReported
      while (!builder.eof()) {
        if (builder.tokenType === JSTokenTypes.SEMICOLON) {
          val mark = builder.mark()
          builder.advanceLexer()
          mark.error("Statements are not allowed in Vue expressions")
          reported = true
        }
        else {
          var justReported = false
          if (!reported) {
            builder.error("Expected end of expression")
            reported = true
            justReported = true
          }
          if (!myExpressionParser.parseExpressionOptional()) {
            if (reported && !justReported) {
              val mark = builder.mark()
              builder.advanceLexer()
              mark.error(JSBundle.message("javascript.parser.message.expected.expression"))
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
        builder.error(JSBundle.message("javascript.parser.message.expected.rparen"))
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
}
