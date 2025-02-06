package org.angular2.lang.expr.service

import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceAnnotationResult
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import kotlin.math.max

object Angular2LanguageServiceErrorFilter {

  fun accept(file: PsiFile, error: JSAnnotationError): Boolean =
    error !is TypeScriptLanguageServiceAnnotationResult
    || when (error.errorCode) {
      TS_ERROR_CODE_UNUSED_DECLARATION -> !isErrorOnTemplateReferenceVariable(error, file)
      else -> true
    }

  private fun isErrorOnTemplateReferenceVariable(error: TypeScriptLanguageServiceAnnotationResult, file: PsiFile): Boolean {
    val document = file.viewProvider.document ?: return false
    val offset = JSLanguageServiceUtil.getElementInfo(file, document, error)?.range?.startOffset
                     ?: return false
    // Template reference variable is defined as `#var` or `ref-var`.
    return document.getText(TextRange(max(0, offset - 4), offset))
      .let { it.endsWith("#") || it.endsWith("ref-")}
  }

}