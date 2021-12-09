// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.i18n

import com.intellij.javascript.web.symbols.*
import com.intellij.lang.Language
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.xml.XmlTextImpl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.codeInsight.LANG_ATTRIBUTE_NAME
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.web.VueFramework
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider

class VueI18nAdditionalContextProvider : WebSymbolsAdditionalContextProvider {

  override fun getAdditionalContext(element: PsiElement?, framework: String?): List<WebSymbolsContainer> =
    if (framework == VueFramework.ID
        && element is HtmlTag
        && element.name == "i18n"
        && element.parentTag == null
        && element.containingFile?.virtualFile?.fileType == VueFileType.INSTANCE) {
      listOf(I18nTagInjectionKind(element))
    }
    else emptyList()

  private class I18nTagInjectionKind(private val tag: HtmlTag) : WebSymbolsContainer {
    override fun equals(other: Any?): Boolean =
      other is I18nTagInjectionKind
      && other.tag == tag

    override fun hashCode(): Int =
      tag.hashCode()

    override fun createPointer(): Pointer<I18nTagInjectionKind> {
      val tag = this.tag.createSmartPointer()
      return Pointer {
        tag.dereference()?.let { I18nTagInjectionKind(it) }
      }
    }

    override fun getModificationCount(): Long = tag.containingFile.modificationStamp

    override fun getSymbols(namespace: WebSymbolsContainer.Namespace?,
                            kind: SymbolKind,
                            name: String?,
                            params: WebSymbolsNameMatchQueryParams,
                            context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> {
      if (kind == VueWebSymbolsAdditionalContextProvider.KIND_VUE_TOP_LEVEL_ELEMENTS
          && namespace == WebSymbolsContainer.Namespace.HTML) {
        val language = tag.getAttributeValue(LANG_ATTRIBUTE_NAME)
                         ?.let { lang -> Language.getRegisteredLanguages().find { it.id.equals(lang, true) } }
                       ?: if (PsiTreeUtil.getChildOfType(tag, XmlTextImpl::class.java)
                           ?.chars?.find { !it.isWhitespace() }?.let { it == '{' || it == '[' } != false)
                         Language.findLanguageByID("JSON")
                       else
                         Language.findLanguageByID("yaml")
        return language?.id?.let { listOf(I18nTagExtension(it)) } ?: emptyList()
      }
      else return emptyList()
    }
  }

  private class I18nTagExtension(private val lang: String) : WebSymbol {

    override val matchedName: String
      get() = "i18n"

    override val origin: WebSymbolsContainer.Origin =
      WebSymbolsContainer.OriginData(VueFramework.ID, "vue-i18n")

    override val namespace: WebSymbolsContainer.Namespace
      get() = WebSymbolsContainer.Namespace.HTML

    override val kind: SymbolKind
      get() = WebSymbol.KIND_HTML_ELEMENTS

    override val extension: Boolean
      get() = true

    override val properties: Map<String, Any>
      get() = mapOf(Pair(WebSymbol.PROP_INJECT_LANGUAGE, lang))

    override fun createPointer(): Pointer<out WebSymbol> =
      Pointer.hardPointer(this)
  }

}