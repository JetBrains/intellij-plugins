// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.parsing

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.flex.FlexSupportLoader
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.psi.tree.IElementType

/**
 * @author Konstantin.Ulitin
 */
internal class ActionScriptParser(
  builder: PsiBuilder,
) : JavaScriptParser(
  FlexSupportLoader.ECMA_SCRIPT_L4,
  builder,
) {
  override val expressionParser: ActionScriptExpressionParser =
    ActionScriptExpressionParser(this)

  override val statementParser: ActionScriptStatementParser =
    ActionScriptStatementParser(this)

  override val functionParser: ActionScriptFunctionParser =
    ActionScriptFunctionParser(this)

  override fun isIdentifierToken(tokenType: IElementType?): Boolean {
    return JSKeywordSets.AS_IDENTIFIER_TOKENS_SET.contains(tokenType)
  }

  override fun buildTokenElement(type: IElementType) {
    val marker = builder.mark()
    builder.advanceLexer()
    if (builder.tokenType === JSTokenTypes.GENERIC_SIGNATURE_START) {
      typeParser.parseECMA4GenericSignature()
    }
    marker.done(type)
  }

  override fun doParseJS() {
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
