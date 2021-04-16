// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.documentation

import com.intellij.lang.documentation.DocumentationMarkup.*
import com.intellij.lang.documentation.DocumentationProvider
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
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.ATTR_DIRECTIVE_PREFIX
import org.jetbrains.vuejs.codeInsight.ATTR_MODIFIER_PREFIX
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeDescriptor
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.codeInsight.refs.VueJSReferenceExpressionResolver
import org.jetbrains.vuejs.codeInsight.tags.VueElementDescriptor
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterReferenceExpression
import org.jetbrains.vuejs.model.VueDirective

class VueDocumentationProvider : DocumentationProvider {

  override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
    return null
  }

  override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): MutableList<String>? {
    return (element as? PsiWrappedVueDocumentedItem)?.let {
      it.item.docUrl?.let { url -> mutableListOf(url) } ?: mutableListOf()
    }
  }

  override fun getCustomDocumentationElement(editor: Editor, file: PsiFile, contextElement: PsiElement?, targetOffset: Int): PsiElement? {
    return getVueDocumentedItem(contextElement, targetOffset)
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

  private fun getVueDocumentedItem(originalElement: PsiElement?, offset: Int): Pair<VueItemDocumentation, PsiElement>? {
    val toCheck = when (originalElement?.node?.elementType) {
      XmlTokenType.XML_TAG_END,
      XmlTokenType.TAG_WHITE_SPACE,
      XmlTokenType.XML_EQ,
      XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER,
      XmlTokenType.XML_WHITE_SPACE -> originalElement?.containingFile
        ?.findElementAt(originalElement.textOffset - 1)
      else -> originalElement
    }
    val docSource = when (toCheck?.node?.elementType) {
      XmlTokenType.XML_NAME,
      JSTokenTypes.IDENTIFIER -> toCheck?.parent
      else -> toCheck
    }
    val relativeOffset = offset - (docSource ?: return null).textOffset
    return when (docSource) {
      is XmlTag -> docSource.descriptor
        ?.castSafelyTo<VueElementDescriptor>()
        ?.getSources()
        ?.getOrNull(0)
        ?.let { Pair(it.documentation, docSource) }
      is XmlAttribute -> {
        docSource.descriptor
          ?.castSafelyTo<VueAttributeDescriptor>()
          ?.let { descriptor ->
            descriptor.getSources()
              .getOrNull(0)
              ?.let { getAttributeDetailedSource(relativeOffset, docSource.name, descriptor.getInfo(), it) }
          }
          ?.let { Pair(it.documentation, docSource) }
      }
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
      result.append(VueBundle.message("vue.documentation.vue") + " ").append(item.type)
    }
    else {
      result.append(name)
    }
    result.append(DEFINITION_END)
    item.description?.let { result.append(CONTENT_START).append(it).append(CONTENT_END) }

    val details = LinkedHashMap(item.customSections)
    item.library?.let { details[VueBundle.message("vue.documentation.section.library")] = "<p>${if (it == "vue") "Vue" else it}" }

    if (details.isNotEmpty()) {
      result.append(SECTIONS_START)
      details.entries.forEach { (name, value) ->
        result.append(SECTION_HEADER_START).append(name)
          .append(SECTION_SEPARATOR).append(value).append(SECTION_END)
      }
      result.append(SECTIONS_END)
    }
    return result.toString()
  }

  private fun getAttributeDetailedSource(offset: Int,
                                         attrName: String,
                                         info: VueAttributeNameParser.VueAttributeInfo,
                                         source: VueDocumentedItem): VueDocumentedItem {
    if (offset >= 0
        && offset <= attrName.length
        && info is VueAttributeNameParser.VueDirectiveInfo
        && source is VueDirective) {
      val argumentOffset = offset - if (info.isShorthand) 1 else (ATTR_DIRECTIVE_PREFIX.length + info.name.length + 1)
      if (argumentOffset >= 0) {
        if (info.arguments != null && argumentOffset <= info.arguments.length) {
          return source.argument ?: source
        }
        else {
          val start = attrName.lastIndexOf(ATTR_MODIFIER_PREFIX, offset - 1)
          val end = attrName.indexOf(ATTR_MODIFIER_PREFIX, offset).takeIf { it >= 0 }
                    ?: attrName.length
          if (start in 1 until end) {
            val modifierName = attrName.substring(start + 1, end)
            if (modifierName.isNotBlank()) {
              source.modifiers.find {
                if (it.pattern != null)
                  it.pattern!!.matches(modifierName)
                else
                  it.name == modifierName
              }?.let {
                return it
              }
            }
          }
        }
      }
    }
    return source
  }

  private class PsiWrappedVueDocumentedItem(val item: VueItemDocumentation,
                                            private val source: PsiElement) : FakePsiElement(), PsiNamedElement {
    override fun getParent(): PsiElement? = source
    override fun getName(): String? = item.defaultName ?: (VueBundle.message("vue.documentation.vue") + " " + item.type)
  }

}
