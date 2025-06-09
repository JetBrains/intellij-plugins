// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.javascript.polySymbols.symbols.asPolySymbol
import com.intellij.javascript.polySymbols.symbols.getJSPropertySymbols
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.model.Pointer
import com.intellij.polySymbols.*
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.query.PolySymbolsNameMatchQueryParams
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PolySymbolsScopeWithCache
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.codeInsight.astroContentRoot
import org.jetbrains.astro.codeInsight.frontmatterScript
import org.jetbrains.astro.codeInsight.propsInterface
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.polySymbols.ASTRO_COMPONENTS
import org.jetbrains.astro.polySymbols.ASTRO_COMPONENT_PROPS
import org.jetbrains.astro.polySymbols.AstroProximity
import org.jetbrains.astro.polySymbols.PROP_ASTRO_PROXIMITY

class AstroLocalComponent(
  override val name: String,
  override val source: PsiElement,
  override val priority: PolySymbol.Priority = PolySymbol.Priority.HIGH,
) : PsiSourcedPolySymbol, PolySymbolsScopeWithCache<PsiElement, Unit>(AstroFramework.ID, source.project, source, Unit) {

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolsNameMatchQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    if (qualifiedName.matches(HTML_ATTRIBUTES) && name.contains(":"))
      emptyList()
    else
      super<PolySymbolsScopeWithCache>.getMatchingSymbols(qualifiedName, params, scope)

  override fun provides(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    qualifiedKind == ASTRO_COMPONENT_PROPS

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
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
      ?.asPolySymbol()
      ?.getJSPropertySymbols()
      ?.map(::AstroComponentPropSymbol)
      ?.forEach(consumer)
  }

  override val origin: PolySymbolOrigin
    get() = AstroProjectSymbolOrigin

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = ASTRO_COMPONENTS

  override val properties: Map<String, Any>
    get() = mapOf(PROP_ASTRO_PROXIMITY to AstroProximity.LOCAL)

  override fun createPointer(): Pointer<out AstroLocalComponent> {
    val name = name
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      sourcePtr.dereference()?.let { AstroLocalComponent(name, it) }
    }
  }

  override fun getModificationCount() = super<PolySymbolsScopeWithCache>.getModificationCount()
}