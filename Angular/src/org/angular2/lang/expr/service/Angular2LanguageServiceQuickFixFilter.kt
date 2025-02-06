package org.angular2.lang.expr.service

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceAnnotationResult
import com.intellij.lang.typescript.compiler.languageService.codeFixes.TypeScriptLanguageServiceFix
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.Angular2HtmlDialect

object Angular2LanguageServiceQuickFixFilter {

  fun accept(file: PsiFile, element: PsiElement?, error: JSAnnotationError, action: IntentionAction): Boolean =
    error !is TypeScriptLanguageServiceAnnotationResult
    || action !is TypeScriptLanguageServiceFix
    || when (error.errorCode) {
      TS_ERROR_CODE_UNUSED_DECLARATION -> {
        !file.language.let { it is Angular2HtmlDialect || it is Angular2Language }
        && element?.parent !is JSLiteralExpression
      }
      else -> true
    }

}