// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.parsing

import com.intellij.lang.PsiBuilder
import com.intellij.lang.WhitespacesBinders
import com.intellij.lang.actionscript.ActionScriptElementTypes
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptParserBundle.message
import com.intellij.lang.javascript.parsing.AdvancesLexer
import com.intellij.lang.javascript.parsing.JSParseResult
import com.intellij.lang.javascript.parsing.JSParseResult.Companion.acceptable
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeNameValuePairImpl
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Key
import com.intellij.psi.tree.IElementType

/**
 * @author Konstantin.Ulitin
 */
class ActionScriptFunctionParser internal constructor(parser: ActionScriptParser) : ActionScriptParserBase(parser) {

  enum class MethodEmptiness {
    ALWAYS,
    POSSIBLY,
    ;
  }

  enum class Context {
    EXPRESSION,
    SOURCE_ELEMENT,
    ;
  }

  fun parseFunctionExpression(): Boolean {
    val mark = builder.mark()
    parseFunctionExpressionAttributeList()
    return parseFunctionNoMarker(Context.EXPRESSION, mark)
  }

  fun parseFunctionDeclaration() {
    val prevMethodEmptiness = builder.getUserData(methodsEmptinessKey)
    try {
      val mark = builder.mark()
      parseAttributesList()
      parseFunctionNoMarker(Context.SOURCE_ELEMENT, mark)
    }
    finally {
      builder.putUserData(methodsEmptinessKey, prevMethodEmptiness)
    }
  }

  fun parseFunctionNoMarker(
    context: Context,
    functionMarker: PsiBuilder.Marker,
  ): Boolean {
    var functionKeywordWasOmitted = true
    var parsedWithoutErrors = true

    if (builder.tokenType === JSTokenTypes.FUNCTION_KEYWORD) {
      // function keyword must be omitted in context of get/set property definition
      builder.advanceLexer()
      functionKeywordWasOmitted = false
    }

    if (!parseFunctionName(functionKeywordWasOmitted, context)) {
      builder.error(message("javascript.parser.message.expected.function.name"))
      parsedWithoutErrors = false
    }

    parsedWithoutErrors = parsedWithoutErrors and !parseParameterList(context == Context.EXPRESSION).hasErrors

    parser.typeParser.tryParseFunctionReturnType()

    val methodEmptiness = builder.getUserData(methodsEmptinessKey)
    if (methodEmptiness == null) {
      parsedWithoutErrors = parsedWithoutErrors and parser.statementParser.parseFunctionBody()
    }
    else if (methodEmptiness == MethodEmptiness.ALWAYS) {
      if (builder.tokenType === JSTokenTypes.SEMICOLON) {
        builder.advanceLexer()
      }
      else if (builder.tokenType === JSTokenTypes.LBRACE) {
        val errorMessage = if (builder.getUserData(ActionScriptStatementParser.withinInterfaceKey) == null)
          message("javascript.ambient.declaration.should.have.no.body")
        else
          message("interface.function.declaration.should.have.no.body")
        parsedWithoutErrors = false
        builder.error(errorMessage)
      }
    }
    else if (methodEmptiness == MethodEmptiness.POSSIBLY) {
      if (builder.tokenType === JSTokenTypes.SEMICOLON) {
        builder.advanceLexer()
      }
      else if (builder.tokenType === JSTokenTypes.LBRACE) {
        parsedWithoutErrors = parsedWithoutErrors and parser.statementParser.parseFunctionBody()
      }
    }

    functionMarker.done((if (context == Context.SOURCE_ELEMENT) ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION else ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION_EXPRESSION))
    functionMarker.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT, WhitespacesBinders.DEFAULT_RIGHT_BINDER)
    return parsedWithoutErrors
  }

  /** possibly does not advance lexer  */
  fun parseFunctionName(functionKeywordWasOmitted: Boolean, context: Context): Boolean {
    if (parseGetSetAndNameAfterFunctionKeyword(context)) return true

    val tokenType = builder.tokenType
    if (isIdentifierToken(tokenType) ||
        functionKeywordWasOmitted && JSKeywordSets.PROPERTY_NAMES.contains(tokenType)
    ) {
      parseFunctionIdentifier()
    }
    else {
      return context == Context.EXPRESSION
    }
    return true
  }

  private fun parseGetSetAndNameAfterFunctionKeyword(context: Context): Boolean {
    val firstToken = builder.tokenType
    if (JSTokenTypes.GET_SET.contains(firstToken) && context != Context.EXPRESSION) {
      val lookAhead = builder.lookAhead(1)
      if (JSKeywordSets.PROPERTY_NAMES.contains(lookAhead)) {
        builder.advanceLexer()
      }
    }
    return false
  }

  fun parseParameterList(funExpr: Boolean): JSParseResult {
    if (!parser.typeParser.tryParseTypeParameterList()) {
      return JSParseResult.UNACCEPTABLE
    }
    val parameterList: PsiBuilder.Marker
    if (builder.tokenType !== JSTokenTypes.LPAR) {
      builder.error(message("javascript.parser.message.expected.lparen"))
      if (!funExpr) {
        parameterList = builder.mark() // To have non-empty parameters list at all the time.
        parameterList.done(parameterListElementType)
      }
      return JSParseResult.UNACCEPTABLE
    }
    else {
      parameterList = builder.mark()
      builder.advanceLexer()
    }

    var hasErrors = false
    var first = true
    while (builder.tokenType !== JSTokenTypes.RPAR) {
      if (first) {
        first = false
      }
      else {
        if (builder.tokenType === JSTokenTypes.COMMA) {
          builder.advanceLexer()
        }
        else {
          builder.error(message("javascript.parser.message.expected.comma.or.rparen"))
          hasErrors = true
          break
        }
      }

      val parameter = builder.mark()
      if (builder.tokenType === JSTokenTypes.DOT_DOT_DOT) {
        builder.advanceLexer()
      }
      else if (builder.tokenType === JSTokenTypes.DOT || builder.tokenType === JSTokenTypes.DOT_DOT) {
        // incomplete ...args
        builder.error(message("javascript.parser.message.expected.parameter.name"))
        while (builder.tokenType === JSTokenTypes.DOT || builder.tokenType === JSTokenTypes.DOT_DOT) {
          builder.advanceLexer()
        }
        hasErrors = true
      }
      hasErrors = hasErrors or !parseSingleParameter(parameter)
    }

    if (builder.tokenType === JSTokenTypes.RPAR) {
      builder.advanceLexer()
    }

    parameterList.done(parameterListElementType)
    return acceptable(hasErrors, null)
  }

  fun parseSingleParameter(parameter: PsiBuilder.Marker): Boolean {
    val result = doParseSingleParameter()
    val elementType = result.elementType
    if (elementType != null) {
      parameter.done(elementType)
    }
    else {
      parameter.drop()
    }
    return !result.hasErrors
  }

  private fun doParseSingleParameter(): JSParseResult {
    var hasErrors = false
    val acceptResult: Boolean
    val elementType = parameterType
    val tokenType = builder.tokenType
    if (isParameterName(tokenType)) {
      builder.advanceLexer()
      parser.typeParser.tryParseType()
      if (builder.tokenType === JSTokenTypes.EQ) {
        builder.advanceLexer()
        if (!parser.expressionParser.parseAssignmentExpression(true)) {
          builder.error(message("javascript.parser.message.expected.expression"))
        }
      }
      acceptResult = true
    }
    else {
      builder.error(message("javascript.parser.message.expected.formal.parameter.name"))
      hasErrors = true
      //f (p1, = defaultVal)
      if (builder.tokenType === JSTokenTypes.EQ) {
        builder.advanceLexer()
        parser.expressionParser.parseAssignmentExpression(true)
      }
      else {
        parser.typeParser.tryParseType()
      }
      acceptResult = false
    }

    return if (acceptResult)
      acceptable(hasErrors, elementType)
    else
      JSParseResult.UNACCEPTABLE
  }

  private fun isParameterName(tokenType: IElementType?): Boolean {
    return isIdentifierToken(tokenType)
  }

  val parameterListElementType: IElementType
    get() = JSElementTypes.PARAMETER_LIST

  @AdvancesLexer(onlyIfReturnsTrue = true)
  fun parseParameterListAndBody(marker: PsiBuilder.Marker, elementType: IElementType): Boolean {
    var lexerAdvanced = !parser.functionParser.parseParameterList(false).hasErrors
    lexerAdvanced = lexerAdvanced or parser.typeParser.tryParseFunctionReturnType()
    lexerAdvanced = lexerAdvanced or parser.statementParser.parseFunctionBody()
    marker.done(elementType)
    marker.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT, WhitespacesBinders.DEFAULT_RIGHT_BINDER)
    return lexerAdvanced
  }

  fun parseFunctionIdentifier() {
    if (!JSKeywordSets.PROPERTY_NAMES.contains(builder.getTokenType())) {
      LOG.error(builder.getTokenText())
    }

    parser.statementParser.parsePossiblyQualifiedName()
  }

  fun parseAttributesList(): Boolean {
    val modifierList = builder.mark()

    var seenNs = false
    var seenAnyAttributes = false

    try {
      var hasSomethingInAttrList = true
      var hadConditionalCompileBlock = false

      var doNotAllowAttributes = false
      while (hasSomethingInAttrList) {
        hasSomethingInAttrList = false

        while (builder.getTokenType() === JSTokenTypes.LBRACKET) {
          if (doNotAllowAttributes) {
            builder.error(message("javascript.parser.message.expected.declaration"))
            break
          }

          val attribute = builder.mark()

          builder.advanceLexer()

          val tokenType = builder.getTokenType()
          if (tokenType === JSTokenTypes.RBRACKET) {
            builder.error(message("javascript.parser.message.expected.identifier"))
          }
          else if (tokenType == null || !isIdentifierToken(tokenType)) {
            attribute.drop()
            return false
          }
          else {
            builder.advanceLexer()
          }

          while (builder.getTokenType() !== JSTokenTypes.RBRACKET) {
            parseAttributeBody()

            if (builder.eof()) {
              attribute.done(JSElementTypes.ATTRIBUTE)
              builder.error(message("javascript.parser.message.expected.rbracket"))
              return true
            }
          }

          builder.advanceLexer()
          attribute.done(JSElementTypes.ATTRIBUTE)
          hasSomethingInAttrList = true
        }

        if (builder.getTokenType() === JSTokenTypes.INCLUDE_KEYWORD) {
          hasSomethingInAttrList = true
          parser.statementParser.parseIncludeDirective()
        }

        if (builder.getTokenType() === JSTokenTypes.USE_KEYWORD && !doNotAllowAttributes) {
          hasSomethingInAttrList = true
          parser.statementParser.parseUseNamespaceDirective()
        }

        if (builder.getTokenType() === JSTokenTypes.IDENTIFIER && !seenNs) {
          var identifier = builder.mark()
          hasSomethingInAttrList = true
          seenNs = true
          val marker = builder.mark()
          builder.advanceLexer()
          marker.done(JSElementTypes.REFERENCE_EXPRESSION)

          val tokenType = builder.getTokenType()

          if (!hadConditionalCompileBlock) {
            if (tokenType === JSTokenTypes.COLON_COLON &&
                parser.expressionParser.proceedWithNamespaceReference(identifier, false)
            ) {
              (identifier.precede().also { identifier = it }).done(JSElementTypes.REFERENCE_EXPRESSION)
              identifier.precede().done(JSElementTypes.CONDITIONAL_COMPILE_VARIABLE_REFERENCE)
              hadConditionalCompileBlock = true
              seenNs = false
            }
            else if (tokenType === JSTokenTypes.DOT) {
              while (builder.getTokenType() === JSTokenTypes.DOT) {
                builder.advanceLexer()
                val identifierToken = isIdentifierToken(builder.getTokenType())
                if (identifierToken) {
                  builder.advanceLexer()
                }
                identifier.done(JSElementTypes.REFERENCE_EXPRESSION)
                identifier = identifier.precede()
                if (!identifierToken) {
                  builder.error(message("javascript.parser.message.expected.name"))
                  break
                }
              }
              identifier.drop()
            }
            else {
              identifier.drop()
            }
          }
          else {
            identifier.drop()
          }
        }

        var tokenType: IElementType?
        while (JSTokenTypes.MODIFIERS.contains(builder.getTokenType().also { tokenType = it })
               || tokenType === JSTokenTypes.GET_KEYWORD || tokenType === JSTokenTypes.SET_KEYWORD
        ) {
          doNotAllowAttributes = true
          seenAnyAttributes = true
          hasSomethingInAttrList = true
          if (builder.getTokenType() === JSTokenTypes.NATIVE_KEYWORD) {
            builder.putUserData(methodsEmptinessKey, MethodEmptiness.ALWAYS)
          }
          builder.advanceLexer()
        }

        if (builder.eof()) {
          return true
        }
      }
    }
    finally {
      val currentTokenType = builder.getTokenType()

      if (seenNs && !seenAnyAttributes && isNonAttrListOwner(currentTokenType)) {
        modifierList.rollbackTo()
      }
      else {
        modifierList.done(attributeListElementType)
      }
    }
    return true
  }

  val attributeListElementType: IElementType
    get() = ActionScriptElementTypes.ACTIONSCRIPT_ATTRIBUTE_LIST

  fun parseAttributeBody() {
    val haveLParen = checkMatches(builder, JSTokenTypes.LPAR, "javascript.parser.message.expected.lparen")

    while (haveLParen) {
      val hasName = JSAttributeNameValuePairImpl.IDENTIFIER_TOKENS_SET.contains(builder.getTokenType())

      if (builder.getTokenType() === JSTokenTypes.COMMA) {
        builder.error(message("javascript.parser.message.expected.identifier.or.value"))
        break
      }
      val tokenType = builder.getTokenType()
      if (tokenType === JSTokenTypes.RBRACKET) break
      if (tokenType === JSTokenTypes.RPAR) break

      val attributeNameValuePair = builder.mark()
      builder.advanceLexer()

      if (hasName && builder.getTokenType() !== JSTokenTypes.COMMA && builder.getTokenType() !== JSTokenTypes.RPAR) {
        checkMatches(builder, JSTokenTypes.EQ, "javascript.parser.message.expected.equal")

        val type = builder.getTokenType()
        if (type !== JSTokenTypes.COMMA && type !== JSTokenTypes.RBRACKET && type !== JSTokenTypes.RPAR) {
          if (type === JSTokenTypes.IDENTIFIER) {
            val ident = builder.mark()
            builder.advanceLexer()
            val nextTokenType = builder.getTokenType()
            ident.rollbackTo()
            if (!JSTokenTypes.STRING_LITERALS.contains(nextTokenType)) {
              parser.expressionParser.parseSimpleExpression()
            }
            else {
              builder.advanceLexer()
            }
          }
          else {
            builder.advanceLexer()
          }
        }
        else {
          builder.error(message("javascript.parser.message.expected.value"))
        }
      }

      attributeNameValuePair.done(JSElementTypes.ATTRIBUTE_NAME_VALUE_PAIR)
      if (builder.getTokenType() !== JSTokenTypes.COMMA) break
      builder.advanceLexer()

      if (builder.eof()) {
        builder.error(message("javascript.parser.message.expected.rparen"))
        return
      }
    }

    if (haveLParen) {
      checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen")
    }
    else {
      builder.advanceLexer()
    }
  }

  fun parseFunctionExpressionAttributeList() {
    val mark = builder.mark()
    val type = builder.getTokenType()
    if (type === JSTokenTypes.GET_KEYWORD || type === JSTokenTypes.SET_KEYWORD) {
      builder.advanceLexer()
    }
    mark.done(attributeListElementType)
  }

  val parameterType: IElementType
    get() = ActionScriptElementTypes.ACTIONSCRIPT_PARAMETER

  companion object {
    @JvmField
    val methodsEmptinessKey: Key<MethodEmptiness> = Key.create("methodsEmptinessKey")

    private val LOG = Logger.getInstance(ActionScriptFunctionParser::class.java)

    private fun isNonAttrListOwner(currentTokenType: IElementType?): Boolean {
      return currentTokenType !== JSTokenTypes.VAR_KEYWORD && currentTokenType !== JSTokenTypes.CONST_KEYWORD && currentTokenType !== JSTokenTypes.FUNCTION_KEYWORD && currentTokenType !== JSTokenTypes.CLASS_KEYWORD && currentTokenType !== JSTokenTypes.INTERFACE_KEYWORD && currentTokenType !== JSTokenTypes.NAMESPACE_KEYWORD
    }
  }
}
