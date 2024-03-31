// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely

enum class Angular2TemplateSyntax(val tokenizeExpansionForms: Boolean,
                                  val enableBlockSyntax: Boolean,
                                  val language: WebFrameworkHtmlDialect) {
  V_2(true, false, Angular2HtmlLanguage),
  V_2_NO_EXPANSION_FORMS(false, false, Angular2HtmlLanguage),
  V_17(true, true, Angular17HtmlLanguage),
  ;
  companion object {
    fun of(psiElement: PsiElement): Angular2TemplateSyntax? =
      psiElement.containingFile.language.asSafely<Angular2HtmlDialect>()?.templateSyntax
  }

}