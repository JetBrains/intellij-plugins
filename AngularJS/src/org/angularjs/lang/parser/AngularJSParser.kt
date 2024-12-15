// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angularjs.lang.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.lang.javascript.parsing.StatementParser
import com.intellij.psi.tree.IElementType
import org.angularjs.lang.lexer.AngularJSTokenTypes

/**
 * @author Dennis.Ushakov
 */
class AngularJSParser(
  builder: PsiBuilder,
) : JavaScriptParser(
  JavascriptLanguage.INSTANCE,
  builder,
) {
  override val expressionParser: AngularJSExpressionParser =
    AngularJSExpressionParser(this)

  override val statementParser: StatementParser<*>
    get() = AngularJSStatementParser(this)

  override fun isIdentifierName(firstToken: IElementType?): Boolean {
    return super.isIdentifierName(firstToken) || firstToken === AngularJSTokenTypes.THEN
  }

  fun parseAngular(root: IElementType) {
    val rootMarker = builder.mark()
    while (!builder.eof()) {
      statementParser.parseStatement()
    }
    rootMarker.done(root)
  }
}
