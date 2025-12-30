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

object Angular20Language : Angular2ExprDialect("Angular20", Angular20Dialect(), Angular2Language) {

  override val templateSyntax: Angular2TemplateSyntax
    get() = Angular2TemplateSyntax.V_20

  override fun getKeywords(): TokenSet =
    Angular2TokenTypes.KEYWORDS_20

  private class Angular20Dialect : DialectOptionHolder("ANGULAR20", true) {
    override fun defineFeatures(): Set<JSLanguageFeature> {
      return setOf(JSLanguageFeature.IMPORT_DECLARATIONS)
    }
  }
}

val Angular20FileElementType: IFileElementType = JSFileElementType.create(Angular20Language)

class Angular20ParserDefinition : Angular2ExprParserDefinitionBase(Angular2TemplateSyntax.V_20)

class Angular20SyntaxHighlighter : Angular2SyntaxHighlighterBase(Angular20Language)