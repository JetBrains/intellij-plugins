package org.angular2.library.forms.quickFixes

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbolNameSegment
import com.intellij.webSymbols.inspections.WebSymbolsProblemQuickFixProvider
import com.intellij.webSymbols.references.WebSymbolReferenceProblem
import org.angular2.library.forms.FORM_CONTROL_NAME_ATTRIBUTE
import org.angular2.library.forms.FORM_GROUP_NAME_ATTRIBUTE
import org.angular2.library.forms.scopes.Angular2FormSymbolsScopeInAttributeValue

class Angular2FormsWebSymbolProblemQuickFixProvider : WebSymbolsProblemQuickFixProvider {
  override fun getQuickFixes(element: PsiElement, segment: WebSymbolNameSegment, problemKind: WebSymbolReferenceProblem.ProblemKind): List<LocalQuickFix> {
    if (element is XmlAttributeValue
        && problemKind == WebSymbolReferenceProblem.ProblemKind.UnknownSymbol
        && element.parent.asSafely<XmlAttribute>()?.let { it.name == FORM_GROUP_NAME_ATTRIBUTE || it.name == FORM_CONTROL_NAME_ATTRIBUTE } == true
    ) {
      val formGroup = Angular2FormSymbolsScopeInAttributeValue(element.parent as XmlAttribute)
        .getNearestFormGroup()
      if (formGroup != null) {
        return listOf(CreateFormGroupPropertyQuickFix(
          formGroup, element.value, (element.parent as XmlAttribute).name.removeSuffix("Name").removePrefix("form")))
      }
    }
    return emptyList()
  }
}