// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.jsx.parser

import com.intellij.html.embedding.HtmlCustomEmbeddedContentTokenType
import com.intellij.javascript.web.html.XmlASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.parsing.JavaScriptParserBase
import com.intellij.lexer.Lexer
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

class AstroJsxBaseExpressionTokenType(debugName: String,
                                      private val parseMethod: AstroJsxParser.(IElementType) -> Unit)
  : HtmlCustomEmbeddedContentTokenType("ASTRO:$debugName", JavaScriptSupportLoader.TYPESCRIPT, true) {

  final override fun parse(builder: PsiBuilder) {
    val parser = AstroJsxParser(builder)
    builder.putUserData(JavaScriptParserBase.JS_DIALECT_KEY, parser.dialect)
    parseMethod(parser, this)
  }

  override fun createLexer(): Lexer =
    JSFlexAdapter(JavaScriptSupportLoader.TYPESCRIPT.optionHolder)

  override fun createPsi(node: ASTNode): PsiElement =
    XmlASTWrapperPsiElement(node)

  companion object {

    val AstroJsxExpressionTokenType =
      AstroJsxBaseExpressionTokenType("EXPRESSION", AstroJsxParser::parseExpression)

    val AstroJsxExpressionAttributeTokenType =
      AstroJsxBaseExpressionTokenType("EXPRESSION_ATTRIBUTE", AstroJsxParser::parseExpression)

    val AstroJsxShorthandAttributeTokenType =
      AstroJsxBaseExpressionTokenType("SHORTHAND_ATTRIBUTE", AstroJsxParser::parseExpression)

    val AstroJsxSpreadAttributeTokenType =
      AstroJsxBaseExpressionTokenType("SPREAD_ATTRIBUTE", AstroJsxParser::parseSpreadAttribute)

    val AstroJsxTemplateLiteralAttributeTokenType =
      AstroJsxBaseExpressionTokenType("TEMPLATE_LITERAL_ATTRIBUTE", AstroJsxParser::parseExpression)

  }

}