// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols.symbols

import com.intellij.javascript.webSymbols.symbols.asWebSymbol
import com.intellij.javascript.webSymbols.symbols.getJSPropertySymbols
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.HTML_ATTRIBUTES
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.codeInsight.astroContentRoot
import org.jetbrains.astro.codeInsight.frontmatterScript
import org.jetbrains.astro.codeInsight.propsInterface
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.webSymbols.ASTRO_COMPONENTS
import org.jetbrains.astro.webSymbols.ASTRO_COMPONENT_PROPS
import org.jetbrains.astro.webSymbols.AstroProximity
import org.jetbrains.astro.webSymbols.PROP_ASTRO_PROXIMITY

class AstroLocalComponent(override val name: String,
                          override val source: PsiElement,
                          override val priority: WebSymbol.Priority = WebSymbol.Priority.HIGH)
  : PsiSourcedWebSymbol, WebSymbolsScopeWithCache<PsiElement, Unit>(AstroFramework.ID, source.project, source, Unit) {

  override fun getMatchingSymbols(qualifiedName: WebSymbolQualifiedName,
                                  params: WebSymbolsNameMatchQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbol> =
    if (qualifiedName.matches(HTML_ATTRIBUTES) && name.contains(":"))
      emptyList()
    else
      super<WebSymbolsScopeWithCache>.getMatchingSymbols(qualifiedName, params, scope)

  override fun provides(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == ASTRO_COMPONENT_PROPS

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)

    dataHolder
      .asSafely<ES6ImportedBinding>()
      ?.findReferencedElements()
      ?.firstOrNull()
      ?.containingFile
      ?.takeIf { it is AstroFileImpl }
      ?.astroContentRoot()
      ?.frontmatterScript()
      ?.propsInterface()
      ?.asWebSymbol()
      ?.getJSPropertySymbols()
      ?.map(::AstroComponentPropSymbol)
      ?.forEach(consumer)
  }

  override val origin: WebSymbolOrigin
    get() = AstroProjectSymbolOrigin

  override val namespace: SymbolNamespace
    get() = NAMESPACE_HTML

  override val kind: SymbolKind
    get() = ASTRO_COMPONENTS.kind

  override val properties: Map<String, Any>
    get() = mapOf(PROP_ASTRO_PROXIMITY to AstroProximity.LOCAL)

  override fun createPointer(): Pointer<out AstroLocalComponent> {
    val name = name
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      sourcePtr.dereference()?.let { AstroLocalComponent(name, it) }
    }
  }

  override fun getModificationCount() = super<WebSymbolsScopeWithCache>.getModificationCount()
}