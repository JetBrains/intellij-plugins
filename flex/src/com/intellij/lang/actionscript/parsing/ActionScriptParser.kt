// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.parsing

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.parsing.JSParsingContextUtil
import com.intellij.lang.javascript.parsing.JavaScriptParserBase
import com.intellij.lang.javascript.parsing.modifiers.JSModifiersStructure
import com.intellij.lang.javascript.parsing.modifiers.JSModifiersStructure.JSModifiersParseResult
import com.intellij.psi.tree.IElementType
import java.util.*
import java.util.function.Predicate

/**
 * @author Konstantin.Ulitin
 */
class ActionScriptParser(internal val builder: PsiBuilder) {

  val expressionParser: ActionScriptExpressionParser =
    ActionScriptExpressionParser(this)

  val statementParser: ActionScriptStatementParser =
    ActionScriptStatementParser(this)

  val functionParser: ActionScriptFunctionParser =
    ActionScriptFunctionParser(this)

  internal val typeParser: ActionScriptPsiTypeParser by lazy {
    ActionScriptPsiTypeParser(this)
  }

  internal val xmlParser: ActionScriptXmlTokensParser by lazy {
    ActionScriptXmlTokensParser(this)
  }

  fun parseJS(root: IElementType) {
    val rootMarker = builder.mark()
    val forceContext = builder.getUserData(JavaScriptParserBase.FORCE_CONTEXT_KEY)
    if (forceContext != null) {
      if (forceContext == JavaScriptParserBase.ForceContext.Parameter && builder.tokenType === JSTokenTypes.DOT_DOT_DOT) {
        builder.advanceLexer()
      }
      else {
        if (forceContext == JavaScriptParserBase.ForceContext.TypeAllowEmpty && builder.eof()) {
          rootMarker.done(root)
          return
        }
        typeParser.parseType()
      }
    }
    else {
      builder.putUserData(JSParsingContextUtil.ASYNC_METHOD_KEY, JSParsingContextUtil.IS_TOP_LEVEL_ASYNC)
    }
    doParseJS()
    rootMarker.done(root)
  }

  fun isIdentifierName(firstToken: IElementType?): Boolean {
    return JSKeywordSets.IDENTIFIER_NAMES.contains(firstToken)
  }

  fun parseModifiers(
    structure: JSModifiersStructure,
    dropIfEmpty: Boolean,
    isPossibleStateAfterModifiers: Predicate<in PsiBuilder>,
  ): EnumSet<JSModifiersParseResult> {
    val offsetBefore = builder.currentOffset
    var attrList = builder.mark()

    var parseResults = structure.parseOptimistically(builder)

    if (parseResults.contains(JSModifiersParseResult.LEXER_ADVANCED) && !isPossibleStateAfterModifiers.test(builder)) {
      attrList.rollbackTo()
      attrList = builder.mark()
      parseResults = structure.parse(builder, isPossibleStateAfterModifiers)
    }

    val lexerAdvanced = builder.currentOffset > offsetBefore
    if (lexerAdvanced) {
      parseResults.add(JSModifiersParseResult.LEXER_ADVANCED)
    }
    else {
      parseResults.remove(JSModifiersParseResult.LEXER_ADVANCED)
    }
    if (dropIfEmpty && !lexerAdvanced) {
      attrList.drop()
    }
    else {
      attrList.done(functionParser.attributeListElementType)
    }

    return parseResults
  }

  fun isIdentifierToken(tokenType: IElementType?): Boolean {
    return JSKeywordSets.AS_IDENTIFIER_TOKENS_SET.contains(tokenType)
  }

  fun buildTokenElement(type: IElementType) {
    val marker = builder.mark()
    builder.advanceLexer()
    if (builder.tokenType === JSTokenTypes.GENERIC_SIGNATURE_START) {
      typeParser.parseECMA4GenericSignature()
    }
    marker.done(type)
  }

  fun doParseJS() {
    while (!builder.eof()) {
      if (builder.tokenType === JSTokenTypes.AT) {
        builder.advanceLexer()
        statementParser.parseAttributeBody()
      }
      else {
        statementParser.parseStatement()
      }
    }
  }
}
