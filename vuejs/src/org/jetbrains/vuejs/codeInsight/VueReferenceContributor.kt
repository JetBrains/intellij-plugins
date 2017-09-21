package org.jetbrains.vuejs.codeInsight

import com.intellij.openapi.paths.PathReferenceManager
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import com.intellij.xml.util.HtmlUtil

/**
 * @author Irina.Chernushina on 9/21/2017.
 */
class VueReferenceContributor: PsiReferenceContributor() {
  companion object {
    private val SRC_ATTRIBUTE = XmlPatterns.xmlAttributeValue("src").inside(XmlPatterns.xmlTag().withLocalName("style"))
  }

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(SRC_ATTRIBUTE, object: PsiReferenceProvider() {
      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val text = ElementManipulators.getValueText(element)
        if (!HtmlUtil.hasHtmlPrefix(text)) {
          return PathReferenceManager.getInstance().createReferences(element, false, false, true)
        }
        return PsiReference.EMPTY_ARRAY
      }
    })
  }
}