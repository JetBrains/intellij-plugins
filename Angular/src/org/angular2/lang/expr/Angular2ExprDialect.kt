package org.angular2.lang.expr

import com.intellij.lang.DependentLanguage
import com.intellij.lang.Language
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import org.angular2.lang.Angular2LangUtil.getTemplateSyntax
import org.angular2.lang.html.Angular2TemplateSyntax
import org.jetbrains.annotations.NonNls

abstract class Angular2ExprDialect(
  id: @NonNls String,
  optionHolder: DialectOptionHolder,
  baseLanguage: Language = JavascriptLanguage,
) : JSLanguageDialect(id, optionHolder, baseLanguage), DependentLanguage {

  abstract val templateSyntax: Angular2TemplateSyntax

  abstract fun getKeywords(): TokenSet

  override fun isAtLeast(other: JSLanguageDialect): Boolean {
    return super.isAtLeast(other) || JavaScriptSupportLoader.TYPESCRIPT.isAtLeast(other)
  }

  companion object {

    fun forContext(element: PsiElement): Angular2ExprDialect =
      (Angular2TemplateSyntax.of(element) ?: getTemplateSyntax(element)).expressionLanguage

  }

}