// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.refs

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.paths.StaticPathReferenceProvider
import com.intellij.patterns.XmlAttributeValuePattern
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.*
import com.intellij.psi.css.resolve.CssReferenceProviderUtil.getFileReferenceData
import com.intellij.psi.css.resolve.StylesheetFileReferenceSet
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil.*
import org.jetbrains.vuejs.codeInsight.LANG_ATTRIBUTE_NAME
import org.jetbrains.vuejs.lang.html.lexer.VueTagEmbeddedContentProvider
import org.jetbrains.vuejs.lang.html.psi.VueRefAttribute

class VueReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(STYLE_PATTERN, STYLE_REF_PROVIDER)

    registrar.registerReferenceProvider(createSrcAttrValuePattern(TEMPLATE_TAG_NAME), STATIC_FILE_REF_PROVIDER)
    registrar.registerReferenceProvider(
      XmlPatterns.xmlAttributeValue().withParent(VueRefAttribute::class.java),
      REF_ATTRIBUTE_REF_PROVIDER
    )
  }

  companion object {

    val STYLE_PATTERN = createSrcAttrValuePattern(STYLE_TAG_NAME)

    private val STYLE_REF_PROVIDER = object : PsiReferenceProvider() {
      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val text = ElementManipulators.getValueText(element)
        if (!hasHtmlPrefix(text)) {
          val referenceData = getFileReferenceData(element)
                              ?: return PsiReference.EMPTY_ARRAY
          val suitableFileTypes =
            (element.parent as? XmlAttribute)
              ?.parent
              ?.getAttribute(LANG_ATTRIBUTE_NAME)
              ?.value
              ?.trim()
              ?.let { VueTagEmbeddedContentProvider.styleLanguage(it) }
              ?.associatedFileType
              ?.let { arrayOf(it) }
            ?: emptyArray()

          val referenceSet = StylesheetFileReferenceSet(element, referenceData.first,
                                                        referenceData.second, *suitableFileTypes)
          @Suppress("UNCHECKED_CAST")
          return referenceSet.allReferences as Array<PsiReference>
        }
        return PsiReference.EMPTY_ARRAY
      }
    }

    val STATIC_FILE_REF_PROVIDER = object : PsiReferenceProvider() {
      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val result = mutableListOf<PsiReference>()
        StaticPathReferenceProvider(FileType.EMPTY_ARRAY).apply {
          setEndingSlashNotAllowed(false)
          setRelativePathsAllowed(true)
          createReferences(element, result, false)
        }
        return result.toTypedArray()
      }
    }

    val REF_ATTRIBUTE_REF_PROVIDER = object : PsiReferenceProvider() {
      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
        (element as? XmlAttributeValue)
          ?.parent
          ?.asSafely<VueRefAttribute>()
          ?.implicitElement
          ?.takeIf { it !is VueRefAttribute.VueRefDeclaration }
          ?.let {
            arrayOf(VueRefReference(element, it))
          }
        ?: PsiReference.EMPTY_ARRAY
    }

    private class VueRefReference(element: PsiElement, private val target: PsiElement)
      : PsiReferenceBase<PsiElement>(element, ElementManipulators.getValueTextRange(element), false) {

      override fun resolve(): PsiElement = target

    }

    private fun createSrcAttrValuePattern(tagName: String): XmlAttributeValuePattern =
      XmlPatterns.xmlAttributeValue(SRC_ATTRIBUTE_NAME).withAncestor(2, XmlPatterns.xmlTag().withLocalName(tagName))
  }
}
