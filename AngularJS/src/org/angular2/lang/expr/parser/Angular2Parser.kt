// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.WhitespacesBinders
import com.intellij.lang.ecmascript6.ES6StubElementTypes
import com.intellij.lang.javascript.*
import com.intellij.lang.javascript.parsing.*
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.Consumer
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.expr.lexer.Angular2TokenTypes
import org.angular2.lang.expr.parser.Angular2ElementTypes.Companion.createTemplateBindingStatement
import org.angular2.lang.expr.parser.Angular2ElementTypes.Companion.createTemplateBindingsStatement
import org.angular2.lang.expr.parser.Angular2Parser.Angular2ExpressionParser
import org.angular2.lang.expr.parser.Angular2Parser.Angular2StatementParser
import org.jetbrains.annotations.NonNls

class Angular2Parser private constructor(builder: PsiBuilder,
                                         private val myIsAction: Boolean,
                                         private val myIsSimpleBinding: Boolean,
                                         private val myIsJavaScript: Boolean)
  : JavaScriptParser<Angular2ExpressionParser, Angular2StatementParser, FunctionParser<*>, JSPsiTypeParser<*>>(DialectOptionHolder.JS_1_5,
                                                                                                               builder) {
  constructor(builder: PsiBuilder) : this(builder, false, false, true)

  init {
    myExpressionParser = Angular2ExpressionParser()
    myStatementParser = Angular2StatementParser(this)
  }

  inner class Angular2StatementParser(parser: Angular2Parser?) : StatementParser<Angular2Parser?>(parser) {
    fun parseChain() {
      assert(!myIsJavaScript)
      val chain = builder.mark()
      var count = 0
      while (!builder.eof()) {
        count++
        val expression = builder.mark()
        if (!expressionParser!!.parseExpressionOptional(false, false)) {
          builder.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"))
          builder.advanceLexer()
          expression.drop()
        }
        else if (myIsAction) {
          expression.done(JSElementTypes.EXPRESSION_STATEMENT)
        }
        else {
          expression.drop()
        }
        val tokenType = builder.tokenType
        if (tokenType === JSTokenTypes.SEMICOLON) {
          if (!myIsAction) {
            builder.error(Angular2Bundle.message("angular.parse.expression.chained-expression-in-binding"))
          }
          while (builder.tokenType === JSTokenTypes.SEMICOLON) {
            builder.advanceLexer()
          }
        }
        else if (tokenType != null) {
          builder.error(Angular2Bundle.message("angular.parse.expression.unexpected-token", builder.tokenText!!))
        }
      }
      when (count) {
        0 -> {
          if (myIsAction) {
            chain.done(JSElementTypes.EMPTY_STATEMENT)
          }
          else {
            chain.done(JSStubElementTypes.EMPTY_EXPRESSION)
          }
        }
        1 -> chain.drop()
        else -> {
          if (myIsAction) {
            chain.done(Angular2ElementTypes.CHAIN_STATEMENT)
          }
          else {
            chain.drop()
          }
        }
      }
    }

    fun parseQuote(): Boolean {
      val quote = builder.mark()
      if (!(builder.tokenType === JSTokenTypes.IDENTIFIER
            || Angular2TokenTypes.KEYWORDS.contains(builder.tokenType))
          || builder.lookAhead(1) !== JSTokenTypes.COLON) {
        quote.drop()
        return false
      }
      builder.advanceLexer()
      builder.enforceCommentTokens(TokenSet.EMPTY)
      builder.advanceLexer()
      val rest = builder.mark()
      while (!builder.eof()) {
        builder.advanceLexer()
      }
      rest.collapse(JSTokenTypes.STRING_LITERAL)
      quote.done(Angular2ElementTypes.QUOTE_STATEMENT)
      return true
    }

    fun parseTemplateBindings(templateKey: String) {
      var firstBinding = true
      do {
        val binding = builder.mark()
        var isVar = false
        var rawKey: String?
        var key: String?
        if (firstBinding) {
          key = templateKey
          rawKey = key
          firstBinding = false
        }
        else {
          isVar = builder.tokenType === JSTokenTypes.LET_KEYWORD
          if (isVar) builder.advanceLexer()
          rawKey = parseTemplateBindingKey(isVar)
          key = if (isVar) rawKey else templateKey + StringUtil.capitalize(rawKey)
          if (builder.tokenType === JSTokenTypes.COLON) {
            builder.advanceLexer()
          }
        }
        var name: String? = null
        if (isVar) {
          name = if (builder.tokenType === JSTokenTypes.EQ) {
            builder.advanceLexer()
            parseTemplateBindingKey(false)
          }
          else {
            Angular2LangUtil.`$IMPLICIT`
          }
        }
        else if (builder.tokenType === JSTokenTypes.AS_KEYWORD) {
          builder.advanceLexer()
          name = rawKey
          key = parseTemplateBindingKey(true)
          isVar = true
        }
        else if (builder.tokenType !== JSTokenTypes.LET_KEYWORD
                 && !expressionParser!!.parsePipe()) {
          builder.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"))
        }
        binding.done(createTemplateBindingStatement(key, isVar, name))
        if (builder.tokenType === JSTokenTypes.AS_KEYWORD && !isVar) {
          val localBinding = builder.mark()
          builder.advanceLexer()
          val letName = parseTemplateBindingKey(true)
          localBinding.done(createTemplateBindingStatement(letName, true, key))
        }
        if (builder.tokenType === JSTokenTypes.SEMICOLON
            || builder.tokenType === JSTokenTypes.COMMA) {
          builder.advanceLexer()
        }
      }
      while (!builder.eof())
    }

    private fun parseTemplateBindingKey(isVariable: Boolean): String {
      val key = builder.mark()
      var operatorFound = true
      val result = StringBuilder()
      do {
        if (!isIdentifierName(builder.tokenType)) {
          if (result.isNotEmpty()) {
            finishKey(key, isVariable)
          }
          else {
            key.drop()
          }
          builder.error(Angular2Bundle.message("angular.parse.expression.expected-identifier-or-keyword"))
          builder.advanceLexer()
          return result.toString()
        }
        result.append(builder.tokenText)
        if (builder.rawLookup(1) === JSTokenTypes.MINUS) {
          builder.advanceLexer()
          result.append(builder.tokenText)
        }
        else {
          operatorFound = false
        }
        builder.advanceLexer()
      }
      while (operatorFound)
      finishKey(key, isVariable)
      return result.toString()
    }
  }

  inner class Angular2ExpressionParser : ExpressionParser<Angular2Parser?>(this@Angular2Parser) {
    override fun parseAssignmentExpression(allowIn: Boolean): Boolean {
      //In Angular EL Pipe is the top level expression instead of Assignment
      return parsePipe()
    }

    override fun parseScriptExpression() {
      throw UnsupportedOperationException()
    }

    fun parsePipe(): Boolean {
      var pipe = builder.mark()
      var firstParam = builder.mark()
      if (!parseAssignmentExpressionChecked()) {
        firstParam.drop()
        pipe.drop()
        return false
      }
      while (builder.tokenType === JSTokenTypes.OR) {
        if (myIsSimpleBinding) {
          builder.error(Angular2Bundle.message("angular.parse.expression.pipe-in-host-binding"))
        }
        else if (myIsAction) {
          builder.error(Angular2Bundle.message("angular.parse.expression.pipe-in-action"))
        }
        firstParam.done(Angular2ElementTypes.PIPE_LEFT_SIDE_ARGUMENT)
        builder.advanceLexer()
        if (builder.tokenType === JSTokenTypes.IDENTIFIER
            || Angular2TokenTypes.KEYWORDS.contains(builder.tokenType)) {
          val pipeName = builder.mark()
          builder.advanceLexer()
          pipeName.done(Angular2ElementTypes.PIPE_REFERENCE_EXPRESSION)
        }
        else {
          builder.error(Angular2Bundle.message("angular.parse.expression.expected-identifier-or-keyword"))
        }
        val params = builder.mark()
        var hasParams = false
        while (builder.tokenType === JSTokenTypes.COLON) {
          builder.advanceLexer()
          if (!parseAssignmentExpressionChecked()) {
            builder.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"))
          }
          else {
            hasParams = true
          }
        }
        if (hasParams) {
          params.done(Angular2ElementTypes.PIPE_ARGUMENTS_LIST)
        }
        else {
          params.drop()
        }
        pipe.done(Angular2ElementTypes.PIPE_EXPRESSION)
        firstParam = pipe.precede()
        pipe = firstParam.precede()
      }
      firstParam.drop()
      pipe.drop()
      return true
    }

    private fun parseAssignmentExpressionChecked(): Boolean {
      val expr = builder.mark()
      if (builder.tokenType === JSTokenTypes.EQ) {
        builder.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"))
        builder.advanceLexer()
        if (!parsePipe()) {
          builder.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"))
        }
        expr.done(JSStubElementTypes.ASSIGNMENT_EXPRESSION)
        return true
      }
      val definitionExpr = builder.mark()
      if (!parseConditionalExpression(false)) {
        definitionExpr.drop()
        expr.drop()
        return false
      }
      if (builder.tokenType === JSTokenTypes.EQ) {
        definitionExpr.done(JSStubElementTypes.DEFINITION_EXPRESSION)
        if (!myIsAction && !myIsJavaScript) {
          builder.error(Angular2Bundle.message("angular.parse.expression.assignment-in-binding"))
        }
        builder.advanceLexer()
        if (!parsePipe()) {
          builder.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"))
        }
        expr.done(JSStubElementTypes.ASSIGNMENT_EXPRESSION)
      }
      else {
        definitionExpr.drop()
        expr.drop()
      }
      return true
    }

    override fun parsePrimaryExpression(): Boolean {
      val firstToken = builder.tokenType
      return if (firstToken === JSTokenTypes.STRING_LITERAL_PART
                 || isEntityStringStart(firstToken)) {
        parsePartialStringLiteral(firstToken)
      }
      else super.parsePrimaryExpression()
    }

    private fun isEntityStringStart(tokenType: IElementType?): Boolean {
      if (tokenType !== Angular2TokenTypes.XML_CHAR_ENTITY_REF) {
        return false
      }
      val text = builder.tokenText
      return text != null && (text == CHAR_ENTITY_QUOT || text == CHAR_ENTITY_APOS)
    }

    override fun isIdentifierToken(tokenType: IElementType?): Boolean {
      return !Angular2TokenTypes.KEYWORDS.contains(tokenType) && super.isIdentifierToken(tokenType)
    }

    override fun getCurrentBinarySignPriority(allowIn: Boolean, advance: Boolean): Int {
      return if (builder.tokenType === JSTokenTypes.OR) {
        -1
      }
      else super.getCurrentBinarySignPriority(allowIn, advance)
    }

    override fun getSafeAccessOperator(): IElementType? {
      return JSTokenTypes.ELVIS
    }

    override fun isReferenceQualifierSeparator(tokenType: IElementType?): Boolean {
      return tokenType === JSTokenTypes.DOT || tokenType === safeAccessOperator
    }

    override fun isPropertyStart(elementType: IElementType?): Boolean {
      if (elementType !== JSTokenTypes.IDENTIFIER
          && elementType !== JSTokenTypes.STRING_LITERAL
          && !Angular2TokenTypes.KEYWORDS.contains(elementType)) {
        builder.error(Angular2Bundle.message("angular.parse.expression.expected-identifier-keyword-or-string"))
        return false
      }
      return true
    }

    override fun parseDialectSpecificMemberExpressionPart(markerRef: Ref<PsiBuilder.Marker>,
                                                          isInExtendsOrImplementsList: Boolean): Boolean {
      if (builder.tokenType === JSTokenTypes.EXCL) {
        builder.advanceLexer()
        val marker = markerRef.get()
        marker.done(JSElementTypes.NOT_NULL_EXPRESSION)
        markerRef.set(marker.precede())
        return true
      }
      return false
    }

    @AdvancesLexer
    override fun parsePropertyNoMarker(property: PsiBuilder.Marker): Boolean {
      val firstToken = builder.tokenType
      val secondToken = builder.lookAhead(1)
      if (myJavaScriptParser!!.isIdentifierName(firstToken) &&  // Angular, in contrast to ECMAScript, accepts Reserved Words here
          (secondToken === JSTokenTypes.COMMA || secondToken === JSTokenTypes.RBRACE)) {
        val ref = builder.mark()
        builder.advanceLexer()
        ref.done(JSElementTypes.REFERENCE_EXPRESSION)
        property.done(ES6StubElementTypes.ES6_PROPERTY)
        return true
      }
      if (Angular2ElementTypes.PROPERTY_NAMES.contains(firstToken)) {
        val errorMessage = validateLiteral()
        advancePropertyName(firstToken)
        if (errorMessage != null) {
          builder.error(errorMessage)
        }
      }
      else {
        builder.error(JavaScriptBundle.message("javascript.parser.message.expected.property.name"))
        builder.advanceLexer()
      }
      parsePropertyInitializer(false)
      property.done(JSStubElementTypes.PROPERTY)
      property.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT, WhitespacesBinders.DEFAULT_RIGHT_BINDER)
      return true
    }

    private fun parsePartialStringLiteral(firstToken: IElementType?): Boolean {
      val mark = builder.mark()
      var currentToken = firstToken
      val literal = StringBuilder()
      var text = getCurrentLiteralPartTokenText(currentToken)
      val singleQuote = (text == null
                         || text.startsWith("'"))
      var first = true
      while (text != null
             && (currentToken === JSTokenTypes.STRING_LITERAL_PART
                 || Angular2TokenTypes.STRING_PART_SPECIAL_SEQ.contains(currentToken))) {
        literal.append(text)
        builder.advanceLexer()
        if (!first
            && (singleQuote && text.endsWith("'") && !text.endsWith("\\'") || !singleQuote && text.endsWith("\"") && !text.endsWith(
            "\\\""))) {
          break
        }
        first = false
        currentToken = builder.tokenType
        text = getCurrentLiteralPartTokenText(currentToken)
      }
      mark.done(JSStubElementTypes.LITERAL_EXPRESSION)
      val errorMessage = validateLiteralText(literal.toString())
      if (errorMessage != null) {
        builder.error(errorMessage)
      }
      return true
    }

    private fun getCurrentLiteralPartTokenText(currentToken: IElementType?): String? {
      val text = builder.tokenText
      if (text != null && currentToken === Angular2TokenTypes.XML_CHAR_ENTITY_REF) {
        if (text == CHAR_ENTITY_APOS) {
          return "'"
        }
        else if (text == CHAR_ENTITY_QUOT) {
          return "\""
        }
      }
      return text
    }

  }

  companion object {
    /*
  Angular Expression AST mapping

  Binary            - JSBinaryExpression
  BindingPipe       - Angular2PipeExpression
  Chain             - Angular2Chain
  Conditional       - JSConditionalExpression
  FunctionCall      - JSCallExpression
  ImplicitReceiver  - JSThisExpression
  KeyedRead         - JSIndexedPropertyAccessExpression
  KeyedWrite        - JSIndexedPropertyAccessExpression
  LiteralArray      - JSArrayLiteralExpression
  LiteralMap        - JSObjectLiteralExpression
  LiteralPrimitive  - JSLiteralExpression
  MethodCall        - JSCallExpression
  NonNullAssert     - JSPostfixExpression
  PrefixNot         - JSPrefixExpression
  PropertyRead      - JSReferenceExpression
  PropertyWrite     - JSReferenceExpression
  Quote             - Angular2Quote
  SafeMethodCall    - JSCallExpression
  SafePropertyRead  - JSReferenceExpression
  */
    private const val CHAR_ENTITY_QUOT: @NonNls String = "&quot;"
    private const val CHAR_ENTITY_APOS: @NonNls String = "&apos;"

    fun parseAction(builder: PsiBuilder, root: IElementType) {
      parseRoot(builder, root, Angular2ElementTypes.ACTION_STATEMENT, true, false) { obj: Angular2StatementParser? -> obj!!.parseChain() }
    }

    fun parseBinding(builder: PsiBuilder, root: IElementType) {
      parseRoot(builder, root, Angular2ElementTypes.BINDING_STATEMENT, false, false) { parser: Angular2StatementParser? ->
        if (!parser!!.parseQuote()) {
          parser.parseChain()
        }
      }
    }

    fun parseTemplateBindings(builder: PsiBuilder, root: IElementType, templateKey: String) {
      parseRoot(builder, root, createTemplateBindingsStatement(templateKey), false, false
      ) { parser: Angular2StatementParser? -> parser!!.parseTemplateBindings(templateKey) }
    }

    fun parseInterpolation(builder: PsiBuilder, root: IElementType) {
      parseRoot(builder, root, Angular2ElementTypes.INTERPOLATION_STATEMENT, false,
                false) { obj: Angular2StatementParser? -> obj!!.parseChain() }
    }

    fun parseSimpleBinding(builder: PsiBuilder, root: IElementType) {
      parseRoot(builder, root, Angular2ElementTypes.SIMPLE_BINDING_STATEMENT, false, true) { parser: Angular2StatementParser? ->
        if (!parser!!.parseQuote()) {
          parser.parseChain()
        }
      }
    }

    private fun parseRoot(builder: PsiBuilder,
                          root: IElementType,
                          statementType: IElementType,
                          isAction: Boolean,
                          isSimpleBinding: Boolean,
                          parseAction: Consumer<in Angular2StatementParser?>) {
      val rootMarker = builder.mark()
      val statementMarker = builder.mark()
      parseAction.consume(Angular2Parser(builder, isAction, isSimpleBinding, false).statementParser)
      statementMarker.done(statementType)
      rootMarker.done(root)
    }

    fun parseJS(builder: PsiBuilder, root: IElementType?) {
      Angular2Parser(builder).parseJS(root)
    }

    private fun finishKey(key: PsiBuilder.Marker, isVariable: Boolean) {
      if (isVariable) {
        key.collapse(JSTokenTypes.IDENTIFIER)
        val preKey = key.precede()
        preKey.done(Angular2StubElementTypes.TEMPLATE_VARIABLE)
        preKey.precede().done(JSStubElementTypes.VAR_STATEMENT)
      }
      else {
        key.done(Angular2ElementTypes.TEMPLATE_BINDING_KEY)
      }
    }
  }
}