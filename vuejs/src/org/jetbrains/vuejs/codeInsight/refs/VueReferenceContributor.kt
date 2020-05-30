// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.refs

import com.intellij.lang.Language
import com.intellij.lang.javascript.modules.NodeModuleUtil.NODE_MODULES
import com.intellij.lang.javascript.psi.util.JSProjectUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.paths.PathReferenceManager
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.XmlAttributeValuePattern
import com.intellij.patterns.XmlPatterns
import com.intellij.psi.*
import com.intellij.psi.css.resolve.CssReferenceProviderUtil.getFileReferenceData
import com.intellij.psi.css.resolve.StylesheetFileReferenceSet
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import com.intellij.xml.util.HtmlUtil.*
import org.jetbrains.vuejs.codeInsight.LANG_ATTRIBUTE_NAME
import org.jetbrains.vuejs.lang.html.lexer.VueLexerHelper
import org.jetbrains.vuejs.model.DEPRECATED_SLOT_ATTRIBUTE
import org.jetbrains.vuejs.model.getAvailableSlots

class VueReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(DEPRECATED_SLOT_NAME_ATTR_PATTERN, DEPRECATED_SLOT_REF_PROVIDER)
    registrar.registerReferenceProvider(createSrcAttrValuePattern(STYLE_TAG_NAME), STYLE_REF_PROVIDER)
    registrar.registerReferenceProvider(createSrcAttrValuePattern(TEMPLATE_TAG_NAME), BASIC_REF_PROVIDER)
  }

  companion object {

    private val DEPRECATED_SLOT_NAME_ATTR_PATTERN = XmlPatterns.xmlAttributeValue(DEPRECATED_SLOT_ATTRIBUTE)

    private val DEPRECATED_SLOT_REF_PROVIDER = object : PsiReferenceProvider() {
      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if (element is XmlAttributeValue && element.textLength > 2) {
          return arrayOf(object : PsiReferenceBase<XmlAttributeValue>(element, TextRange(1, element.textLength - 1), true) {
            override fun resolve(): PsiElement? {
              return (element.parent as? XmlAttribute)
                ?.let { getAvailableSlots(it, false) }
                ?.asSequence()
                ?.find { it.name == this.value }
                ?.source
            }
          })
        }
        return PsiReference.EMPTY_ARRAY
      }
    }

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
              ?.let { VueLexerHelper.styleViaLang(Language.findLanguageByID("CSS"), it) }
              ?.associatedFileType
              ?.let { arrayOf(it) }
            ?: emptyArray()

          val referenceSet = VueStylesheetFileReferenceSet(element, referenceData.first,
                                                           referenceData.second, *suitableFileTypes)
          @Suppress("UNCHECKED_CAST")
          return referenceSet.allReferences as Array<PsiReference>
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

    private class VueStylesheetFileReferenceSet(element: PsiElement, referenceText: String,
                                                textRange: TextRange, vararg suitableFileTypes: FileType)
      : StylesheetFileReferenceSet(element, referenceText, textRange, false, *suitableFileTypes) {

      override fun computeDefaultContexts(): MutableCollection<PsiFileSystemItem> {
        val result = super.computeDefaultContexts()
        if (pathString[0] !in listOf('.', '/', '\\')) {
          val psiManager = PsiManager.getInstance(this.element.project)
          PsiUtilCore.getVirtualFile(this.element)?.let { root ->
            JSProjectUtil.processDirectoriesUpToContentRoot(this.element.project, root) { dir ->
              dir.findChild(NODE_MODULES)
                ?.takeIf { it.isDirectory }
                ?.let { psiManager.findDirectory(it) }
                ?.let { result.add(it) }
              true
            }
          }
        }
        return result
      }
    }
  }
}
