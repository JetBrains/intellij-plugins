// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.refs

import com.intellij.lang.Language
import com.intellij.openapi.paths.PathReferenceManager
import com.intellij.patterns.XmlAttributeValuePattern
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.*
import com.intellij.psi.css.resolve.CssReferenceProviderUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ProcessingContext
import com.intellij.xml.util.HtmlUtil.*
import org.jetbrains.vuejs.lang.html.lexer.VueLexerHelper

class VueReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(createSrcAttrValuePattern(STYLE_TAG_NAME), STYLE_REF_PROVIDER)
    registrar.registerReferenceProvider(createSrcAttrValuePattern(TEMPLATE_TAG_NAME), BASIC_REF_PROVIDER)
    registrar.registerReferenceProvider(createSrcAttrValuePattern(SCRIPT_TAG_NAME), BASIC_REF_PROVIDER)
  }

  companion object {

    private val STYLE_REF_PROVIDER = object : PsiReferenceProvider() {
      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val text = ElementManipulators.getValueText(element)
        if (!hasHtmlPrefix(text)) {
          val xmlTag = (element.parent as? XmlAttribute)?.parent
          val langValue = xmlTag?.getAttribute("lang")?.value
          if (langValue != null) {
            val lang = VueLexerHelper.styleViaLang(Language.findLanguageByID("CSS"), langValue)
            val fileType = lang?.associatedFileType
            if (fileType != null) {
              return CssReferenceProviderUtil.getFileReferences(element, true, false, fileType)
            }
          }
          return CssReferenceProviderUtil.getFileReferences(element, true, false)
        }
        return PsiReference.EMPTY_ARRAY
      }
    }

    val BASIC_REF_PROVIDER = object : PsiReferenceProvider() {
      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
        PathReferenceManager.getInstance().createReferences(element, false, false, true)
    }

    private fun createSrcAttrValuePattern(tagName: String): XmlAttributeValuePattern =
      XmlPatterns.xmlAttributeValue(SRC_ATTRIBUTE_NAME).inside(XmlPatterns.xmlTag().withLocalName(tagName))

  }
}
