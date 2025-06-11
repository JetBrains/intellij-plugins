// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.i18n

import com.intellij.lang.Language
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.polySymbols.*
import com.intellij.polySymbols.context.PolyContext
import com.intellij.polySymbols.html.HTML_ELEMENTS
import com.intellij.polySymbols.query.PolySymbolsListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolsQueryConfigurator
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.html.HtmlTag
import com.intellij.psi.impl.source.xml.XmlTextImpl
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.Stack
import com.intellij.xml.util.HtmlUtil.LANG_ATTRIBUTE_NAME
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.web.VUE_TOP_LEVEL_ELEMENTS
import org.jetbrains.vuejs.web.VueFramework

class VueI18NQueryConfigurator : PolySymbolsQueryConfigurator {

  override fun getScope(
    project: Project,
    location: PsiElement?,
    context: PolyContext,
    allowResolve: Boolean,
  ): List<PolySymbolsScope> =
    if (context.framework == VueFramework.ID
        && location is HtmlTag
        && location.name == "i18n"
        && location.parentTag == null
        && location.containingFile?.virtualFile?.fileType == VueFileType) {
      listOf(I18nTagInjectionKind(location))
    }
    else emptyList()

  private class I18nTagInjectionKind(private val tag: HtmlTag) : PolySymbolsScope {
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

    override fun getSymbols(
      qualifiedKind: PolySymbolQualifiedKind,
      params: PolySymbolsListSymbolsQueryParams,
      scope: Stack<PolySymbolsScope>,
    ): List<PolySymbolsScope> {
      if (qualifiedKind == VUE_TOP_LEVEL_ELEMENTS) {
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

  private class I18nTagExtension(private val lang: String) : PolySymbol {

    override val name: String
      get() = "i18n"

    override val origin: PolySymbolOrigin =
      PolySymbolOrigin.create(VueFramework.ID, "vue-i18n")

    override val qualifiedKind: PolySymbolQualifiedKind
      get() = HTML_ELEMENTS

    override val extension: Boolean
      get() = true

    override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
      when (property) {
        PolySymbol.PROP_INJECT_LANGUAGE -> property.tryCast(lang)
        else -> null
      }

    override fun createPointer(): Pointer<out PolySymbol> =
      Pointer.hardPointer(this)
  }

}