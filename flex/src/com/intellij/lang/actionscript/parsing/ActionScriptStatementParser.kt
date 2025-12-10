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
import com.intellij.lang.javascript.parsing.FunctionParser
import com.intellij.lang.javascript.parsing.StatementParser
import com.intellij.psi.tree.IElementType

/**
 * @author Konstantin.Ulitin
 */
class ActionScriptStatementParser internal constructor(parser: ActionScriptParser) : StatementParser<ActionScriptParser>(parser) {
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
  override fun parseVarName(variable: PsiBuilder.Marker): Boolean {
    if (!isIdentifierToken(builder.getTokenType())) {
      builder.error(message("javascript.parser.message.expected.variable.name"))
      builder.advanceLexer()
      variable.drop()
      return false
    }

    parser.typeParser.parseQualifiedTypeName()
    return true
  }

  override fun parseStatement() {
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

    super.parseStatement()
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

  override fun startAttributeListOwner(): PsiBuilder.Marker {
    val marker = builder.mark()
    if (!isBlockBodyContext) {
      val modifierListMarker = builder.mark()
      modifierListMarker.done(parser.functionParser.attributeListElementType)
    }
    return marker
  }

  override val variableElementType: IElementType
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

  override fun parseClassOrInterfaceNoMarker(clazz: PsiBuilder.Marker) {
    val methodEmptiness = builder.getUserData(FunctionParser.methodsEmptinessKey)
    try {
      val tokenType = builder.getTokenType()
      LOG.assertTrue(JSTokenTypes.CLASS_KEYWORD === tokenType || JSTokenTypes.INTERFACE_KEYWORD === tokenType)
      if (builder.getTokenType() === JSTokenTypes.INTERFACE_KEYWORD) {
        builder.putUserData(
          FunctionParser.methodsEmptinessKey,
          FunctionParser.MethodEmptiness.ALWAYS
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
      builder.putUserData(FunctionParser.methodsEmptinessKey, methodEmptiness)
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

  override val classElementType: IElementType
    get() = ActionScriptElementTypes.ACTIONSCRIPT_CLASS

  override val classExtendListElementType: IElementType
    get() = JSElementTypes.EXTENDS_LIST

  override fun parseDialectSpecificSourceElements(marker: PsiBuilder.Marker): Boolean {
    val tokenType = builder.getTokenType()
    if (tokenType === JSTokenTypes.LBRACE) {
      parseBlockAndAttachStatementsDirectly()
      marker.done(ActionScriptInternalElementTypes.CONDITIONAL_COMPILE_BLOCK_STATEMENT)
      return true
    }
    else if (tokenType === JSTokenTypes.NAMESPACE_KEYWORD && isECMAL4) {
      if (!parseNamespaceNoMarker(marker)) {
        builder.advanceLexer()
      }
      return true
    }
    return false
  }

  override fun parseForLoopHeader(): Boolean {
    LOG.assertTrue(builder.getTokenType() === JSTokenTypes.FOR_KEYWORD)
    builder.advanceLexer()
    val hasEach = builder.getTokenType() === JSTokenTypes.EACH_KEYWORD
    if (hasEach) {
      builder.advanceLexer()
    }
    return parseForLoopHeaderCondition()
  }

  override fun parseBlock(): Boolean {
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
