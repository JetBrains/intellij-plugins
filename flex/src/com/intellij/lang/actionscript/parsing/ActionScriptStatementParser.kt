// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.parsing

import com.intellij.lang.PsiBuilder
import com.intellij.lang.WhitespacesBinders
import com.intellij.lang.actionscript.ActionScriptElementTypes
import com.intellij.lang.actionscript.ActionScriptInternalElementTypes
import com.intellij.lang.actionscript.ActionScriptSpecificStubElementTypes
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptParserBundle.message
import com.intellij.lang.javascript.parsing.AdvancesLexer
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.Key
import com.intellij.psi.tree.IElementType

/**
 * @author Konstantin.Ulitin
 */
class ActionScriptStatementParser internal constructor(parser: ActionScriptParser) : ActionScriptParserBase(parser) {

  fun parseStatementBase() {
    val startOffset = builder.currentOffset
    val firstToken = builder.tokenType

    if (firstToken == null) {
      builder.error(message("javascript.parser.message.expected.statement"))
      return
    }

    if (firstToken === JSTokenTypes.LBRACE) {
      parseBlock()
      return
    }

    if (firstToken === JSTokenTypes.SEMICOLON) {
      parseEmptyStatement()
      return
    }

    if (firstToken === JSTokenTypes.IF_KEYWORD) {
      parseIfStatement()
      return
    }

    if (firstToken === JSTokenTypes.DO_KEYWORD
        || firstToken === JSTokenTypes.WHILE_KEYWORD
        || firstToken === JSTokenTypes.FOR_KEYWORD
    ) {
      parseIterationStatement()
      return
    }

    if (firstToken === JSTokenTypes.CONTINUE_KEYWORD) {
      parseContinueStatement()
      return
    }

    if (firstToken === JSTokenTypes.BREAK_KEYWORD) {
      parseBreakStatement()
      return
    }

    if (firstToken === JSTokenTypes.RETURN_KEYWORD) {
      parseReturnStatement()
      return
    }

    if (firstToken === JSTokenTypes.WITH_KEYWORD) {
      parseWithStatement()
      return
    }

    if (firstToken === JSTokenTypes.SWITCH_KEYWORD) {
      parseSwitchStatement()
      return
    }

    if (firstToken === JSTokenTypes.THROW_KEYWORD) {
      parseThrowStatement()
      return
    }

    if (firstToken === JSTokenTypes.TRY_KEYWORD) {
      parseTryStatement()
      return
    }

    if (firstToken === JSTokenTypes.DEBUGGER_KEYWORD) {
      val stmt = builder.mark()
      builder.advanceLexer()
      forceCheckForSemicolon()
      stmt.done(JSElementTypes.DEBUGGER_STATEMENT)
      return
    }

    val functionParser = parser.functionParser
    if ((JSTokenTypes.IDENTIFIER === firstToken
         || isPossibleStartStatementModifier(firstToken)
         || JSTokenTypes.LBRACKET === firstToken
        )
    ) {
      do {
        val marker = builder.mark()
        val methodEmptiness = builder.getUserData(ActionScriptFunctionParser.methodsEmptinessKey)
        if (!functionParser.parseAttributesList()) {
          marker.rollbackTo() // lbracket might be array literal!
          break
        }

        try {
          if (builder.eof()) {
            marker.drop()
            return
          }

          val tokenType = builder.tokenType
          if (tokenType === JSTokenTypes.FUNCTION_KEYWORD) {
            functionParser.parseFunctionNoMarker(ActionScriptFunctionParser.Context.SOURCE_ELEMENT, marker)
            return
          }
          else if (JSTokenTypes.VAR_MODIFIERS_WITHOUT_USING.contains(tokenType)) {
            parseVarStatementNoMarker(false, marker)
            return
          }
          else if (parseDialectSpecificSourceElements(marker)) {
            return
          }
          else if (tokenType === JSTokenTypes.CLASS_KEYWORD || tokenType === JSTokenTypes.INTERFACE_KEYWORD) {
            parseClassOrInterfaceNoMarker(marker)
            return
          }
          else {
            builder.putUserData(ActionScriptFunctionParser.methodsEmptinessKey, null)

            if (firstToken === JSTokenTypes.IDENTIFIER) {
              marker.rollbackTo()
            }
            else if (JSTokenTypes.COLON_COLON === builder.tokenType) {
              marker.rollbackTo()
              if (parseExpressionStatement()) return
            }
            else {
              builder.error(message("javascript.parser.message.expected.declaration"))
              marker.drop()
            }
          }
        }
        finally {
          builder.putUserData(ActionScriptFunctionParser.methodsEmptinessKey, methodEmptiness)
        }
      }
      while (false)
    }
    else if (firstToken === JSTokenTypes.CLASS_KEYWORD || firstToken === JSTokenTypes.INTERFACE_KEYWORD) {
      val marker = startAttributeListOwner()
      parseClassOrInterfaceNoMarker(marker)
      return
    }
    else if (JSTokenTypes.VAR_MODIFIERS_WITHOUT_USING.contains(firstToken)) {
      val marker = startAttributeListOwner()
      parseVarStatementNoMarker(false, marker)
      return
    }
    else if (firstToken === JSTokenTypes.FUNCTION_KEYWORD) {
      val marker = startAttributeListOwner()
      functionParser.parseFunctionNoMarker(ActionScriptFunctionParser.Context.SOURCE_ELEMENT, marker)
      return
    }

    if (builder.tokenType === JSTokenTypes.FUNCTION_KEYWORD) {
      functionParser.parseFunctionDeclaration()
      return
    }

    if (isIdentifierToken(firstToken)) {
      // Try labeled statement:
      val labeledStatement = builder.mark()
      builder.advanceLexer()
      if (builder.tokenType === JSTokenTypes.COLON) {
        builder.advanceLexer()
        parseStatement()
        labeledStatement.done(JSElementTypes.LABELED_STATEMENT)
        return
      }
      else {
        labeledStatement.rollbackTo()
      }
    }

    if (firstToken !== JSTokenTypes.FUNCTION_KEYWORD) {
      if (parseExpressionStatement()) return
    }

    builder.error(message("javascript.parser.message.expected.statement"))
    if (builder.currentOffset == startOffset
        // We don't want to eat dangling right brace if we are within a block.
        // Braces has already been counted and should match.
        && (builder.tokenType !== JSTokenTypes.RBRACE || builder.getUserData(IS_BLOCK_BODY_CONTEXT) == null)
    ) {
      builder.advanceLexer()
    }
  }


  private fun isPossibleStartStatementModifier(token: IElementType?): Boolean {
    return JSTokenTypes.MODIFIERS.contains(token)
  }


  fun parseExpressionStatement(): Boolean {
    // Try expression statement
    val exprStatement = builder.mark()
    if (parser.expressionParser.parseExpressionOptional()) {
      forceCheckForSemicolon()
      exprStatement.done(JSElementTypes.EXPRESSION_STATEMENT)
      exprStatement.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT_NO_EXTRA_LINEBREAK, WhitespacesBinders.DEFAULT_RIGHT_BINDER)
      return true
    }
    else {
      exprStatement.drop()
    }
    return false
  }

  fun parseReferenceList(doneToken: IElementType) {
    val referenceList = builder.mark()
    builder.advanceLexer()

    if (parseReferenceListMember()) {
      while (builder.tokenType === JSTokenTypes.COMMA) {
        builder.advanceLexer()
        if (!isIdentifierToken(builder.tokenType)) {
          builder.error(message("javascript.parser.message.expected.type.name"))
          break
        }
        else {
          if (!parseReferenceListMember())
            break
        }
      }
    }
    else {
      builder.error(message("javascript.parser.message.expected.type.name"))
    }
    referenceList.done(doneToken)
  }

  @AdvancesLexer(onlyIfReturnsTrue = true)
  private fun parseReferenceListMember(): Boolean {
    val startRefMember = builder.mark()
    val result = parser.typeParser.parseQualifiedTypeName()
    if (result) {
      startRefMember.done(JSElementTypes.EXTENDS_LIST_MEMBER)
    }
    else {
      startRefMember.drop()
    }

    return result
  }

  private fun parseTryStatement() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.TRY_KEYWORD)
    val statement = builder.mark()
    builder.advanceLexer()
    parseBlock()

    while (builder.tokenType === JSTokenTypes.CATCH_KEYWORD) {
      parseCatchBlock()
    }

    if (builder.tokenType === JSTokenTypes.FINALLY_KEYWORD) {
      builder.advanceLexer()
      parseBlock()
    }

    statement.done(JSElementTypes.TRY_STATEMENT)
  }

  private fun parseCatchBlock() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.CATCH_KEYWORD)
    val block = builder.mark()
    builder.advanceLexer()
    if (builder.tokenType === JSTokenTypes.LPAR) {
      builder.advanceLexer()
      val identifierType = builder.tokenType

      val parameterElementType = parser.functionParser.parameterType // using JS here breaks PSI actions
      if (isIdentifierToken(identifierType)) {
        val param = builder.mark()
        builder.advanceLexer()

        parser.typeParser.tryParseType()
        param.done(parameterElementType)
      }
      else {
        builder.error(message("javascript.parser.message.expected.parameter.name"))
      }
      checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen")
    }

    parseBlock()

    block.done(JSElementTypes.CATCH_BLOCK)
  }

  private fun parseThrowStatement() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.THROW_KEYWORD)
    val statement = builder.mark()
    builder.advanceLexer()

    if (!hasLineTerminatorBefore(builder)) {
      parser.expressionParser.parseExpression()

      checkForSemicolon()
    }
    else {
      builder.error(message("javascript.parser.message.expected.expression"))
    }
    statement.done(JSElementTypes.THROW_STATEMENT)
  }

  private fun parseSwitchStatement() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.SWITCH_KEYWORD)
    val statement = builder.mark()
    builder.advanceLexer()

    if (checkLParBeforeBlock()) {
      parser.expressionParser.parseExpression()
      checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen")
    }

    val hadLBrace = checkMatches(builder, JSTokenTypes.LBRACE, "javascript.parser.message.expected.lbrace")
    while (builder.tokenType !== JSTokenTypes.RBRACE) {
      if (builder.eof()) {
        builder.error(message("javascript.parser.message.unexpected.end.of.file"))
        statement.done(JSElementTypes.SWITCH_STATEMENT)
        return
      }
      parseCaseOrDefaultClause()
    }

    if (hadLBrace) {
      builder.advanceLexer()
    }
    statement.done(JSElementTypes.SWITCH_STATEMENT)
  }

  private fun checkLParBeforeBlock(): Boolean {
    return checkMatches(builder, JSTokenTypes.LPAR, "javascript.parser.message.expected.lparen")
           || builder.tokenType !== JSTokenTypes.LBRACE
  }

  private fun parseCaseOrDefaultClause() {
    val firstToken = builder.tokenType
    val clause = builder.mark()
    if (firstToken !== JSTokenTypes.CASE_KEYWORD && firstToken !== JSTokenTypes.DEFAULT_KEYWORD) {
      builder.error(message("javascript.parser.message.expected.case.or.default"))
    }
    builder.advanceLexer()
    if (firstToken === JSTokenTypes.CASE_KEYWORD) {
      parser.expressionParser.parseExpression()
    }
    checkMatches(builder, JSTokenTypes.COLON, "javascript.parser.message.expected.colon")
    while (true) {
      val token = builder.tokenType
      if (token == null
          || token === JSTokenTypes.CASE_KEYWORD
          || token === JSTokenTypes.DEFAULT_KEYWORD
          || token === JSTokenTypes.RBRACE
      ) {
        break
      }
      parseStatement()
    }
    clause.done(JSElementTypes.CASE_CLAUSE)
  }

  private fun parseWithStatement() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.WITH_KEYWORD)
    val statement = builder.mark()
    builder.advanceLexer()

    checkMatches(builder, JSTokenTypes.LPAR, "javascript.parser.message.expected.lparen")
    parser.expressionParser.parseExpression()
    checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen")

    parseStatement()

    statement.done(JSElementTypes.WITH_STATEMENT)
  }

  private fun parseReturnStatement() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.RETURN_KEYWORD)
    val statement = builder.mark()
    builder.advanceLexer()
    val hasNewLine = hasLineTerminatorBefore(builder)

    if (!hasNewLine) {
      parser.expressionParser.parseExpressionOptional()
    }
    forceCheckForSemicolon()
    statement.done(JSElementTypes.RETURN_STATEMENT)
  }

  private fun parseBreakStatement() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.BREAK_KEYWORD)
    parseBreakAndContinue(JSElementTypes.BREAK_STATEMENT)
  }

  private fun parseContinueStatement() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.CONTINUE_KEYWORD)
    parseBreakAndContinue(JSElementTypes.CONTINUE_STATEMENT)
  }

  private fun parseBreakAndContinue(elementType: IElementType) {
    val statement = builder.mark()
    builder.advanceLexer()
    if (!hasLineTerminatorBefore(builder) && isIdentifierToken(builder.tokenType)) {
      builder.advanceLexer()
    }

    forceCheckForSemicolon()
    statement.done(elementType)
  }

  private fun parseIterationStatement() {
    val tokenType = builder.tokenType
    if (tokenType === JSTokenTypes.DO_KEYWORD) {
      parseDoWhileStatement()
    }
    else if (tokenType === JSTokenTypes.WHILE_KEYWORD) {
      parseWhileStatement()
    }
    else if (tokenType === JSTokenTypes.FOR_KEYWORD) {
      parseForStatement()
    }
    else {
      LOG.error("Unknown iteration statement")
    }
  }

  private fun parseForStatement() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.FOR_KEYWORD)
    val statement = builder.mark()
    val forin = parseForLoopHeader()

    parseStatement()
    statement.done(if (forin) JSElementTypes.FOR_IN_STATEMENT else JSElementTypes.FOR_STATEMENT)
  }

  private fun parseForLoopHeaderCondition(): Boolean {
    if (!checkLParBeforeBlock()) {
      return false
    }
    val empty: Boolean
    val firstToken = builder.tokenType
    if (JSTokenTypes.VAR_MODIFIERS_WITHOUT_USING.contains(firstToken)) {
      parseVarStatementNoMarker(true, startAttributeListOwner())
      empty = false
    }
    else {
      empty = !parser.expressionParser.parseExpressionOptional(false)
    }

    var forInOrOf = false
    if (builder.tokenType === JSTokenTypes.SEMICOLON) {
      builder.advanceLexer()
      parser.expressionParser.parseExpressionOptional()

      if (builder.tokenType === JSTokenTypes.SEMICOLON) {
        builder.advanceLexer()
      }
      else {
        builder.error(message("javascript.parser.message.expected.semicolon"))
      }
      parser.expressionParser.parseExpressionOptional()
    }
    else if (builder.tokenType === JSTokenTypes.IN_KEYWORD || builder.tokenType === JSTokenTypes.OF_KEYWORD) {
      forInOrOf = true
      if (empty) {
        builder.error(
          message("javascript.parser.message.expected.forloop.left.hand.side.expression.or.variable.declaration"))
      }
      builder.advanceLexer()
      parser.expressionParser.parseExpression()
    }
    else {
      builder.error(message("javascript.parser.message.expected.forloop.in.or.semicolon"))
    }

    checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen")
    return forInOrOf
  }

  private fun parseWhileStatement() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.WHILE_KEYWORD)
    val statement = builder.mark()
    builder.advanceLexer()

    if (checkLParBeforeBlock()) {
      parser.expressionParser.parseExpression()
      checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen")
    }

    parseStatement()
    statement.done(JSElementTypes.WHILE_STATEMENT)
  }

  private fun parseDoWhileStatement() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.DO_KEYWORD)
    val statement = builder.mark()
    builder.advanceLexer()

    parseStatement()
    checkMatches(builder, JSTokenTypes.WHILE_KEYWORD, "javascript.parser.message.expected.while.keyword")
    checkMatches(builder, JSTokenTypes.LPAR, "javascript.parser.message.expected.lparen")
    parser.expressionParser.parseExpression()
    checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen")
    checkForSemicolon()

    statement.done(JSElementTypes.DOWHILE_STATEMENT)
  }

  private fun parseIfStatement() {
    var ifMarkers: MutableList<PsiBuilder.Marker>? = null

    while (true) {
      LOG.assertTrue(builder.tokenType === JSTokenTypes.IF_KEYWORD)
      val ifStatement = builder.mark()
      parseIfStatementHeader()

      parseStatement()

      if (builder.tokenType === JSTokenTypes.ELSE_KEYWORD) {
        builder.advanceLexer()
        if (builder.tokenType === JSTokenTypes.IF_KEYWORD) { // avoid SOE due to nested else if's
          if (ifMarkers == null) ifMarkers = ArrayList()
          if (ifMarkers.size < MAX_TREE_DEPTH) {
            ifMarkers.add(ifStatement)
          }
          else {
            ifStatement.drop()
          }
          continue
        }
        parseStatement()
      }
      ifStatement.done(JSElementTypes.IF_STATEMENT)
      break
    }

    if (ifMarkers != null) {
      for (i in ifMarkers.indices.reversed()) {
        ifMarkers[i].done(JSElementTypes.IF_STATEMENT)
      }
    }
  }

  fun parseIfStatementHeader() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.IF_KEYWORD)
    builder.advanceLexer()

    if (!checkLParBeforeBlock()) {
      return
    }
    parser.expressionParser.parseExpression()

    // handle empty expressions inside
    while (builder.tokenType === JSTokenTypes.OROR || builder.tokenType === JSTokenTypes.EQEQ) {
      builder.advanceLexer()
    }

    checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen")
  }

  private fun parseEmptyStatement() {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.SEMICOLON)
    val statement = builder.mark()
    builder.advanceLexer()
    statement.done(JSElementTypes.EMPTY_STATEMENT)
  }

  /** advances lexer  */
  private fun parseVarStatementNoMarker(
    inForInitializationContext: Boolean,
    variable: PsiBuilder.Marker,
  ) {
    LOG.assertTrue(JSTokenTypes.VAR_MODIFIERS.contains(builder.tokenType))
    if (builder.getUserData(withinInterfaceKey) != null) {
      builder.error(message("interface.should.have.no.variable.declarations"))
    }

    builder.advanceLexer()

    parseVarList(inForInitializationContext)

    if (!inForInitializationContext) {
      forceCheckForSemicolon()
    }

    variable.done(JSElementTypes.VAR_STATEMENT)
    variable.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT, WhitespacesBinders.DEFAULT_RIGHT_BINDER)
  }

  private fun parseVarList(inForInitializationContext: Boolean) {
    var first = true
    while (true) {
      if (first) {
        first = false
      }
      else {
        checkMatches(builder, JSTokenTypes.COMMA, "javascript.parser.message.expected.comma")
      }

      parseVarDeclaration(variableElementType, true, !inForInitializationContext)

      if (builder.tokenType !== JSTokenTypes.COMMA) {
        break
      }
    }
  }

  fun checkForSemicolon(): Boolean {
    val tokenType = builder.tokenType
    if (tokenType === JSTokenTypes.SEMICOLON) {
      builder.advanceLexer()
      return true
    }
    return false
  }

  fun forceCheckForSemicolon() {
    val b = checkForSemicolon()
    if (!b && !hasLineTerminatorBefore(builder)) {
      builder.error(message("javascript.parser.message.expected.newline.or.semicolon"))
    }
  }

  @AdvancesLexer
  fun parseVarDeclaration(
    varType: IElementType,
    allowTypeDeclaration: Boolean,
    allowIn: Boolean,
  ) {
    val variable = builder.mark()
    if (!parseVarName(variable)) return

    if (allowTypeDeclaration) {
      parser.typeParser.tryParseType()
    }

    if (builder.tokenType === JSTokenTypes.EQ) {
      parseVariableInitializer(allowIn)
    }
    variable.done(varType)
    variable.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT, WhitespacesBinders.DEFAULT_RIGHT_BINDER)
  }

  private val isBlockBodyContext: Boolean
    get() = builder.getUserData(IS_BLOCK_BODY_CONTEXT) == true

  private fun parseVariableInitializer(allowIn: Boolean) {
    LOG.assertTrue(builder.tokenType === JSTokenTypes.EQ)
    builder.advanceLexer()
    if (!parser.expressionParser.parseAssignmentExpression(allowIn)) {
      builder.error(message("javascript.parser.message.expected.expression"))
    }
  }

  fun parseFunctionBody(): Boolean {
    return parseBlock()
  }


  private fun parseBlockAndAttachStatementsDirectly() {
    if (builder.tokenType !== JSTokenTypes.LBRACE) {
      builder.error(message("javascript.parser.message.expected.lbrace"))
      return
    }
    builder.advanceLexer()

    while (builder.tokenType !== JSTokenTypes.RBRACE) {
      if (builder.eof()) {
        builder.error(message("javascript.parser.message.missing.rbrace"))
        return
      }
      parseStatement()
    }
    builder.advanceLexer()
  }


  companion object {
    @JvmField
    // protected
    val LOG: Logger = logger<ActionScriptStatementParser>()

    @JvmField
    val withinInterfaceKey: Key<String> = Key.create("within.interface")

    @JvmField
    val IS_BLOCK_BODY_CONTEXT: Key<Boolean> = Key.create("js.block.body.context")
  }


  fun parseAttributeBody() {
    val attribute = builder.mark()
    if (!checkMatches(builder, JSTokenTypes.IDENTIFIER, "javascript.parser.message.expected.identifier")) {
      attribute.drop()
      return
    }
    parser.functionParser.parseAttributeBody()
    attribute.done(JSElementTypes.ATTRIBUTE)
  }

  /** advances lexer  */
  fun parseVarName(variable: PsiBuilder.Marker): Boolean {
    if (!isIdentifierToken(builder.getTokenType())) {
      builder.error(message("javascript.parser.message.expected.variable.name"))
      builder.advanceLexer()
      variable.drop()
      return false
    }

    parser.typeParser.parseQualifiedTypeName()
    return true
  }

  fun parseStatement() {
    val firstToken = builder.getTokenType()

    if (firstToken === JSTokenTypes.PACKAGE_KEYWORD) {
      parsePackage()
      return
    }

    if (firstToken === JSTokenTypes.DEFAULT_KEYWORD) {
      parseDefaultNsStatement()
      return
    }

    if (firstToken === JSTokenTypes.IMPORT_KEYWORD) {
      parseImportStatement()
      return
    }

    if (firstToken === JSTokenTypes.USE_KEYWORD) {
      parseUseNamespaceDirective()
      return
    }

    if (firstToken === JSTokenTypes.GOTO_KEYWORD && isIdentifierToken(builder.lookAhead(1))) {
      val statement = builder.mark()
      builder.advanceLexer()
      builder.advanceLexer()
      checkForSemicolon()
      statement.done(ActionScriptInternalElementTypes.GOTO_STATEMENT)
      return
    }

    if (firstToken === JSTokenTypes.INCLUDE_KEYWORD) {
      parseIncludeDirective()
      return
    }

    if (firstToken === JSTokenTypes.NAMESPACE_KEYWORD) {
      val marker = startAttributeListOwner()
      if (parseNamespaceNoMarker(marker)) {
        return
      }
    }

    parseStatementBase()
  }

  fun parseIncludeDirective() {
    LOG.assertTrue(builder.getTokenType() === JSTokenTypes.INCLUDE_KEYWORD)
    val useNSStatement = builder.mark()
    builder.advanceLexer()
    checkMatches(builder, JSTokenTypes.STRING_LITERAL, "javascript.parser.message.expected.string.literal")
    checkForSemicolon()

    useNSStatement.done(ActionScriptElementTypes.INCLUDE_DIRECTIVE)
  }

  private fun parseNamespaceNoMarker(useNSStatement: PsiBuilder.Marker): Boolean {
    LOG.assertTrue(builder.getTokenType() === JSTokenTypes.NAMESPACE_KEYWORD)

    builder.advanceLexer()
    if (!JSKeywordSets.IDENTIFIER_TOKENS_SET.contains(builder.getTokenType())) {
      useNSStatement.rollbackTo()
      return false
    }

    parser.typeParser.parseQualifiedTypeName()

    if (builder.getTokenType() === JSTokenTypes.EQ) {
      builder.advanceLexer()

      val tokenType = builder.getTokenType()

      if (tokenType === JSTokenTypes.PUBLIC_KEYWORD) {
        builder.advanceLexer()
      }
      else if (tokenType === JSTokenTypes.STRING_LITERAL || tokenType === JSTokenTypes.IDENTIFIER) {
        parser.expressionParser.parseExpression()
      }
      else {
        builder.error(message("javascript.parser.message.expected.string.literal"))
      }
    }
    checkForSemicolon()
    useNSStatement.done(ActionScriptElementTypes.NAMESPACE_DECLARATION)
    useNSStatement.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT, WhitespacesBinders.DEFAULT_RIGHT_BINDER)
    return true
  }

  private fun parseDefaultNsStatement() {
    LOG.assertTrue(builder.getTokenType() === JSTokenTypes.DEFAULT_KEYWORD)
    val statementMarker = builder.mark()
    val marker = builder.mark()
    builder.advanceLexer()

    if (builder.getTokenType() === JSTokenTypes.IDENTIFIER &&
        "xml" == builder.getTokenText()
    ) {
      builder.advanceLexer()

      if (checkMatches(builder, JSTokenTypes.NAMESPACE_KEYWORD, "javascript.parser.message.expected.namespace")) {
        if (checkMatches(builder, JSTokenTypes.EQ, "javascript.parser.message.expected.equal")) {
          parser.expressionParser.parseExpression()
        }
      }
    }
    else {
      builder.error(message("javascript.parser.message.expected.xml"))
    }
    marker.done(JSElementTypes.ASSIGNMENT_EXPRESSION)
    checkForSemicolon()
    statementMarker.done(JSElementTypes.EXPRESSION_STATEMENT)
  }

  fun startAttributeListOwner(): PsiBuilder.Marker {
    val marker = builder.mark()
    if (!isBlockBodyContext) {
      val modifierListMarker = builder.mark()
      modifierListMarker.done(parser.functionParser.attributeListElementType)
    }
    return marker
  }

  val variableElementType: IElementType
    get() = if (isBlockBodyContext) ActionScriptSpecificStubElementTypes.LOCAL_VARIABLE else ActionScriptElementTypes.ACTIONSCRIPT_VARIABLE

  fun parseUseNamespaceDirective() {
    val useNSStatement = builder.mark()
    builder.advanceLexer()

    if (builder.getTokenType() !== JSTokenTypes.NAMESPACE_KEYWORD) {
      builder.error(message("javascript.parser.message.expected.namespace"))
    }
    else {
      builder.advanceLexer()

      if (!parser.typeParser.parseQualifiedTypeName()) {
        builder.error(message("javascript.parser.message.expected.typename.or.*"))
      }

      while (builder.getTokenType() === JSTokenTypes.COMMA) {
        builder.advanceLexer()
        if (!parser.typeParser.parseQualifiedTypeName()) {
          builder.error(message("javascript.parser.message.expected.typename.or.*"))
          break
        }
      }
    }
    checkForSemicolon()
    useNSStatement.done(ActionScriptElementTypes.USE_NAMESPACE_DIRECTIVE)
  }

  private fun parsePackage() {
    val _package = builder.mark()
    builder.advanceLexer()
    if (builder.getTokenType() === JSTokenTypes.IDENTIFIER) {
      parser.typeParser.parseQualifiedTypeName()
    }

    if (builder.getTokenType() !== JSTokenTypes.LBRACE) {
      builder.error(message("javascript.parser.message.expected.name.or.lbrace"))
    }
    else {
      parseBlockAndAttachStatementsDirectly()
    }
    _package.done(JSElementTypes.PACKAGE_STATEMENT)
  }

  private fun parseImportStatement() {
    val importStatement = builder.mark()
    try {
      builder.advanceLexer()

      val nsAssignment = builder.mark()
      if (!parser.typeParser.parseQualifiedTypeName(true)) {
        builder.error(message("javascript.parser.message.expected.typename.or.*"))
        nsAssignment.drop()
        return
      }

      if (builder.getTokenType() === JSTokenTypes.EQ) {
        builder.advanceLexer()
        if (!parser.typeParser.parseQualifiedTypeName()) {
          builder.error(message("javascript.parser.message.expected.typename.or.*"))
        }

        nsAssignment.done(JSElementTypes.ASSIGNMENT_EXPRESSION)
      }
      else {
        nsAssignment.drop()
      }

      checkForSemicolon()
    }
    finally {
      importStatement.done(JSElementTypes.IMPORT_STATEMENT)
    }
  }

  fun parseClassOrInterfaceNoMarker(clazz: PsiBuilder.Marker) {
    val methodEmptiness = builder.getUserData(ActionScriptFunctionParser.methodsEmptinessKey)
    try {
      val tokenType = builder.getTokenType()
      LOG.assertTrue(JSTokenTypes.CLASS_KEYWORD === tokenType || JSTokenTypes.INTERFACE_KEYWORD === tokenType)
      if (builder.getTokenType() === JSTokenTypes.INTERFACE_KEYWORD) {
        builder.putUserData(
          ActionScriptFunctionParser.methodsEmptinessKey,
          ActionScriptFunctionParser.MethodEmptiness.ALWAYS
        )
        builder.putUserData(withinInterfaceKey, "")
      }

      builder.advanceLexer()

      if (isIdentifierToken(builder.getTokenType())) {
        parsePossiblyQualifiedName()
      }
      else {
        builder.error(message("javascript.parser.message.expected.typename.or.*"))
      }

      if (builder.getTokenType() === JSTokenTypes.EXTENDS_KEYWORD) {
        parseReferenceList(JSElementTypes.EXTENDS_LIST)
      }

      if (builder.getTokenType() === JSTokenTypes.IMPLEMENTS_KEYWORD) {
        parseReferenceList(JSElementTypes.IMPLEMENTS_LIST)
      }

      parseBlockAndAttachStatementsDirectly()
      clazz.done(ActionScriptElementTypes.ACTIONSCRIPT_CLASS)
      clazz.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT, WhitespacesBinders.DEFAULT_RIGHT_BINDER)
    }
    finally {
      builder.putUserData(ActionScriptFunctionParser.methodsEmptinessKey, methodEmptiness)
      builder.putUserData(withinInterfaceKey, null)
    }
  }

  fun parsePossiblyQualifiedName() {
    var validQualifier = JSKeywordSets.IDENTIFIER_NAMES.contains(builder.getTokenType())
    var qualifier = builder.mark()
    builder.advanceLexer()

    // ECMAScriptLevelFourParsingTest.testOldAs2Code
    while (validQualifier && builder.getTokenType() === JSTokenTypes.DOT) {
      qualifier.done(JSElementTypes.REFERENCE_EXPRESSION)
      qualifier = qualifier.precede()
      builder.advanceLexer()
      validQualifier = JSKeywordSets.IDENTIFIER_NAMES.contains(builder.getTokenType())
      if (!validQualifier) {
        builder.error(message("javascript.parser.message.expected.identifier"))
      }
      builder.advanceLexer()
    }
    qualifier.drop()
  }


  fun parseDialectSpecificSourceElements(marker: PsiBuilder.Marker): Boolean {
    val tokenType = builder.getTokenType()
    if (tokenType === JSTokenTypes.LBRACE) {
      parseBlockAndAttachStatementsDirectly()
      marker.done(ActionScriptInternalElementTypes.CONDITIONAL_COMPILE_BLOCK_STATEMENT)
      return true
    }
    else if (tokenType === JSTokenTypes.NAMESPACE_KEYWORD) {
      if (!parseNamespaceNoMarker(marker)) {
        builder.advanceLexer()
      }
      return true
    }
    return false
  }

  fun parseForLoopHeader(): Boolean {
    LOG.assertTrue(builder.getTokenType() === JSTokenTypes.FOR_KEYWORD)
    builder.advanceLexer()
    val hasEach = builder.getTokenType() === JSTokenTypes.EACH_KEYWORD
    if (hasEach) {
      builder.advanceLexer()
    }
    return parseForLoopHeaderCondition()
  }

  fun parseBlock(): Boolean {
    if (builder.getTokenType() !== JSTokenTypes.LBRACE) {
      builder.error(message("javascript.parser.message.expected.lbrace"))
      return false
    }
    val mark = builder.mark()
    val wasBlockBodyContext: Boolean? = builder.getUserData(IS_BLOCK_BODY_CONTEXT)
    builder.putUserData(IS_BLOCK_BODY_CONTEXT, true)
    parseBlockAndAttachStatementsDirectly()
    builder.putUserData(IS_BLOCK_BODY_CONTEXT, wasBlockBodyContext)
    mark.done(ActionScriptElementTypes.BLOCK_STATEMENT)
    return true
  }
}
