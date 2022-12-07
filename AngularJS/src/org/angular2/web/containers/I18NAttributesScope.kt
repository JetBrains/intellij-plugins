// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.containers

import com.intellij.html.webSymbols.attributes.WebSymbolAttributeDescriptor
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.CompositeWebSymbol
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import com.intellij.webSymbols.utils.nameSegments
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.web.Angular2PsiSourcedSymbol
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_I18N_ATTRIBUTES
import org.jetbrains.annotations.NonNls

class I18NAttributesScope(private val tag: XmlTag) : WebSymbolsScope {

  override fun getSymbols(namespace: SymbolNamespace,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    if (namespace == WebSymbol.NAMESPACE_HTML && kind == KIND_NG_I18N_ATTRIBUTES) {
      tag.attributes
        .mapNotNull { attr ->
          val info = Angular2AttributeNameParser.parse(attr.name, tag)
          if (isI18nCandidate(info) && (name == null || name.equals(info.name, true)))
            Angular2I18nAttributeSymbol(attr)
          else null
        }
    }
    else emptyList()


  override fun createPointer(): Pointer<out WebSymbolsScope> {
    val tag = this.tag.createSmartPointer()
    return Pointer {
      tag.dereference()?.let { I18NAttributesScope(it) }
    }
  }

  override fun getModificationCount(): Long = tag.containingFile.modificationStamp

  override fun equals(other: Any?): Boolean =
    other is I18NAttributesScope
    && other.tag == tag

  override fun hashCode(): Int =
    tag.hashCode()

  companion object {

    @NonNls
    val I18N_ATTR = "i18n"

    @JvmStatic
    fun isI18nCandidate(info: Angular2AttributeNameParser.AttributeInfo): Boolean {
      return info.type == Angular2AttributeType.REGULAR && info.name != I18N_ATTR
    }
  }

  private class Angular2I18nAttributeSymbol(private val attribute: XmlAttribute) : Angular2PsiSourcedSymbol, CompositeWebSymbol {

    override val source: PsiElement
      get() = attribute

    override val name: String
      get() = attribute.name

    override val nameSegments: List<WebSymbolNameSegment> by lazy(LazyThreadSafetyMode.NONE) {
      (attribute.descriptor as? WebSymbolAttributeDescriptor)?.symbol?.nameSegments
      ?: listOf(WebSymbolNameSegment(this))
    }

    override val priority: WebSymbol.Priority
      get() = WebSymbol.Priority.NORMAL

    override val project: Project
      get() = attribute.project

    override val namespace: SymbolNamespace
      get() = WebSymbol.NAMESPACE_HTML

    override val kind: SymbolKind
      get() = KIND_NG_I18N_ATTRIBUTES

    override fun equals(other: Any?): Boolean =
      other === this
      || other is Angular2I18nAttributeSymbol
      && other.attribute == attribute

    override fun hashCode(): Int =
      attribute.hashCode()

    override fun createPointer(): Pointer<Angular2I18nAttributeSymbol> {
      val attributePtr = attribute.createSmartPointer()
      return Pointer {
        val attribute = attributePtr.dereference() ?: return@Pointer null
        Angular2I18nAttributeSymbol(attribute)
      }
    }

  }

}