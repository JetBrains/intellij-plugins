// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.containers

import com.intellij.javascript.web.symbols.SymbolKind
import com.intellij.javascript.web.symbols.WebSymbolsContainer
import com.intellij.javascript.web.symbols.WebSymbolsNameMatchQueryParams
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.Stack
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.web.Angular2PsiSourcedSymbol
import org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.Companion.KIND_NG_I18N_ATTRIBUTES
import org.jetbrains.annotations.NonNls

class I18nAttributesContainer(private val tag: XmlTag) : WebSymbolsContainer {

  override fun getSymbols(namespace: WebSymbolsContainer.Namespace?,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
    if (namespace == WebSymbolsContainer.Namespace.HTML && kind == KIND_NG_I18N_ATTRIBUTES) {
      tag.attributes
        .mapNotNull { attr ->
          val info = Angular2AttributeNameParser.parse(attr.name, tag)
          if (isI18nCandidate(info) && (name == null || name.equals(info.name, true))) {
            return@mapNotNull Angular2I18nAttributeSymbol(attr)
          }
          null
        }
    }
    else emptyList()


  override fun createPointer(): Pointer<out WebSymbolsContainer> {
    val tag = this.tag.createSmartPointer()
    return Pointer {
      tag.dereference()?.let { I18nAttributesContainer(it) }
    }
  }

  override fun getModificationCount(): Long = tag.containingFile.modificationStamp

  override fun equals(other: Any?): Boolean =
    other is I18nAttributesContainer
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

  private class Angular2I18nAttributeSymbol(private val attribute: XmlAttribute) : Angular2PsiSourcedSymbol {

    override val source: PsiElement
      get() = attribute

    override val name: String
      get() = attribute.name

    override val namespace: WebSymbolsContainer.Namespace
      get() = WebSymbolsContainer.Namespace.HTML

    override val kind: SymbolKind
      get() = KIND_NG_I18N_ATTRIBUTES

    override fun equals(other: Any?): Boolean =
      other === this
      || other is Angular2I18nAttributeSymbol
      && other.attribute == attribute

    override fun hashCode(): Int =
      attribute.hashCode()

    override fun createPointer(): Pointer<Angular2I18nAttributeSymbol> {
      val attribute = this.attribute.createSmartPointer()
      return Pointer {
        attribute.dereference()?.let { Angular2I18nAttributeSymbol(it) }
      }
    }

    override val project: Project
      get() = attribute.project

  }

}