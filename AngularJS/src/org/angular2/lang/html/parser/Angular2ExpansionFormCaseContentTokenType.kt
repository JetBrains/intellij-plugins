// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser

import com.intellij.html.embedding.HtmlCustomEmbeddedContentTokenType
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lexer.Lexer
import com.intellij.psi.PsiElement
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.XmlASTWrapperPsiElement
import org.angular2.lang.html.lexer.Angular2HtmlLexer

internal class Angular2ExpansionFormCaseContentTokenType private constructor()
  : HtmlCustomEmbeddedContentTokenType("NG:EXPANSION_FORM_CASE_CONTENT_TOKEN", Angular2HtmlLanguage.INSTANCE) {
  override fun createLexer(): Lexer {
    return Angular2HtmlLexer(true, null)
  }

  override fun parse(builder: PsiBuilder) {
    Angular2HtmlParsing(builder).parseExpansionFormContent()
  }

  override fun createPsi(node: ASTNode): PsiElement {
    return XmlASTWrapperPsiElement(node)
  }

  companion object {
    val INSTANCE = Angular2ExpansionFormCaseContentTokenType()
  }
}