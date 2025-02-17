// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.js

import com.intellij.lang.PsiBuilder
import com.intellij.lang.ecmascript6.parsing.ES6ExpressionParser
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.psi.tree.IElementType
import com.intellij.xml.parsing.XmlParserBundle.message
import com.jetbrains.plugins.jade.psi.JadeElementTypes

internal class JavaScriptInJadeParser(
  builder: PsiBuilder,
) : ES6Parser(
  JavaScriptInJadeLanguageDialect.INSTANCE,
  builder,
) {
  override val expressionParser: ES6ExpressionParser<*> =
    JavaScriptInJadeExpressionParser(this)

  override val statementParser: JavaScriptInJadeStatementParser =
    JavaScriptInJadeStatementParser(this)

  fun parseExpression() {
    val marker = builder.mark()
    expressionParser.parseExpression()
    closeWrapperMarker(marker)
  }

  fun parseForeach() {
    val marker = builder.mark()
    statementParser.parseEachStatement(false)
    closeWrapperMarker(marker)
  }

  fun parseMixinParams() {
    val marker = builder.mark()
    statementParser.parseMixinParameterList(true)
    closeWrapperMarker(marker)
  }

  fun parseMixinParamsValues() {
    val marker = builder.mark()
    statementParser.parseMixinParameterList(false)
    closeWrapperMarker(marker)
  }

  private fun closeWrapperMarker(marker: PsiBuilder.Marker) {
    val errorMarker = builder.mark()

    val somethingLeft = flushTokens()
    if (somethingLeft) {
      errorMarker.error(message("xml.parsing.unexpected.tokens"))
      marker.done(JadeElementTypes.EMBEDDED_STATEMENT_WRAPPER)
    }
    else {
      errorMarker.drop()
      marker.drop()
    }
  }

  private fun flushTokens(): Boolean {
    if (builder.eof()) {
      return false
    }

    while (!builder.eof()) {
      builder.advanceLexer()
    }
    return true
  }

  override fun isIdentifierToken(tokenType: IElementType?): Boolean {
    if (tokenType === JSTokenTypes.EACH_KEYWORD) {
      return false
    }
    return super.isIdentifierToken(tokenType)
  }
}
