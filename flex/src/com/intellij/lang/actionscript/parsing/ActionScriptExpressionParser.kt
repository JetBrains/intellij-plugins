// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.parsing

import com.intellij.lang.PsiBuilder
import com.intellij.lang.WhitespacesBinders
import com.intellij.lang.actionscript.ActionScriptInternalElementTypes
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptParserBundle.message
import com.intellij.lang.javascript.parsing.AdvancesLexer
import com.intellij.lang.javascript.parsing.modifiers.JSModifiersStructure
import com.intellij.lang.javascript.parsing.modifiers.JSModifiersStructure.JSModifiersParseResult
import com.intellij.lang.javascript.parsing.modifiers.JSOneOfModifiersStructure
import com.intellij.lang.javascript.parsing.modifiers.JSOrderedModifiersStructure
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.templateLanguages.DefaultOuterLanguagePatcher
import com.intellij.psi.tree.IElementType
import com.intellij.util.containers.Stack
import java.util.ArrayDeque
import java.util.Deque
import java.util.EnumSet

/**
 * @author Konstantin.Ulitin
 */
class ActionScriptExpressionParser internal constructor(parser: ActionScriptParser) : ActionScriptParserBase(parser) {

  fun parsePrimaryExpressionBase(): Boolean {
    val firstToken = builder.tokenType

    return when {
      firstToken === JSTokenTypes.THIS_KEYWORD -> {
        parser.buildTokenElement(JSElementTypes.THIS_EXPRESSION)
        true
      }

      firstToken === JSTokenTypes.SUPER_KEYWORD -> {
        parser.buildTokenElement(JSElementTypes.SUPER_EXPRESSION)
        true
      }

      isIdentifierToken(firstToken) || firstToken === JSTokenTypes.ANY_IDENTIFIER -> {
        val start = builder.mark()
        val elementType =
          if (StringUtil.equalsIgnoreCase(DefaultOuterLanguagePatcher.OUTER_EXPRESSION_PLACEHOLDER, getTokenCharSequence()))
            JSElementTypes.OUTER_LANGUAGE_ELEMENT_EXPRESSION
          else
            nameReferenceElementType
        parser.buildTokenElement(elementType)
        if (proceedWithNamespaceReference(start, true)) {
          start.precede().done(nameReferenceElementType)
        }

        true
      }

      firstToken === JSTokenTypes.NUMERIC_LITERAL || firstToken === JSTokenTypes.STRING_LITERAL || firstToken === JSTokenTypes.REGEXP_LITERAL || firstToken === JSTokenTypes.NULL_KEYWORD || firstToken === JSTokenTypes.UNDEFINED_KEYWORD || firstToken === JSTokenTypes.FALSE_KEYWORD || firstToken === JSTokenTypes.TRUE_KEYWORD -> {
        val errorMessage = validateLiteral()
        parser.buildTokenElement(JSElementTypes.LITERAL_EXPRESSION)
        if (errorMessage != null) {
          builder.error(errorMessage)
        }
        true
      }

      firstToken === JSTokenTypes.LPAR -> {
        parseParenthesizedExpression()
        true
      }

      firstToken === JSTokenTypes.LBRACKET -> {
        parseArrayLiteralExpression(true)
        true
      }

      firstToken === JSTokenTypes.LBRACE -> {
        parseObjectLiteralExpression()
        true
      }

      firstToken === JSTokenTypes.FUNCTION_KEYWORD -> {
        parser.functionParser.parseFunctionExpression()
        true
      }

      JSTokenTypes.ACCESS_MODIFIERS.contains(firstToken) -> {
        val marker = builder.mark()
        builder.advanceLexer()
        if (JSTokenTypes.COLON_COLON === builder.tokenType) {
          builder.advanceLexer()
          if (isIdentifierToken(builder.tokenType)) builder.advanceLexer()
          marker.done(nameReferenceElementType)
          true
        }
        else {
          marker.drop()
          false
        }
      }

      parser.xmlParser.isXmlTagStart(firstToken) -> {
        parser.xmlParser.parseTag(Stack())
        true
      }

      firstToken === JSTokenTypes.INT_KEYWORD || firstToken === JSTokenTypes.UINT_KEYWORD -> {
        val marker = builder.mark()
        builder.advanceLexer()
        marker.done(nameReferenceElementType) // ?

        true
      }

      else -> false
    }
  }

  fun validateLiteral(): @NlsContexts.ParsingError String? {
    val ttype = builder.tokenType
    if (ttype === JSTokenTypes.STRING_LITERAL) {
      val ttext = checkNotNull(getTokenCharSequence())
      return validateLiteralText(ttext)
    }

    return null
  }


  private var nestedObjectLiterals = 0

  @AdvancesLexer
  private fun parseObjectLiteralExpression() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.LBRACE)
    nestedObjectLiterals++
    if (nestedObjectLiterals > MAX_TREE_DEPTH) {
      builder.advanceLexer()
      return
    }
    val expr = builder.mark()
    builder.advanceLexer()

    var elementType = builder.tokenType

    while (elementType !== JSTokenTypes.RBRACE && elementType != null) {
      if (elementType === JSTokenTypes.LPAR) {
        parseProperty()
      }
      else {
        if (!isPropertyStart(elementType)) {
          builder.error(message("javascript.parser.message.expected.identifier.string.literal.or.numeric.literal"))
          break
        }

        parseProperty()
      }

      while (nestedObjectLiterals > MAX_TREE_DEPTH && builder.tokenType === JSTokenTypes.RBRACE) {
        nestedObjectLiterals--
        builder.advanceLexer()
      }

      var wasCommaBefore = false
      elementType = builder.tokenType
      if (elementType === JSTokenTypes.RBRACE) {
        break
      }
      else if (elementType === JSTokenTypes.COMMA) {
        builder.advanceLexer()
        wasCommaBefore = true
      }
      else {
        builder.error(message("javascript.parser.message.expected.comma"))
        //";" is used in ES6 classes so it is a common typo
        if (elementType === JSTokenTypes.SEMICOLON) {
          builder.advanceLexer()
          wasCommaBefore = true
        }
      }

      elementType = builder.tokenType
      if (elementType === JSTokenTypes.RBRACE) {
        if (wasCommaBefore) break
        builder.error(message("javascript.parser.property.expected"))
      }
      else if (!isPropertyStart(elementType)) {
        break
      }
    }

    checkMatches(builder, JSTokenTypes.RBRACE, "javascript.parser.message.expected.rbrace")
    expr.done(JSElementTypes.OBJECT_LITERAL_EXPRESSION)
    nestedObjectLiterals--
  }

  @AdvancesLexer(onlyIfReturnsTrue = true)
  fun parsePropertyName(): Boolean {
    val tokenType = builder.tokenType
    if (tokenType === JSTokenTypes.LBRACKET) {
      val computedPropertyMarker = builder.mark()
      builder.advanceLexer()
      parseAssignmentExpression(true)
      checkMatches(builder, JSTokenTypes.RBRACKET, "javascript.parser.message.expected.rbracket")
      computedPropertyMarker.done(JSElementTypes.COMPUTED_NAME)
      return true
    }
    else if (JSKeywordSets.PROPERTY_NAMES.contains(tokenType)) {
      advancePropertyName(tokenType)
      return true
    }
    else {
      builder.error(message("javascript.parser.message.expected.property.name"))
      return false
    }
  }

  fun advancePropertyName(tokenType: IElementType?) {
    if (!JSKeywordSets.NON_IDENTIFIER_PROPERTY_NAMES.contains(tokenType)) {
      advanceIdentifier(tokenType)
    }
    else {
      builder.advanceLexer()
    }
  }

  private fun advanceIdentifier(currentTokenType: IElementType?) {
    if (builder.getUserData(PROHIBIT_TOKEN_REMAPPING) != true
        && currentTokenType !== JSTokenTypes.IDENTIFIER
        && currentTokenType !== JSTokenTypes.PRIVATE_IDENTIFIER
    ) {
      builder.remapCurrentTokenAndRestoreOnRollback(JSTokenTypes.IDENTIFIER)
    }
    builder.advanceLexer()
  }

  private fun parseProperty() {
    val mark = builder.mark()
    parsePropertyNoMarker(mark)
  }

  @AdvancesLexer
  private fun parsePropertyNoMarkerBase(property: PsiBuilder.Marker) {
    val firstToken = builder.tokenType
    val secondToken = builder.lookAhead(1)

    if (firstToken === JSTokenTypes.LBRACKET) {
      val lexerAdvanced = parsePropertyName()
      assert(lexerAdvanced) { "must be advanced after LBRACKET" }
      if (builder.tokenType === JSTokenTypes.LPAR || builder.tokenType === JSTokenTypes.LT) {
        parseFunctionPropertyNoMarker(property, true)
      }
      else {
        parsePropertyInitializer(false)
        property.done(JSElementTypes.PROPERTY)
      }
      return
    }
    else if (parseFunctionPropertyNoMarker(property, false)) {
      return
    }
    else if (parser.isIdentifierName(firstToken)
             && (secondToken === JSTokenTypes.COMMA
                 || secondToken === JSTokenTypes.RBRACE
                 || canBeIncompleteProperty(firstToken, secondToken))
    ) {
      val ref = builder.mark()
      builder.advanceLexer()
      ref.done(nameReferenceElementType)
      property.done(JSElementTypes.PROPERTY)
      return
    }
    else if (parser.isIdentifierName(firstToken) && secondToken === JSTokenTypes.EQ) {
      // CoverInitializedName
      val ref = builder.mark()
      builder.advanceLexer()
      ref.done(nameReferenceElementType) // same as shorthanded property, but with initializer
      builder.advanceLexer()
      parseAssignmentExpression(true)
      property.done(JSElementTypes.PROPERTY)
      return
    }
    else if (firstToken === JSTokenTypes.DOT_DOT_DOT) {
      builder.advanceLexer()
      parseAssignmentExpression(true)
      property.done(JSElementTypes.SPREAD_EXPRESSION)
      return
    }

    if (JSKeywordSets.PROPERTY_NAMES.contains(firstToken)) {
      val errorMessage = validateLiteral()
      advancePropertyName(firstToken)
      if (errorMessage != null) {
        builder.error(errorMessage)
      }
    }
    else {
      builder.error(message("javascript.parser.message.expected.property.name"))
      builder.advanceLexer()
    }

    parsePropertyInitializer(JSKeywordSets.IDENTIFIER_NAMES.contains(firstToken))


    property.done(JSElementTypes.PROPERTY)
    property.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT, WhitespacesBinders.DEFAULT_RIGHT_BINDER)

    return
  }

  private fun canBeIncompleteProperty(
    firstToken: IElementType?,
    secondToken: IElementType?,
  ): Boolean {
    // {
    //   y    //<-- here
    //   x
    // }
    if (!hasLineTerminatorAfter(builder)) return false
    return isPropertyNameStart(firstToken) && secondToken !== JSTokenTypes.COLON && secondToken !== JSTokenTypes.EQ
  }

  /** may not advance lexer  */
  private fun parsePropertyInitializer(couldHaveComma: Boolean) {
    if (!checkMatches(builder, JSTokenTypes.COLON, if (couldHaveComma)
        "javascript.parser.message.expected.colon.or.comma"
      else
        "javascript.parser.message.expected.colon")
    ) {
      if (hasLineTerminatorBefore(builder)) return
    }
    if (!parseAssignmentExpression(true)) {
      builder.error(message("javascript.parser.message.expected.expression"))
    }
  }

  @AdvancesLexer(onlyIfReturnsTrue = true)
  private fun parseFunctionPropertyNoMarker(
    property: PsiBuilder.Marker,
    skipName: Boolean,
  ): Boolean {
    var lexerAdvanced = false
    if (!skipName) {
      val modifiers = parser.parseModifiers(FUNCTION_PROPERTY_MODIFIERS, true, ::isFunctionPropertyStart)
      lexerAdvanced = modifiers.contains(JSModifiersParseResult.LEXER_ADVANCED)
      if (!lexerAdvanced && !isFunctionPropertyStart(builder)) {
        val tokenType = builder.tokenType
        if ((tokenType === JSTokenTypes.GET_KEYWORD || tokenType === JSTokenTypes.SET_KEYWORD) &&
            JSKeywordSets.PROPERTY_NAMES.contains(builder.lookAhead(1))
        ) {
          // better error recovery
          parser.functionParser.parseFunctionExpressionAttributeList()
        }
        else {
          return false
        }
      }
      lexerAdvanced = lexerAdvanced or parsePropertyName()
    }

    lexerAdvanced = lexerAdvanced or parser.functionParser.parseParameterListAndBody(property, functionPropertyElementType)
    if (!lexerAdvanced) {
      builder.advanceLexer()
    }
    return true
  }

  private val functionPropertyElementType: IElementType
    get() = JSElementTypes.FUNCTION_PROPERTY

  @AdvancesLexer
  private fun parseArrayLiteralExpression(allowSkippingLeadingElements: Boolean) {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.LBRACKET)
    val marker = builder.mark()
    builder.advanceLexer()
    var commaExpected = false

    while (builder.tokenType !== JSTokenTypes.RBRACKET && !builder.eof()) {
      if (commaExpected) {
        checkMatches(builder, JSTokenTypes.COMMA, "javascript.parser.message.expected.comma")
      }

      if (builder.tokenType === JSTokenTypes.COMMA) {
        if (!allowSkippingLeadingElements) builder.error(message("javascript.parser.message.expected.expression"))
        while (builder.tokenType === JSTokenTypes.COMMA) {
          val emptyMark = builder.mark()
          emptyMark.done(JSElementTypes.EMPTY_EXPRESSION)
          builder.advanceLexer()
        }
      }

      commaExpected = false
      if (builder.tokenType !== JSTokenTypes.RBRACKET) {
        if (!parseArrayElement()) break

        commaExpected = true
      }
    }

    checkMatches(builder, JSTokenTypes.RBRACKET, "javascript.parser.message.expected.rbracket")
    marker.done(JSElementTypes.ARRAY_LITERAL_EXPRESSION)
  }

  private fun parseArrayElement(): Boolean {
    if (!parseAssignmentExpression(true)) {
      builder.error(message("javascript.parser.message.expected.expression"))
      return false
    }
    return true
  }

  @AdvancesLexer
  fun parseParenthesizedExpression() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.LPAR)
    val expr = builder.mark()
    builder.advanceLexer()
    if (!parseExpressionOptional(true)) {
      builder.error(message("javascript.parser.message.expected.expression"))
    }
    checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen")
    expr.done(JSElementTypes.PARENTHESIZED_EXPRESSION)
  }

  @AdvancesLexer(onlyIfReturnsTrue = true)
  fun parseLeftHandSideExpression(
    options: Set<ParseLeftHandSideExpressionOptions>,
  ): Boolean {
    var expr = builder.mark()

    val type = builder.tokenType
    var isNew: Boolean
    if (type === JSTokenTypes.NEW_KEYWORD) {
      isNew = parseNewExpression()
    }
    else if (type === JSTokenTypes.COLON_COLON) {
      builder.advanceLexer()
      parseLeftHandSideExpression(EnumSet.of(ParseLeftHandSideExpressionOptions.ONLY_MEMBER_EXPRESSION))
      expr.done(JSElementTypes.BIND_EXPRESSION)
      expr = expr.precede()
      isNew = false
    }
    else {
      isNew = false
      if (!parsePrimaryExpression()) {
        expr.drop()
        return false
      }
    }

    while (true) {
      var tokenType = builder.tokenType
      var parsedSuccessfully = true
      if (isReferenceQualifierSeparator(tokenType)) {
        builder.advanceLexer()

        var hasAt = false
        if (builder.tokenType === JSTokenTypes.AT) {
          hasAt = true
          builder.advanceLexer()
        }

        tokenType = builder.tokenType

        if (tokenType === JSTokenTypes.LBRACKET && hasAt) {
          continue
        }

        if (parseAfterReferenceQualifierSeparator(expr)) {
          expr = expr.precede()
          continue
        }

        if (tokenType === JSTokenTypes.ANY_IDENTIFIER || parser.isIdentifierName(tokenType)) {
          val identifier = builder.mark()
          builder.advanceLexer()

          if (builder.tokenType === JSTokenTypes.COLON_COLON) {
            identifier.done(nameReferenceElementType)
            proceedWithNamespaceReference(identifier.precede(), true)
          }
          else {
            identifier.drop()
          }
        }
        else {
          builder.error(message("javascript.parser.message.expected.name"))
        }

        expr.done(nameReferenceElementType)
        expr = expr.precede()
      }
      else if (!options.contains(ParseLeftHandSideExpressionOptions.DISALLOW_INDEXER) && tokenType === JSTokenTypes.LBRACKET) {
        builder.advanceLexer()
        parseExpression()
        checkMatches(builder, JSTokenTypes.RBRACKET, "javascript.parser.message.expected.rbracket")
        expr.done(JSElementTypes.INDEXED_PROPERTY_ACCESS_EXPRESSION)
        expr = expr.precede()
      }
      else if (!options.contains(ParseLeftHandSideExpressionOptions.ONLY_MEMBER_EXPRESSION) && tokenType === JSTokenTypes.LPAR) {
        parseArgumentList()
        expr.done((if (isNew) newExpressionElementType else JSElementTypes.CALL_EXPRESSION))
        expr = expr.precede()
        isNew = false
      }
      else if (tokenType === JSTokenTypes.COLON_COLON &&
               !options.contains(ParseLeftHandSideExpressionOptions.ONLY_MEMBER_EXPRESSION)
      ) {
        if (isNew) {
          expr.done(newExpressionElementType)
          expr = expr.precede()
          isNew = false
        }
        builder.advanceLexer()
        if (!parseLeftHandSideExpression(EnumSet.of(ParseLeftHandSideExpressionOptions.ONLY_MEMBER_EXPRESSION))) {
          builder.error(message("javascript.parser.message.expected.expression"))
        }
        expr.done(JSElementTypes.BIND_EXPRESSION)
        expr = expr.precede()
      }
      else {
        parsedSuccessfully = false
      }
      if (!parsedSuccessfully) {
        if (isNew) {
          if (tokenType === JSTokenTypes.LT) {
            builder.error(message("javascript.parser.message.expected.dot"))
          }
          expr.done(newExpressionElementType)
        }
        else {
          expr.drop()
        }
        break
      }
    }

    return true
  }

  private val newExpressionElementType: IElementType
    get() = JSElementTypes.NEW_EXPRESSION

  private val nameReferenceElementType: IElementType
    get() = JSElementTypes.REFERENCE_EXPRESSION

  fun proceedWithNamespaceReference(identifier: PsiBuilder.Marker, expressionContext: Boolean): Boolean {
    if (builder.tokenType === JSTokenTypes.COLON_COLON) {
      builder.advanceLexer()
      identifier.done(JSElementTypes.E4X_NAMESPACE_REFERENCE)
      val tokenType = builder.tokenType

      if (tokenType !== JSTokenTypes.ANY_IDENTIFIER && !isIdentifierToken(tokenType)) {
        if (!expressionContext || tokenType !== JSTokenTypes.LBRACKET) {
          builder.error(
            message("javascript.parser.message.expected.name"))
        }
      }
      else {
        builder.advanceLexer()
      }
      return true
    }
    else {
      identifier.drop()
      return false
    }
  }

  @AdvancesLexer
  private fun parseNewExpression(): Boolean {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.NEW_KEYWORD)

    if (builder.lookAhead(1) === JSTokenTypes.FUNCTION_KEYWORD) {
      val marker = builder.mark()
      builder.advanceLexer()
      parser.functionParser.parseFunctionExpression()
      marker.done(newExpressionElementType)
      return false
    }

    builder.advanceLexer() // new
    val tokenType = builder.tokenType
    if (tokenType === JSTokenTypes.LT) {
      parser.typeParser.parseECMA4GenericSignature()
      if (builder.tokenType === JSTokenTypes.LBRACKET) {
        parseArrayLiteralExpression(false)
      }
      else {
        builder.error(message("javascript.parser.message.expected.lbracket"))
      }
      return true
    }

    if (!parseLeftHandSideExpression(EnumSet.of(ParseLeftHandSideExpressionOptions.ONLY_MEMBER_EXPRESSION))) {
      builder.error(message("javascript.parser.message.expected.expression"))
    }
    while (builder.tokenType === JSTokenTypes.LBRACKET) {
      builder.advanceLexer()
      if (builder.tokenType !== JSTokenTypes.RBRACKET) {
        builder.error(message("javascript.parser.message.expected.rbracket"))
        break
      }
      builder.advanceLexer()
    }
    return true
  }

  @AdvancesLexer
  fun parseArgumentList() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.LPAR)
    val arglist = builder.mark()
    parseArgumentListNoMarker()
    arglist.done(JSElementTypes.ARGUMENT_LIST)
  }

  @AdvancesLexer
  private fun parseArgumentListNoMarker() {
    builder.advanceLexer()
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
          break
        }
      }
      if (!parseArgument()) {
        builder.error(message("javascript.parser.message.expected.expression"))
      }
    }

    checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen")
  }

  private fun parseArgument(): Boolean {
    return parseAssignmentExpression(true)
  }

  fun parseExpression() {
    if (!parseExpressionOptional()) {
      builder.error(message("javascript.parser.message.expected.expression"))
    }
  }

  fun parseAssignmentExpression(allowIn: Boolean): Boolean {
    val expr = builder.mark()
    if (JSElementTypes.ASSIGNMENT_OPERATIONS.contains(builder.tokenType)) {
      builder.error(message("javascript.parser.message.expected.expression"))
      builder.advanceLexer()
      if (!parseAssignmentExpression(allowIn)) {
        builder.error(message("javascript.parser.message.expected.expression"))
      }
      expr.done(JSElementTypes.ASSIGNMENT_EXPRESSION)
      return true
    }

    val definitionExpr = builder.mark()
    if (!parseConditionalExpression(allowIn)) {
      definitionExpr.drop()
      expr.drop()
      return false
    }

    if (JSElementTypes.ASSIGNMENT_OPERATIONS.contains(builder.tokenType)) {
      definitionExpr.done(JSElementTypes.DEFINITION_EXPRESSION)
      builder.advanceLexer()
      if (!parseAssignmentExpression(allowIn)) {
        builder.error(message("javascript.parser.message.expected.expression"))
      }
      expr.done(JSElementTypes.ASSIGNMENT_EXPRESSION)
    }
    else {
      definitionExpr.drop()
      expr.drop()
    }
    return true
  }

  private fun parseConditionalExpression(allowIn: Boolean): Boolean {
    val expr = builder.mark()
    if (!parseBinaryExpression(allowIn)) {
      if (builder.tokenType === JSTokenTypes.QUEST) {
        builder.error(message("javascript.parser.message.expected.expression"))
      }
      else {
        expr.drop()
        return false
      }
    }

    val nextTokenType = builder.tokenType

    if (nextTokenType === JSTokenTypes.QUEST) {
      builder.advanceLexer()
      if (!parseAssignmentExpression(allowIn)) {
        builder.error(message("javascript.parser.message.expected.expression"))
      }
      checkMatches(builder, JSTokenTypes.COLON, "javascript.parser.message.expected.colon")
      if (!parseAssignmentExpression(allowIn)) {
        builder.error(message("javascript.parser.message.expected.expression"))
      }
      expr.done(JSElementTypes.CONDITIONAL_EXPRESSION)
    }
    else {
      expr.drop()
    }
    return true
  }

  private class MarkerData(
    val priority: Int,
    val marker: PsiBuilder.Marker,
    val elementType: IElementType,
  )

  private enum class BinaryParsingState {
    FAIL,
    OK,
    STOP,
    FAIL_AND_STOP,

    ;
  }

  private fun parseBinaryExpression(allowIn: Boolean): Boolean {
    var currentMarker = builder.mark()
    if (!parseExponentialExpression()) {
      currentMarker.drop()
      return false
    }
    if (getCurrentBinarySignPriority(allowIn, false) < 0) { // optimization
      currentMarker.drop()
      return true
    }

    var depth = 0
    val markers: Deque<MarkerData> = ArrayDeque()
    var priority: Int

    var tempStop = false
    while ((getCurrentBinarySignPriority(allowIn, false).also { priority = it }) >= 0) {
      val type = builder.tokenType
      val elementType = getBinaryExpressionElementType(type)
      val depthExceeded = depth >= MAX_TREE_DEPTH
      if (!depthExceeded && !markers.isEmpty() && (markers.peek().priority >= priority || tempStop)) {
        currentMarker.drop()

        var lastPoppedMarker: PsiBuilder.Marker? = null
        while (!markers.isEmpty() && markers.peek().priority > priority) {
          val markerData = markers.pop()
          lastPoppedMarker = markerData.marker
          lastPoppedMarker.done(markerData.elementType)
        }
        val lastPriority = if (markers.isEmpty()) -1 else markers.peek().priority
        if (lastPriority == priority || tempStop && !markers.isEmpty()) {
          val markerData = markers.pop()
          val lastMarker = markerData.marker
          lastMarker.done(markerData.elementType)
          val precede = lastMarker.precede()
          markers.push(MarkerData(priority, precede, elementType))
        }
        else {
          assert(lastPriority < priority)
          checkNotNull(lastPoppedMarker)
          val precede = lastPoppedMarker.precede()
          markers.push(MarkerData(priority, precede, elementType))
        }
      }
      else if (!depthExceeded) {
        markers.push(MarkerData(priority, currentMarker, elementType))
      }

      tempStop = false

      getCurrentBinarySignPriority(allowIn, true)

      if (!depthExceeded) {
        currentMarker = builder.mark()
      }

      val parsingState = parseBinaryRightHandSide()
      if (parsingState == BinaryParsingState.FAIL || parsingState == BinaryParsingState.FAIL_AND_STOP) {
        builder.error(message("javascript.parser.message.expected.expression"))
      }

      if (parsingState == BinaryParsingState.STOP || parsingState == BinaryParsingState.FAIL_AND_STOP) {
        tempStop = true
      }

      depth++
    }

    currentMarker.drop()
    while (!markers.isEmpty()) {
      val markerData = markers.pop()
      val elementType = markerData.elementType
      markerData.marker.done(elementType)
    }

    return true
  }

  private fun parseExponentialExpression(): Boolean {
    val marker = builder.mark()
    var result = parseUnaryExpression()
    if (builder.tokenType === JSTokenTypes.MULTMULT) {
      builder.advanceLexer()
      result = parseExponentialExpression()
      marker.done(JSElementTypes.BINARY_EXPRESSION)
    }
    else {
      marker.drop()
    }
    return result
  }

  private fun getBinaryExpressionElementType(signType: IElementType?): IElementType {
    if (signType === JSTokenTypes.PIPE) {
      return JSElementTypes.PIPE_EXPRESSION
    }
    return JSElementTypes.BINARY_EXPRESSION
  }

  private fun parseBinaryRightHandSide(): BinaryParsingState {
    return if (parseExponentialExpression()) BinaryParsingState.OK else BinaryParsingState.FAIL
  }

  private fun getCurrentBinarySignPriorityBase(allowIn: Boolean, advance: Boolean): Int {
    val tokenType = builder.tokenType

    val result = when {
      tokenType === JSTokenTypes.OROR || tokenType === JSTokenTypes.QUEST_QUEST
        -> 0
      tokenType === JSTokenTypes.ANDAND
        -> 1
      tokenType === JSTokenTypes.OR || tokenType === JSTokenTypes.PIPE
        -> 2
      tokenType === JSTokenTypes.XOR
        -> 3
      tokenType === JSTokenTypes.AND
        -> 4
      JSTokenTypes.EQUALITY_OPERATIONS.contains(tokenType)
        -> 5
      JSTokenTypes.RELATIONAL_OPERATIONS.contains(tokenType)
      && (allowIn || builder.tokenType !== JSTokenTypes.IN_KEYWORD)
        -> 6
      JSTokenTypes.SHIFT_OPERATIONS.contains(tokenType)
        -> 7
      JSTokenTypes.ADDITIVE_OPERATIONS.contains(tokenType)
        -> 8
      JSTokenTypes.MULTIPLICATIVE_OPERATIONS.contains(tokenType)
        -> 9
      else
        -> -1
    }

    if (advance && result >= 0)
      builder.advanceLexer()

    return result
  }

  private fun parseUnaryExpression(): Boolean {
    val tokenType = builder.tokenType
    if (JSTokenTypes.UNARY_OPERATIONS.contains(tokenType)) {
      val expr = builder.mark()
      builder.advanceLexer()
      if (!parseUnaryExpression()) {
        builder.error(message("javascript.parser.message.expected.expression"))
      }
      expr.done(JSElementTypes.PREFIX_EXPRESSION)
      return true
    }
    else {
      return parsePostfixExpression()
    }
  }

  private fun parsePostfixExpression(): Boolean {
    val expr = builder.mark()
    if (!parseLeftHandSideExpression(EnumSet.noneOf(ParseLeftHandSideExpressionOptions::class.java))) {
      expr.drop()
      return false
    }

    val tokenType = builder.tokenType
    if ((tokenType === JSTokenTypes.PLUSPLUS || tokenType === JSTokenTypes.MINUSMINUS)
        && !hasLineTerminatorBefore(builder)
    ) {
      builder.advanceLexer()
      expr.done(JSElementTypes.POSTFIX_EXPRESSION)
    }
    else {
      expr.drop()
    }
    return true
  }

  @JvmOverloads
  fun parseExpressionOptional(allowIn: Boolean = true): Boolean {
    var expr = builder.mark()
    if (!parseAssignmentExpression(allowIn)) {
      expr.drop()
      return false
    }

    if (builder.tokenType === JSTokenTypes.IN_KEYWORD) {
      expr.done(JSElementTypes.DEFINITION_EXPRESSION)
      return true
    }

    var nestingLevel = 0
    while (builder.tokenType === JSTokenTypes.COMMA) {
      builder.advanceLexer()
      if (!parseAssignmentExpression(allowIn)) {
        builder.error(message("javascript.parser.message.expected.expression"))
      }

      if (nestingLevel < MAX_TREE_DEPTH) {
        expr.done(JSElementTypes.COMMA_EXPRESSION)
        expr = expr.precede()
      }

      nestingLevel++
    }

    expr.drop()

    return true
  }

  fun parseSimpleExpression() {
    if (!parseUnaryExpression()) {
      builder.error(message("javascript.parser.message.expected.expression"))
    }
  }

  fun parseScriptExpression(isEmbeddedToken: Boolean) {
    val root = builder.mark()
    checkMatches(builder, JSTokenTypes.XML_LBRACE, "javascript.parser.message.expected.lbrace")
    parseExpression()
    if (isEmbeddedToken) {
      checkMatches(builder, JSTokenTypes.XML_RBRACE, "javascript.parser.message.expected.rbrace")

      while (!builder.eof()) builder.advanceLexer()
    }
    else if (!checkMatches(builder, JSTokenTypes.XML_RBRACE, "javascript.parser.message.expected.rbrace")) {
      while (!builder.eof()) {
        val tokenType = builder.tokenType
        builder.advanceLexer()
        if (tokenType === JSTokenTypes.XML_RBRACE) break
      }
    }
    root.done(JSElementTypes.EMBEDDED_EXPRESSION)
  }

  enum class ParseLeftHandSideExpressionOptions {
    ONLY_MEMBER_EXPRESSION,
    DISALLOW_INDEXER,
  }

  companion object {
    private val LOG: Logger = logger<ActionScriptExpressionParser>()

    @JvmField
    val PROHIBIT_TOKEN_REMAPPING: Key<Boolean> = Key.create("no.token.remapping")

    private val FUNCTION_PROPERTY_MODIFIERS: JSModifiersStructure = JSOrderedModifiersStructure(
      JSOneOfModifiersStructure(JSTokenTypes.GET_KEYWORD, JSTokenTypes.SET_KEYWORD),
    )

    @JvmStatic
    fun validateLiteralText(text: CharSequence): @NlsContexts.ParsingError String? {
      if (lastSymbolEscaped(text)
          || text.startsWith("\"") && (!text.endsWith("\"") || text.length == 1)
          || text.startsWith("'") && (!text.endsWith("'") || text.length == 1)
      ) {
        return message("javascript.parser.message.unclosed.string.literal")
      }

      return null
    }

    private fun lastSymbolEscaped(text: CharSequence): Boolean {
      var escapes = false
      var escaped = true

      for (c in text) {
        if (escapes) {
          escapes = false
          escaped = true
          continue
        }
        if (c == '\\') {
          escapes = true
        }
        escaped = false
      }

      return escapes || escaped
    }
  }


  fun isPropertyStart(elementType: IElementType?): Boolean {
    return JSKeywordSets.AS_IDENTIFIER_TOKENS_SET.contains(elementType) || elementType === JSTokenTypes.STRING_LITERAL || elementType === JSTokenTypes.NUMERIC_LITERAL || elementType === JSTokenTypes.LPAR
  }

  fun isPropertyNameStart(elementType: IElementType?): Boolean {
    return JSKeywordSets.PROPERTY_NAMES.contains(elementType)
  }

  fun isFunctionPropertyStart(builder: PsiBuilder): Boolean {
    return JSKeywordSets.PROPERTY_NAMES.contains(builder.getTokenType()) && builder.lookAhead(1) === JSTokenTypes.LPAR
  }

  fun parsePropertyNoMarker(property: PsiBuilder.Marker) {
    if (builder.getTokenType() === JSTokenTypes.LPAR) {
      parseParenthesizedExpression()
      parsePropertyInitializer(false)
      property.done(JSElementTypes.PROPERTY)
      property.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT, WhitespacesBinders.DEFAULT_RIGHT_BINDER)
      return
    }

    parsePropertyNoMarkerBase(property)
  }

  fun isReferenceQualifierSeparator(tokenType: IElementType?): Boolean {
    return tokenType === JSTokenTypes.DOT || tokenType === JSTokenTypes.COLON_COLON || tokenType === JSTokenTypes.DOT_DOT
  }

  fun parsePrimaryExpression(): Boolean {
    if (builder.getTokenType() === JSTokenTypes.AT) {
      val attrReferenceStartMarker = builder.mark()

      builder.advanceLexer()
      var possibleNamespaceStartMarker: PsiBuilder.Marker? = builder.mark()

      if (!builder.eof()) {
        val tokenType = builder.getTokenType()
        if (tokenType === JSTokenTypes.ANY_IDENTIFIER ||
            isIdentifierToken(tokenType)
        ) {
          builder.advanceLexer()

          if (builder.getTokenType() === JSTokenTypes.COLON_COLON) {
            possibleNamespaceStartMarker!!.done(JSElementTypes.REFERENCE_EXPRESSION)
            possibleNamespaceStartMarker = possibleNamespaceStartMarker.precede()
            proceedWithNamespaceReference(possibleNamespaceStartMarker, true)

            possibleNamespaceStartMarker = null
          }
        }
        else if (tokenType === JSTokenTypes.LBRACKET) {
          builder.advanceLexer()
          parseExpression()
          checkMatches(builder, JSTokenTypes.RBRACKET, "javascript.parser.message.expected.rbracket")
        }
        else {
          builder.error(message("javascript.parser.message.expected.identifier"))
        }
      }

      possibleNamespaceStartMarker?.drop()
      attrReferenceStartMarker.done(JSElementTypes.REFERENCE_EXPRESSION)

      return true
    }

    return parsePrimaryExpressionBase()
  }

  fun getCurrentBinarySignPriority(allowIn: Boolean, advance: Boolean): Int {
    val tokenType = builder.getTokenType()
    if (tokenType === JSTokenTypes.IS_KEYWORD || tokenType === JSTokenTypes.AS_KEYWORD) {
      if (advance) builder.advanceLexer()
      return 10
    }
    return getCurrentBinarySignPriorityBase(allowIn, advance)
  }

  fun parseAfterReferenceQualifierSeparator(expr: PsiBuilder.Marker): Boolean {
    if (builder.getTokenType() !== JSTokenTypes.LPAR) return false
    val requestedArgumentListMarker = builder.mark()
    parseArgumentListNoMarker()
    requestedArgumentListMarker.done(ActionScriptInternalElementTypes.E4X_FILTER_QUERY_ARGUMENT_LIST)
    expr.done(JSElementTypes.CALL_EXPRESSION)
    return true
  }
}
