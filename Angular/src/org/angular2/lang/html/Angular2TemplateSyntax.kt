// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely

enum class Angular2TemplateSyntax(
  val language: WebFrameworkHtmlDialect,
  val tokenizeExpansionForms: Boolean,
  val enableBlockSyntax: Boolean = false,
  val enableLetSyntax: Boolean = false,
) {
  V_2(Angular2HtmlLanguage, true),
  V_2_NO_EXPANSION_FORMS(Angular2HtmlLanguage, false),
  V_17(Angular17HtmlLanguage, true, true),
  V_18_1(Angular181HtmlLanguage, true, true, true),
  ;

  companion object {
    fun of(psiElement: PsiElement): Angular2TemplateSyntax? =
      psiElement.containingFile.language.asSafely<Angular2HtmlDialect>()?.templateSyntax
  }

}