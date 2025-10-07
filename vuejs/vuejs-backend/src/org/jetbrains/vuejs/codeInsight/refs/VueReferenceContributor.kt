// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight.refs

import com.intellij.lang.javascript.frameworks.jsx.JSXReferenceContributor
import com.intellij.lang.javascript.frameworks.jsx.JSXReferenceContributor.createPathReferenceProvider
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.paths.StaticPathReferenceProvider
import com.intellij.patterns.XmlAttributeValuePattern
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.*
import com.intellij.psi.css.resolve.CssReferenceProviderUtil.getFileReferenceData
import com.intellij.psi.css.resolve.StylesheetFileReferenceSet
import com.intellij.psi.filters.position.FilterPattern
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil.*
import org.jetbrains.vuejs.lang.html.lexer.VueTagEmbeddedContentProvider
import org.jetbrains.vuejs.lang.html.psi.VueRefAttribute

private class VueReferenceContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(STYLE_PATTERN, STYLE_REF_PROVIDER)

    registrar.registerReferenceProvider(createSrcAttrValuePattern(TEMPLATE_TAG_NAME), STATIC_FILE_REF_PROVIDER)
    registrar.registerReferenceProvider(
      XmlPatterns.xmlAttributeValue().withParent(VueRefAttribute::class.java),
      REF_ATTRIBUTE_REF_PROVIDER
    )
    registrar.registerReferenceProvider(PATH_VALUE_PATTERN, PATH_REFERENCE_PROVIDER)
  }
}

private val STYLE_PATTERN = createSrcAttrValuePattern(STYLE_TAG_NAME)

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

internal val STATIC_FILE_REF_PROVIDER = object : PsiReferenceProvider() {
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

private val REF_ATTRIBUTE_REF_PROVIDER = object : PsiReferenceProvider() {
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

private val PATH_VALUE_PATTERN = creatPathAttributeValuePattern()

private val PATH_REFERENCE_PROVIDER = createPathReferenceProvider()

private class VueRefReference(element: PsiElement, private val target: PsiElement)
  : PsiReferenceBase<PsiElement>(element, ElementManipulators.getValueTextRange(element), false) {

  override fun resolve(): PsiElement = target

}

private fun createSrcAttrValuePattern(tagName: String): XmlAttributeValuePattern =
  XmlPatterns.xmlAttributeValue(SRC_ATTRIBUTE_NAME).withAncestor(2, XmlPatterns.xmlTag().withLocalName(tagName))

private fun creatPathAttributeValuePattern(): XmlAttributeValuePattern = XmlPatterns.xmlAttributeValue("href", "to")
  .withSuperParent(2, XmlPatterns.xmlTag().and(FilterPattern(JSXReferenceContributor.createPathContainedTagFilter(false))))
