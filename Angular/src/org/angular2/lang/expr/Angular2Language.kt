// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr

import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.dialects.JSLanguageFeature
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.angular2.lang.expr.highlighting.Angular2SyntaxHighlighterBase
import org.angular2.lang.expr.lexer.Angular2TokenTypes
import org.angular2.lang.expr.parser.Angular2ExprParserDefinitionBase
import org.angular2.lang.html.Angular2TemplateSyntax

object Angular2Language : Angular2ExprDialect("Angular2", Angular2Dialect()) {

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_2

  override fun getKeywords(): TokenSet =
    Angular2TokenTypes.KEYWORDS

  private class Angular2Dialect : DialectOptionHolder("ANGULAR2", true) {
    override fun defineFeatures(): Set<JSLanguageFeature> {
      return setOf(JSLanguageFeature.IMPORT_DECLARATIONS)
    }
  }
}

val Angular2FileElementType: IFileElementType = JSFileElementType.create(Angular2Language)

class Angular2ParserDefinition : Angular2ExprParserDefinitionBase(Angular2TemplateSyntax.V_2)

class Angular2SyntaxHighlighter : Angular2SyntaxHighlighterBase(Angular2Language)
