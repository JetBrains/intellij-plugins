package org.angular2.lang.expr.service

import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.lang.javascript.integration.JSAnnotationError
import com.intellij.lang.javascript.psi.JSType.TypeTextFormat.CODE
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.service.JSLanguageService
import com.intellij.lang.javascript.service.highlighting.JSLanguageServiceQuickFixProvider
import com.intellij.lang.javascript.service.highlighting.JSLanguageServiceQuickFixProvider.JSLanguageServiceQuickFix
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceAnnotationResult
import com.intellij.lang.typescript.inspections.TypeScriptValidateTypesInspection
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.asSafely
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.lang.expr.psi.Angular2Binding

class Angular2LanguageServiceQuickFixProvider : JSLanguageServiceQuickFixProvider {

  override fun getQuickFixes(
    service: JSLanguageService,
    serviceError: JSAnnotationError,
    file: PsiFile,
    textRange: TextRange,
  ): List<JSLanguageServiceQuickFix> =
    if (service is Angular2TypeScriptService && serviceError is TypeScriptLanguageServiceAnnotationResult) {
      when (serviceError.errorCode) {
        TS_ERROR_CODE_TYPE_A_IS_NOT_ASSIGNABLE_TO_TYPE_B -> getQuickFixesForWrongBindingType(file, textRange)
        else -> emptyList()
      }
    }
    else
      emptyList()

  private fun getQuickFixesForWrongBindingType(
    file: PsiFile,
    textRange: TextRange,
  ): List<JSLanguageServiceQuickFix> {
    val displayKey = HighlightDisplayKey.find(LocalInspectionTool.getShortName(TypeScriptValidateTypesInspection::class.java.simpleName))
                     ?: return emptyList()
    var attribute = PsiTreeUtil.findElementOfClassAtRange(file, textRange.startOffset, textRange.endOffset, PsiElement::class.java)
                      ?.takeIf { it.elementType == XmlTokenType.XML_NAME }
                      ?.parent
                      ?.asSafely<XmlAttribute>()
                    ?: return emptyList()
    return Angular2Binding.get(attribute)?.expression
             ?.let { JSResolveUtil.getElementJSType(it)?.substitute(it) }
             ?.let { Angular2FixesFactory.getCreateInputTransformFixes(attribute, it.getTypeText(CODE)) }
             ?.map { JSLanguageServiceQuickFix(it, it.name, textRange, displayKey) }
           ?: emptyList()
  }
}