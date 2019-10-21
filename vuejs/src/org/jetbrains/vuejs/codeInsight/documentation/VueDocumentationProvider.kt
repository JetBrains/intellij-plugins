// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.documentation

import com.intellij.lang.documentation.DocumentationMarkup.*
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.lang.documentation.DocumentationProviderEx
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeDescriptor
import org.jetbrains.vuejs.codeInsight.refs.VueJSReferenceExpressionResolver
import org.jetbrains.vuejs.codeInsight.tags.VueElementDescriptor
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterReferenceExpression

class VueDocumentationProvider : DocumentationProviderEx(), DocumentationProvider {

  override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
    return null
  }

  override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): MutableList<String>? {
    return (element as? PsiWrappedVueDocumentedItem)?.let {
        it.item.docUrl?.let { url -> mutableListOf(url) } ?: mutableListOf()
      }
  }

  override fun getCustomDocumentationElement(editor: Editor, file: PsiFile, contextElement: PsiElement?): PsiElement? {
    return getVueDocumentedItem(contextElement)
      ?.let { PsiWrappedVueDocumentedItem(it.first, it.second) }
  }

  override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
    return (element as? PsiWrappedVueDocumentedItem)
      ?.let { generateDoc(it.item) }
  }

  override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, `object`: Any?, element: PsiElement?): PsiElement? {
    val documentationItem = `object`?.castSafelyTo<Pair<*, *>>()
      ?.first
      ?.castSafelyTo<VueItemDocumentation>()
    if (documentationItem != null && element != null) {
      return PsiWrappedVueDocumentedItem(documentationItem, element)
    }
    return null
  }

  private fun getVueDocumentedItem(originalElement: PsiElement?): Pair<VueItemDocumentation, PsiElement>? {
    val docSource = when (originalElement?.node?.elementType) {
      XmlTokenType.XML_NAME, JSTokenTypes.IDENTIFIER -> originalElement?.parent
      else -> originalElement
    }
    return when (docSource) {
      is XmlTag -> docSource.descriptor
        ?.castSafelyTo<VueElementDescriptor>()
        ?.getSources()
        ?.getOrNull(0)
        ?.let { Pair(it.documentation, docSource) }
      is XmlAttribute -> docSource.descriptor
        ?.castSafelyTo<VueAttributeDescriptor>()
        ?.getSources()
        ?.getOrNull(0)
        ?.let { Pair(it.documentation, docSource) }
      is VueJSFilterReferenceExpression -> VueJSReferenceExpressionResolver
        .resolveFiltersFromReferenceExpression(docSource)
        .getOrNull(0)
        ?.let { Pair(it.documentation, docSource) }
      else -> null
    }
  }

  private fun generateDoc(item: VueItemDocumentation): String? {
    val result = StringBuilder().append(DEFINITION_START)
    val name = item.defaultName ?: ""
    if (name.isBlank()) {
      result.append("Vue ").append(item.type)
    }
    else {
      result.append(name)
    }
    result.append(DEFINITION_END)
    item.description?.let { result.append(CONTENT_START).append(it).append(CONTENT_END) }

    val details = LinkedHashMap(item.customSections)
    item.library?.let { details["Library"] = "<p>$it" }

    if (details.isNotEmpty()) {
      result.append(SECTIONS_START)
      details.entries.forEach { (name, value) ->
        result.append(SECTION_HEADER_START).append(if (name.endsWith(':')) name.capitalize() else "${name.capitalize()}:")
          .append(SECTION_SEPARATOR).append(value).append(SECTION_END)
      }
      result.append(SECTIONS_END)
    }
    return result.toString()
  }

  private class PsiWrappedVueDocumentedItem(val item: VueItemDocumentation,
                                            private val source: PsiElement) : FakePsiElement(), PsiNamedElement {
    override fun getParent(): PsiElement? = source
    override fun getName(): String? = item.defaultName ?: "Vue " + item.type
  }

}
