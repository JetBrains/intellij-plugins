package org.angular2.lang.expr.service

import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.javascript.service.getElementInfoInjectionAware
import com.intellij.lang.typescript.compiler.languageService.TS_ERROR_IMPLICIT_ANY_TYPE
import com.intellij.lang.typescript.compiler.languageService.TypeScriptAnnotationRangeError
import com.intellij.lang.typescript.compiler.languageService.TypeScriptAnnotationErrorFilter
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.util.asSafely
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.Companion.ANIMATE_ENTER_ATTR
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.Companion.ANIMATE_LEAVE_ATTR
import org.angular2.inspections.Angular2InspectionSuppressor.isUnderscoredLocalVariableIdentifierInAngularTemplate
import org.angular2.lang.expr.Angular2Language
import kotlin.math.max

object Angular2AnnotationErrorFilter : TypeScriptAnnotationErrorFilter() {

  override fun accept(file: PsiFile, error: TypeScriptAnnotationRangeError): Boolean =
    super.accept(file, error) && when (error.errorCode) {
      TS_ERROR_IMPLICIT_ANY_TYPE -> !shouldIgnoreImplicitAnyTypeError(error, file)
      else -> true
    }

  override fun shouldIgnoreUnusedDeclarationError(document: Document, elementInfo: JSLanguageServiceUtil.PsiElementInfo): Boolean =
    super.shouldIgnoreUnusedDeclarationError(document, elementInfo)
    || isTemplateReferenceVariable(document, elementInfo)
    || isNgAnimateBinding(document, elementInfo)
    || elementInfo.element?.let { isUnderscoredLocalVariableIdentifierInAngularTemplate(it) } == true

  private fun shouldIgnoreImplicitAnyTypeError(error: TypeScriptAnnotationRangeError, file: PsiFile): Boolean {
    val document = file.viewProvider.document ?: return false
    val elementInfo = getElementInfoInjectionAware(file, document, error) ?: return false
    return isAngularTemplateArrowFunctionParameter(elementInfo)
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

  private fun isAngularTemplateArrowFunctionParameter(elementInfo: JSLanguageServiceUtil.PsiElementInfo) =
    elementInfo.element
      ?.parent
      ?.asSafely<JSParameter>()
      ?.takeIf { it.language.isKindOf(Angular2Language) }
      ?.declaringFunction
      ?.isArrowFunction == true

}