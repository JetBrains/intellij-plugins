package org.angular2.lang.expr.service

import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.javascript.service.getElementInfoInjectionAware
import com.intellij.lang.typescript.compiler.languageService.TS_ERROR_IMPLICIT_ANY_TYPE
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceAnnotationResult
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceErrorFilter
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.Companion.ANIMATE_ENTER_ATTR
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.Companion.ANIMATE_LEAVE_ATTR
import org.angular2.inspections.Angular2InspectionSuppressor.isUnderscoredLocalVariableIdentifierInAngularTemplate
import kotlin.math.max

object Angular2LanguageServiceErrorFilter : TypeScriptLanguageServiceErrorFilter() {

  override fun invoke(file: PsiFile, error: JSAnnotationError): Boolean =
    super.invoke(file, error) &&
    (error !is TypeScriptLanguageServiceAnnotationResult || when (error.errorCode) {
      TS_ERROR_IMPLICIT_ANY_TYPE -> !shouldIgnoreImplicitAnyTypeError(error, file)
      else -> true
    })

  override fun shouldIgnoreUnusedDeclarationError(document: Document, elementInfo: JSLanguageServiceUtil.PsiElementInfo): Boolean =
    super.shouldIgnoreUnusedDeclarationError(document, elementInfo)
    || isTemplateReferenceVariable(document, elementInfo)
    || isNgAnimateBinding(document, elementInfo)
    || elementInfo.element?.let { isUnderscoredLocalVariableIdentifierInAngularTemplate(it) } == true

  private fun shouldIgnoreImplicitAnyTypeError(error: TypeScriptLanguageServiceAnnotationResult, file: PsiFile): Boolean {
    val document = file.viewProvider.document ?: return false
    val elementInfo = getElementInfoInjectionAware(file, document, error) ?: return false
    return isArrowFunctionParameter(document, elementInfo)
  }

  private fun isTemplateReferenceVariable(document: Document, elementInfo: JSLanguageServiceUtil.PsiElementInfo) =
    // Template reference variable is defined as `#var` or `ref-var`.
    elementInfo.range?.startOffset
      ?.let { offset -> document.getText(TextRange(max(0, offset - 4), offset)) }
      ?.let { it.endsWith("#") || it.endsWith("ref-") } == true

  private fun isNgAnimateBinding(document: Document, elementInfo: JSLanguageServiceUtil.PsiElementInfo) =
    // Angular animation binding is defined as `[animate.enter]` or `[animate.leave]`.
    elementInfo.range
      ?.let { document.getText(it) }
      ?.takeIf { it.startsWith("[") && it.endsWith("]") }
      ?.let { it.substring(1, it.length - 1) }
      .let { it == ANIMATE_ENTER_ATTR || it == ANIMATE_LEAVE_ATTR }

  private fun isArrowFunctionParameter(document: Document, elementInfo: JSLanguageServiceUtil.PsiElementInfo) =
    elementInfo.element?.let { it.parent is JSParameter } == true

}