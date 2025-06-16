// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.scopes

import com.intellij.html.polySymbols.attributes.HtmlAttributeSymbolDescriptor
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.polySymbols.*
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolNameMatchQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.utils.nameSegments
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.web.Angular2PsiSourcedSymbol
import org.angular2.web.NG_I18N_ATTRIBUTES
import org.jetbrains.annotations.NonNls

class I18NAttributesScope(private val tag: XmlTag) : PolySymbolScope {

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (qualifiedName.matches(NG_I18N_ATTRIBUTES)) {
      tag.attributes
        .mapNotNull { attr ->
          val info = Angular2AttributeNameParser.parse(attr.name, tag)
          if (isI18nCandidate(info) && qualifiedName.name.equals(info.name, true))
            Angular2I18nAttributeSymbol(attr)
          else null
        }
    }
    else emptyList()

  override fun getSymbols(
    qualifiedKind: PolySymbolQualifiedKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (qualifiedKind == NG_I18N_ATTRIBUTES) {
      tag.attributes
        .mapNotNull { attr ->
          val info = Angular2AttributeNameParser.parse(attr.name, tag)
          if (isI18nCandidate(info))
            Angular2I18nAttributeSymbol(attr)
          else null
        }
    }
    else emptyList()


  override fun createPointer(): Pointer<out PolySymbolScope> {
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
    val I18N_ATTR: String = "i18n"

    @JvmStatic
    fun isI18nCandidate(info: Angular2AttributeNameParser.AttributeInfo): Boolean {
      return info.type == Angular2AttributeType.REGULAR && info.name != I18N_ATTR
    }
  }

  private class Angular2I18nAttributeSymbol(private val attribute: XmlAttribute) : Angular2PsiSourcedSymbol, CompositePolySymbol {

    override val source: PsiElement
      get() = attribute

    override val name: String
      get() = attribute.name

    override val nameSegments: List<PolySymbolNameSegment> by lazy(LazyThreadSafetyMode.PUBLICATION) {
      (attribute.descriptor as? HtmlAttributeSymbolDescriptor)?.symbol?.nameSegments
      ?: listOf(PolySymbolNameSegment.create(this))
    }

    override val priority: PolySymbol.Priority
      get() = PolySymbol.Priority.NORMAL

    override val project: Project
      get() = attribute.project

    override val qualifiedKind: PolySymbolQualifiedKind
      get() = NG_I18N_ATTRIBUTES

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