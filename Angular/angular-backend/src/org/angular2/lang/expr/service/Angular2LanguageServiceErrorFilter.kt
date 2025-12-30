package org.angular2.lang.expr.service

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceAnnotationResult
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.Companion.ANIMATE_ENTER_ATTR
import org.angular2.codeInsight.attributes.Angular2AttributeValueProvider.Companion.ANIMATE_LEAVE_ATTR
import org.angular2.inspections.Angular2InspectionSuppressor.isUnderscoredLocalVariableIdentifierInAngularTemplate
import kotlin.math.max

object Angular2LanguageServiceErrorFilter {

  fun accept(file: PsiFile, error: JSAnnotationError): Boolean =
    error !is TypeScriptLanguageServiceAnnotationResult
    || when (error.errorCode) {
      TS_ERROR_CODE_UNUSED_DECLARATION -> !shouldIgnoreUnusedDeclarationError(error, file)
      else -> true
    }

  private fun shouldIgnoreUnusedDeclarationError(error: TypeScriptLanguageServiceAnnotationResult, file: PsiFile): Boolean {
    val document = file.viewProvider.document ?: return false
    val elementInfo = getElementInfoInjectionAware(file, document, error) ?: return false
    return isTemplateReferenceVariable(document, elementInfo)
           || isNgAnimateBinding(document, elementInfo)
           || elementInfo.element?.let { isUnderscoredLocalVariableIdentifierInAngularTemplate(it) } == true
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

  fun getElementInfoInjectionAware(
    file: PsiFile,
    document: Document,
    error: JSAnnotationError,
  ): JSLanguageServiceUtil.PsiElementInfo? {
    val result = JSLanguageServiceUtil.getElementInfo(file, document, error) ?: return null
    val range = result.range ?: return result
    val injectedLanguageManager = InjectedLanguageManager.getInstance(file.project)
    val rangeStartElement = injectedLanguageManager.findInjectedElementAt(file, range.startOffset)
                            ?: return result
    val host = injectedLanguageManager.getInjectionHost(rangeStartElement)
               ?: return result
    val rangeEndElement = injectedLanguageManager.findInjectedElementAt(file, range.endOffset - 1)
                          ?: return result
    val injectedFile = rangeStartElement.containingFile
    if (injectedFile != rangeEndElement.containingFile) return result

    val rangeWithinHost = range.shiftLeft(host.textRange.startOffset)
    if (rangeWithinHost.startOffset < 0 || rangeWithinHost.endOffset > host.textLength) return result

    val editableRanges = injectedLanguageManager.intersectWithAllEditableFragments(injectedFile, rangeWithinHost)
    if (editableRanges.size != 1) return result

    val element = TypeScriptPsiUtil.getPsiElementByRange(injectedFile, editableRanges[0])

    return JSLanguageServiceUtil.PsiElementInfo(element, range)
  }

}