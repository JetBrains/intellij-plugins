// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.lang.WhitespacesBinders
import com.intellij.lang.javascript.*
import com.intellij.lang.javascript.parsing.AdvancesLexer
import com.intellij.lang.javascript.parsing.ExpressionParser
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.lang.javascript.parsing.StatementParser
import com.intellij.openapi.util.Ref
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.angular2.codeInsight.blocks.*
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.lexer.Angular2TokenTypes
import org.angular2.lang.expr.parser.Angular2ElementTypes.Companion.createTemplateBindingStatement
import org.angular2.lang.expr.parser.Angular2ElementTypes.Companion.createTemplateBindingsStatement
import org.angular2.lang.expr.psi.Angular2TemplateBinding.KeyKind
import org.angular2.templateBindingVarToDirectiveInput
import org.jetbrains.annotations.NonNls

class Angular2Parser private constructor(
  builder: PsiBuilder,
  private val myIsAction: Boolean,
  private val myIsSimpleBinding: Boolean,
  private val myIsJavaScript: Boolean,
) : JavaScriptParser(
  Angular2Language, builder
) {
  constructor(builder: PsiBuilder) : this(builder, false, false, true)

  override val expressionParser: Angular2ExpressionParser =
    Angular2ExpressionParser()

  override val statementParser: Angular2StatementParser =
    Angular2StatementParser(this)

  override fun isIdentifierToken(tokenType: IElementType?): Boolean {
    return JSKeywordSets.TS_IDENTIFIERS_TOKENS_SET.contains(tokenType)
  }

  inner class Angular2StatementParser(parser: Angular2Parser) : StatementParser<Angular2Parser>(parser) {
    fun parseChain(openParens: Int = 0, allowEmpty: Boolean = true) {
      assert(!myIsJavaScript)
      val chain = builder.mark()
      var count = 0
      var openParensCount = openParens
      var parenExpectedReported = false
      while (!builder.eof()) {
        count++
        val expression = builder.mark()
        if (!expressionParser.parseExpressionOptional(false)) {
          builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"))
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
        else if (tokenType == JSTokenTypes.RPAR && openParensCount > 0) {
          builder.advanceLexer()
          openParensCount--
        }
        else if (tokenType != null) {
          if (!parenExpectedReported && openParensCount > 0) {
            builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.rparen", builder.tokenText))
            parenExpectedReported = true
          }
          else {
            builder.error(Angular2Bundle.message("angular.parse.expression.unexpected-token", builder.tokenText!!))
          }
        }
      }
      if (openParensCount > 0) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.missing.rparen"))
      }
      when (count) {
        0 -> {
          if (myIsAction) {
            chain.done(JSElementTypes.EMPTY_STATEMENT)
          }
          else {
            chain.done(JSStubElementTypes.EMPTY_EXPRESSION)
            if (!allowEmpty) {
              chain.precede().error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"))
            }
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

    /*
       Pseudo-grammar for parsing Template Bindings:

       ('as' {var} | {expression} ('as' {var})?)[;,]? (('let' {var} ':'? ('=' {context-key})? | {context-key} ':'? 'as' {var} | {binding-key} ':'? {expression} ('as' {var})?)[;,]?)*
     */
    fun parseTemplateBindings(templateKey: String) {
      var firstBinding = true
      do {
        val binding = builder.mark()
        var isVar = false
        var isLet = true
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
          key = if (isVar) rawKey else templateBindingVarToDirectiveInput(rawKey, templateKey)
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
          isLet = false
        }
        else if (builder.tokenType !== JSTokenTypes.LET_KEYWORD
                 && !expressionParser.parsePipe()) {
          builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"))
        }
        binding.done(createTemplateBindingStatement(key, if (isVar) if (isLet) KeyKind.LET else KeyKind.AS else KeyKind.BINDING, name))
        if (builder.tokenType === JSTokenTypes.AS_KEYWORD && !isVar) {
          val localBinding = builder.mark()
          builder.advanceLexer()
          val letName = parseTemplateBindingKey(true)
          localBinding.done(createTemplateBindingStatement(letName, KeyKind.AS, key))
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
            finishTemplateBindingKey(key, isVariable)
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
      finishTemplateBindingKey(key, isVariable)
      return result.toString()
    }
  }

  inner class Angular2ExpressionParser : ExpressionParser<Angular2Parser>(this@Angular2Parser) {
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
            builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"))
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
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"))
        builder.advanceLexer()
        if (!parsePipe()) {
          builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"))
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
          builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"))
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

    override fun getCurrentBinarySignPriority(allowIn: Boolean, advance: Boolean): Int {
      return if (builder.tokenType === JSTokenTypes.OR) {
        -1
      }
      else super.getCurrentBinarySignPriority(allowIn, advance)
    }

    override val safeAccessOperator: IElementType
      get() = JSTokenTypes.ELVIS

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

    override fun parseDialectSpecificMemberExpressionPart(
      markerRef: Ref<Marker>,
      isInExtendsOrImplementsList: Boolean,
    ): Boolean {
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
    override fun parsePropertyNoMarker(property: Marker): Boolean {
      val firstToken = builder.tokenType
      val secondToken = builder.lookAhead(1)
      if (parser.isIdentifierName(firstToken) &&  // Angular, in contrast to ECMAScript, accepts Reserved Words here
          (secondToken === JSTokenTypes.COMMA || secondToken === JSTokenTypes.RBRACE)) {
        val ref = builder.mark()
        builder.advanceLexer()
        ref.done(JSElementTypes.REFERENCE_EXPRESSION)
        property.done(JSStubElementTypes.PROPERTY)
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
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.property.name"))
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
      mark.done(Angular2StubElementTypes.STRING_PARTS_LITERAL_EXPRESSION)
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
      parseRoot(builder, root, Angular2ElementTypes.ACTION_STATEMENT, true, false) { parser ->
        parser.parseChain()
      }
    }

    fun parseBinding(builder: PsiBuilder, root: IElementType) {
      parseRoot(builder, root, Angular2ElementTypes.BINDING_STATEMENT, false, false) { parser ->
        if (!parser.parseQuote()) {
          parser.parseChain()
        }
      }
    }

    fun parseTemplateBindings(builder: PsiBuilder, root: IElementType, templateKey: String) {
      parseRoot(builder, root, createTemplateBindingsStatement(templateKey), false, false) { parser ->
        parser.parseTemplateBindings(templateKey)
      }
    }

    fun parseInterpolation(builder: PsiBuilder, root: IElementType) {
      parseRoot(builder, root, Angular2ElementTypes.INTERPOLATION_STATEMENT, false, false) { parser ->
        parser.parseChain()
      }
    }

    fun parseSimpleBinding(builder: PsiBuilder, root: IElementType) {
      parseRoot(builder, root, Angular2ElementTypes.SIMPLE_BINDING_STATEMENT, false, true) { parser ->
        if (!parser.parseQuote()) {
          parser.parseChain()
        }
      }
    }

    fun parseBlockParameter(builder: PsiBuilder, root: IElementType, blockName: String, parameterIndex: Int) {
      parseRoot(builder, root, Angular2ElementTypes.BLOCK_PARAMETER_STATEMENT, false, false) { parser ->
        when (blockName) {
          BLOCK_IF -> when (parameterIndex) {
            0 -> parser.parseChain(allowEmpty = false)
            else -> parseAliasAsVariable(builder)
          }
          BLOCK_ELSE_IF, BLOCK_SWITCH, BLOCK_CASE -> when (parameterIndex) {
            0 -> parser.parseChain(allowEmpty = false)
            else -> skipContents(builder)
          }
          BLOCK_FOR -> when (parameterIndex) {
            0 -> parseForLoopMainExpression(builder, parser)
            else -> parseForLoopLetOrTrackExpression(builder, parser)
          }
          BLOCK_DEFER -> parseDeferTrigger(builder, parser)
          BLOCK_PLACEHOLDER -> parsePlaceholderExpression(builder)
          BLOCK_LOADING -> parseLoadingExpression(builder)
          BLOCK_LET -> parseLetDefinition(builder, parser)
          else -> skipContents(builder)
        }
        if (!builder.eof()) {
          builder.error(Angular2Bundle.message("angular.parse.expression.unexpected-token", builder.tokenText!!))
        }
        skipContents(builder)
      }
    }

    private fun parseDeferTrigger(builder: PsiBuilder, parser: Angular2StatementParser) {
      val prefetchPrefix = isParameterName(builder, PARAMETER_PREFIX_PREFETCH)
      val hydratePrefix = !prefetchPrefix && isParameterName(builder, PARAMETER_PREFIX_HYDRATE)
      if (prefetchPrefix || hydratePrefix) {
        val prefix = builder.mark()
        builder.advanceLexer()
        prefix.collapse(Angular2TokenTypes.BLOCK_PARAMETER_PREFIX)
      }
      if (isParameterName(builder, PARAMETER_WHEN)) {
        builder.advanceLexer()
        parser.parseChain(allowEmpty = false)
      }
      else if (isParameterName(builder, PARAMETER_ON)) {
        builder.advanceLexer()
        parseOnTrigger(builder)
      }
      else if (hydratePrefix && isParameterName(builder, PARAMETER_NEVER)) {
        builder.advanceLexer()
        if (!builder.eof()) {
          builder.error(JavaScriptParserBundle.message("javascript.parser.message.unexpected.token", builder.tokenText))
        }
        skipContents(builder)
      }
      else {
        if (prefetchPrefix) {
          builder.error(Angular2Bundle.message("angular.parse.expression.expected-on-when"))
        }
        else if (hydratePrefix) {
          builder.error(Angular2Bundle.message("angular.parse.expression.expected-on-when-never"))
        }
        skipContents(builder)
      }
    }

    private fun parsePlaceholderExpression(builder: PsiBuilder) {
      if (builder.tokenType == Angular2TokenTypes.BLOCK_PARAMETER_NAME) {
        builder.advanceLexer()
        parseDeferredTime(builder)
      }
      else {
        skipContents(builder)
      }
    }

    private fun parseLoadingExpression(builder: PsiBuilder) {
      if (builder.tokenType == Angular2TokenTypes.BLOCK_PARAMETER_NAME) {
        builder.advanceLexer()
        parseDeferredTime(builder)
      }
      else {
        skipContents(builder)
      }
    }

    private fun parseLetDefinition(builder: PsiBuilder, parser: Angular2StatementParser) {
      if (!JSKeywordSets.TS_IDENTIFIERS_TOKENS_SET.contains(builder.tokenType)) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"))
        skipContents(builder)
        return
      }
      val definition = builder.mark()
      builder.advanceLexer()
      if (builder.tokenType != JSTokenTypes.EQ) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.equal"))
        skipContents(builder)
      }
      else {
        builder.advanceLexer()
        if (builder.eof()) {
          builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"))
        }
        else {
          // Parse binding
          parser.parseChain()
        }
      }
      definition.done(Angular2StubElementTypes.BLOCK_PARAMETER_VARIABLE)
      definition.precede().done(JSStubElementTypes.VAR_STATEMENT)
    }

    private fun parseOnTrigger(builder: PsiBuilder) {
      if (!JSKeywordSets.TS_IDENTIFIERS_TOKENS_SET.contains(builder.tokenType)) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"))
        skipContents(builder)
        return
      }
      val identifier = builder.mark()
      builder.advanceLexer()
      identifier.done(JSElementTypes.REFERENCE_EXPRESSION)
      if (builder.eof()) {
        return
      }
      if (builder.tokenType != JSTokenTypes.LPAR) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.lparen", builder.tokenText!!))
      }
      else {
        builder.advanceLexer()
      }
      if (JSKeywordSets.TS_IDENTIFIERS_TOKENS_SET.contains(builder.tokenType)) {
        val ref = builder.mark()
        builder.advanceLexer()
        ref.done(JSElementTypes.REFERENCE_EXPRESSION)
      }
      else if (builder.tokenType == JSTokenTypes.NUMERIC_LITERAL) {
        parseDeferredTime(builder, JSTokenTypes.RPAR)
      }
      else if (!builder.eof()) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.unexpected.token", builder.tokenText))
        skipContents(builder)
        return
      }
      if (builder.tokenType != JSTokenTypes.RPAR) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.rparen"))
      }
      else {
        builder.advanceLexer()
        if (!builder.eof()) {
          builder.error(JavaScriptParserBundle.message("javascript.parser.message.unexpected.token", builder.tokenText))
        }
      }
      skipContents(builder)
    }

    private fun parseDeferredTime(builder: PsiBuilder, endToken: IElementType? = null) {
      if (builder.tokenType != JSTokenTypes.NUMERIC_LITERAL) {
        builder.error(Angular2Bundle.message("angular.parse.expression.expected-numeric-literal"))
        skipContents(builder)
        return
      }
      val timeLiteral = builder.mark()
      if (!builder.tokenText!!.matches(Regex("[0-9]+\\.?[0-9]*"))) {
        val error = builder.mark()
        builder.advanceLexer()
        error.error(Angular2Bundle.message("angular.parse.expression.deferred-time.bad-numeric-format"))
      }
      else {
        if (builder.rawLookup(1) == TokenType.WHITE_SPACE) {
          builder.rawAdvanceLexer(1)
          if (builder.rawLookup(1) == JSTokenTypes.IDENTIFIER)
            builder.error(Angular2Bundle.message("angular.parse.expression.unexpected-whitespace"))
        }
        builder.advanceLexer()
      }
      if (builder.tokenType == JSTokenTypes.IDENTIFIER) {
        val text = builder.tokenText
        if (text != "s" && text != "ms") {
          val error = builder.mark()
          builder.advanceLexer()
          error.error(Angular2Bundle.message("angular.parse.expression.deferred-time.wrong-time-unit"))
        }
        else {
          builder.advanceLexer()
        }
      }
      timeLiteral.done(Angular2StubElementTypes.DEFERRED_TIME_LITERAL_EXPRESSION)
      if (!builder.eof() && builder.tokenType != endToken) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.unexpected.token", builder.tokenText))
        while (!builder.eof() && builder.tokenType != endToken) {
          builder.advanceLexer()
        }
      }
    }

    private fun parseRoot(
      builder: PsiBuilder,
      root: IElementType,
      statementType: IElementType,
      isAction: Boolean,
      isSimpleBinding: Boolean,
      parseAction: (Angular2StatementParser) -> Unit,
    ) {
      val rootMarker = builder.mark()
      val statementMarker = builder.mark()
      parseAction(Angular2Parser(builder, isAction, isSimpleBinding, false).statementParser)
      statementMarker.done(statementType)
      rootMarker.done(root)
    }

    fun parseJS(builder: PsiBuilder, root: IElementType) {
      Angular2Parser(builder).parseJS(root)
    }

    private fun finishTemplateBindingKey(key: Marker, isVariable: Boolean) {
      if (isVariable) {
        completeVar(key, Angular2StubElementTypes.TEMPLATE_VARIABLE)
      }
      else {
        key.done(Angular2ElementTypes.TEMPLATE_BINDING_KEY)
      }
    }

    private fun parseAliasAsVariable(builder: PsiBuilder) {
      if (!isParameterName(builder, "as")) {
        skipContents(builder)
        return
      }
      builder.advanceLexer()
      tryParseParameterVariable(builder)
    }

    private fun parseForLoopMainExpression(builder: PsiBuilder, parser: Angular2StatementParser) {
      var parensCount = 0
      while (builder.tokenType == JSTokenTypes.LPAR) {
        builder.advanceLexer()
        parensCount++
      }
      tryParseParameterVariable(builder)
      if (isSemanticToken(builder, "of")) {
        builder.advanceLexer()
      }
      else {
        builder.error(Angular2Bundle.message("angular.parse.expression.expected-of"))
      }
      parser.parseChain(parensCount, allowEmpty = false)
    }

    private fun parseForLoopLetOrTrackExpression(builder: PsiBuilder, parser: Angular2StatementParser) {
      if (isParameterName(builder, "let")) {
        builder.advanceLexer()
        if (builder.eof()) {
          builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"))
          return
        }
        val stmt = builder.mark()
        while (!builder.eof()) {
          val variable = builder.mark()
          if (!JSKeywordSets.TS_IDENTIFIERS_TOKENS_SET.contains(builder.tokenType)) {
            builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"))
            if (builder.tokenType.let { it != JSTokenTypes.EQ && it != JSTokenTypes.COMMA }) {
              builder.advanceLexer()
            }
          }
          else {
            val identifier = builder.mark()
            builder.advanceLexer()
            identifier.collapse(JSTokenTypes.IDENTIFIER)
          }
          if (builder.tokenType != JSTokenTypes.EQ) {
            builder.error(Angular2Bundle.message("angular.parse.expression.expected-eq"))
          }
          else {
            builder.advanceLexer()
          }
          if (!JSKeywordSets.TS_IDENTIFIERS_TOKENS_SET.contains(builder.tokenType)) {
            if (builder.tokenType == JSTokenTypes.COMMA) {
              builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"))
            }
            else {
              val errorStart = builder.mark()
              builder.advanceLexer()
              errorStart.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"))
            }
          }
          else {
            val identifier = builder.mark()
            builder.advanceLexer()
            identifier.collapse(JSTokenTypes.IDENTIFIER)
            identifier.precede().done(JSElementTypes.REFERENCE_EXPRESSION)
          }
          variable.done(Angular2StubElementTypes.BLOCK_PARAMETER_VARIABLE)
          if (!builder.eof() && builder.tokenType != JSTokenTypes.COMMA) {
            builder.error(Angular2Bundle.message("angular.parse.expression.expected-comma"))
          }
          else {
            builder.advanceLexer()
          }
        }
        stmt.done(JSStubElementTypes.VAR_STATEMENT)
      }
      else if (isParameterName(builder, "track")) {
        builder.advanceLexer()
        parser.parseChain(allowEmpty = false)
      }
      else {
        skipContents(builder)
      }
    }

    private fun isSemanticToken(builder: PsiBuilder, name: String): Boolean =
      builder.tokenType == JSTokenTypes.IDENTIFIER && builder.tokenText == name

    private fun isParameterName(builder: PsiBuilder, name: String): Boolean =
      builder.tokenType == Angular2TokenTypes.BLOCK_PARAMETER_NAME
      && builder.tokenText == name

    private fun tryParseParameterVariable(builder: PsiBuilder): Boolean {
      if (!JSKeywordSets.TS_IDENTIFIERS_TOKENS_SET.contains(builder.tokenType)) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"))
        return false
      }
      else {
        val start = builder.mark()
        builder.advanceLexer()
        completeVar(start, Angular2StubElementTypes.BLOCK_PARAMETER_VARIABLE)
        return true
      }
    }

    private fun completeVar(marker: Marker, variableType: IElementType) {
      marker.collapse(JSTokenTypes.IDENTIFIER)
      val preKey = marker.precede()
      preKey.done(variableType)
      preKey.precede().done(JSStubElementTypes.VAR_STATEMENT)
    }

    private fun skipContents(builder: PsiBuilder) {
      while (!builder.eof())
        builder.advanceLexer()
    }
  }
}