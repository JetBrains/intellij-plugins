// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlDialect
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import com.intellij.util.asSafely
import org.angular2.lang.svg.Angular17SvgFileElementType
import org.angular2.lang.svg.Angular17SvgLanguage
import org.angular2.lang.svg.Angular181SvgFileElementType
import org.angular2.lang.svg.Angular181SvgLanguage
import org.angular2.lang.svg.Angular2SvgFileElementType
import org.angular2.lang.svg.Angular2SvgLanguage
import kotlin.text.isDigit

enum class Angular2TemplateSyntax(
  val languageHtml: WebFrameworkHtmlDialect,
  val fileElementTypeHtml: IFileElementType,
  val languageSvg: WebFrameworkHtmlDialect,
  val fileElementTypeSvg: IFileElementType,
  val tokenizeExpansionForms: Boolean,
  val enableBlockSyntax: Boolean = false,
  val enableLetSyntax: Boolean = false,
) {
  V_2(
    Angular2HtmlLanguage, Angular2HtmlFileElementType,
      Angular2SvgLanguage, Angular2SvgFileElementType,
      true),
  V_2_NO_EXPANSION_FORMS(
    Angular2HtmlLanguage, Angular2HtmlFileElementType,
    Angular2SvgLanguage, Angular2SvgFileElementType,
    false),
  V_17(
    Angular17HtmlLanguage, Angular17HtmlFileElementType,
    Angular17SvgLanguage, Angular17SvgFileElementType,
    true, true),
  V_18_1(
    Angular181HtmlLanguage, Angular181HtmlFileElementType,
    Angular181SvgLanguage, Angular181SvgFileElementType,
    true, true, true),
  ;

  val version: String = name.filter { it.isDigit() }

  companion object {
    fun of(psiElement: PsiElement): Angular2TemplateSyntax? =
      psiElement.containingFile.language.asSafely<Angular2HtmlDialect>()?.templateSyntax
  }

}