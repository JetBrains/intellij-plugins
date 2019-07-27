// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.refs

import com.intellij.lang.Language
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.*
import com.intellij.psi.css.resolve.CssReferenceProviderUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ProcessingContext
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.lang.html.lexer.VueHandledLexer

class VueReferenceContributor : PsiReferenceContributor() {
  companion object {
    private val SRC_ATTRIBUTE = XmlPatterns.xmlAttributeValue("src").inside(XmlPatterns.xmlTag().withLocalName("style"))
  }

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(SRC_ATTRIBUTE, object : PsiReferenceProvider() {
      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val text = ElementManipulators.getValueText(element)
        if (!HtmlUtil.hasHtmlPrefix(text)) {
          val xmlTag = (element.parent as? XmlAttribute)?.parent
          val langValue = xmlTag?.getAttribute("lang")?.value
          if (langValue != null) {
            val lang = VueHandledLexer.styleViaLang(Language.findLanguageByID("CSS"), langValue)
            val fileType = lang?.associatedFileType
            if (fileType != null) {
              return CssReferenceProviderUtil.getFileReferences(element, true, false, fileType)
            }
          }
          return CssReferenceProviderUtil.getFileReferences(element, true, false)
        }
        return PsiReference.EMPTY_ARRAY
      }
    })
  }
}
