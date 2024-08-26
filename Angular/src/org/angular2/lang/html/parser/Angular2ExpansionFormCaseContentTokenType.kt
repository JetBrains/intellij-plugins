// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser

import com.intellij.html.embedding.HtmlCustomEmbeddedContentTokenType
import com.intellij.javascript.web.html.XmlASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lexer.Lexer
import com.intellij.psi.PsiElement
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.html.lexer.Angular2HtmlLexer

internal class Angular2ExpansionFormCaseContentTokenType private constructor(private val templateSyntax: Angular2TemplateSyntax)
  : HtmlCustomEmbeddedContentTokenType("NG:EXPANSION_FORM_CASE_CONTENT_TOKEN", Angular2HtmlLanguage) {
  override fun createLexer(): Lexer {
    return Angular2HtmlLexer(false, templateSyntax, null)
  }

  override fun parse(builder: PsiBuilder) {
    Angular2HtmlParsing(templateSyntax, builder).parseExpansionFormContent()
  }

  override fun createPsi(node: ASTNode): PsiElement {
    return XmlASTWrapperPsiElement(node)
  }

  companion object {
    private val INSTANCE_V_2 = Angular2ExpansionFormCaseContentTokenType(Angular2TemplateSyntax.V_2)
    private val INSTANCE_V_2_NO_EXPANSION_FORMS = Angular2ExpansionFormCaseContentTokenType(Angular2TemplateSyntax.V_2_NO_EXPANSION_FORMS)
    private val INSTANCE_V_17 = Angular2ExpansionFormCaseContentTokenType(Angular2TemplateSyntax.V_17)
    private val INSTANCE_V_18_1 = Angular2ExpansionFormCaseContentTokenType(Angular2TemplateSyntax.V_18_1)

    fun get(templateSyntaxVersion: Angular2TemplateSyntax): Angular2ExpansionFormCaseContentTokenType =
      when (templateSyntaxVersion) {
        Angular2TemplateSyntax.V_2 -> INSTANCE_V_2
        Angular2TemplateSyntax.V_2_NO_EXPANSION_FORMS -> INSTANCE_V_2_NO_EXPANSION_FORMS
        Angular2TemplateSyntax.V_17 -> INSTANCE_V_17
        Angular2TemplateSyntax.V_18_1 -> INSTANCE_V_18_1
      }
  }
}