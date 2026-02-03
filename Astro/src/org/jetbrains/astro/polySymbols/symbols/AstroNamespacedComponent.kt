// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.types.JSPsiBasedTypeOfType
import com.intellij.model.Pointer
import com.intellij.polySymbols.*
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.js.symbols.JSPropertySymbol
import com.intellij.polySymbols.js.symbols.getJSPropertySymbols
import com.intellij.polySymbols.js.symbols.getMatchingJSPropertySymbols
import com.intellij.polySymbols.js.types.PROP_JS_TYPE
import com.intellij.polySymbols.query.*
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PsiSourcedPolySymbolDelegate
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.astro.codeInsight.resolveIfImportSpecifier
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENTS
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENT_NAMESPACES

data class AstroNamespacedComponent(
  override val name: String,
  override val source: PsiElement,
) : ComponentPolySymbol, PolySymbolScope {

  override fun createPointer(): Pointer<out AstroNamespacedComponent> {
    val name = name
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      sourcePtr.dereference()?.let { AstroNamespacedComponent(name, it) }
    }
  }

  override fun getModificationCount(): Long =
    PsiModificationTracker.getInstance(source.project).modificationCount

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_JS_TYPE -> property.tryCast(type)
      else -> super.get(property)
    }

  private val type: Any
    get() = JSPsiBasedTypeOfType(
      (source as? JSPsiNamedElementBase)?.resolveIfImportSpecifier() ?: source, false)

  override val origin: PolySymbolOrigin
    get() = AstroProjectSymbolOrigin

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = UI_FRAMEWORK_COMPONENT_NAMESPACES

  override fun isExclusiveFor(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    isNamespacedKind(qualifiedKind)

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (isNamespacedKind(qualifiedName.qualifiedKind) && qualifiedName.name.getOrNull(0)?.isUpperCase() != false) {
      getMatchingJSPropertySymbols(qualifiedName.name, params.queryExecutor.namesProvider)
        .adaptToNamespaceComponents(qualifiedName.qualifiedKind)
        .ifEmpty { listOf(UiFrameworkNamespacedComponent(UnknownComponent(params.queryExecutor.location ?: source, qualifiedName.name))) }
    }
    else
      emptyList()

  override fun getSymbols(
    qualifiedKind: PolySymbolQualifiedKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (isNamespacedKind(qualifiedKind)) {
      getJSPropertySymbols().adaptToNamespaceComponents(qualifiedKind)
    }
    else
      emptyList()

  private fun List<JSPropertySymbol>.adaptToNamespaceComponents(qualifiedKind: PolySymbolQualifiedKind): List<PolySymbol> =
    mapNotNull { symbol ->
      val source = symbol.source as? PsiElement ?: return@mapNotNull null
      when {
        qualifiedKind == UI_FRAMEWORK_COMPONENTS -> {
          UiFrameworkNamespacedComponent(UiFrameworkComponent(symbol.name, source))
        }
        else -> null
      }
    }

  private data class UiFrameworkNamespacedComponent(override val delegate: PsiSourcedPolySymbol) :
    PsiSourcedPolySymbolDelegate<PsiSourcedPolySymbol> {

    private val namespaceSymbol = AstroNamespacedComponent(delegate.name, delegate.source as PsiElement)

    override fun isExclusiveFor(qualifiedKind: PolySymbolQualifiedKind): Boolean =
      isNamespacedKind(qualifiedKind) || qualifiedKind == UI_FRAMEWORK_COMPONENTS || super.isExclusiveFor(qualifiedKind)

    override fun createPointer(): Pointer<out UiFrameworkNamespacedComponent> {
      val delegatePtr = delegate.createPointer()
      return Pointer {
        delegatePtr.dereference()?.let { UiFrameworkNamespacedComponent(it) }
      }
    }

    override val queryScope: List<PolySymbolScope>
      get() = listOf(this)

    override fun getMatchingSymbols(
      qualifiedName: PolySymbolQualifiedName,
      params: PolySymbolNameMatchQueryParams,
      stack: PolySymbolQueryStack,
    ): List<PolySymbol> =
      namespaceSymbol.getMatchingSymbols(qualifiedName, params, stack) +
      super.getMatchingSymbols(qualifiedName, params, stack)

    override fun getSymbols(
      qualifiedKind: PolySymbolQualifiedKind,
      params: PolySymbolListSymbolsQueryParams,
      stack: PolySymbolQueryStack,
    ): List<PolySymbol> =
      namespaceSymbol.getSymbols(qualifiedKind, params, stack) +
      super.getSymbols(qualifiedKind, params, stack)

    override fun getCodeCompletions(
      qualifiedName: PolySymbolQualifiedName,
      params: PolySymbolCodeCompletionQueryParams,
      stack: PolySymbolQueryStack,
    ): List<PolySymbolCodeCompletionItem> =
      namespaceSymbol.getCodeCompletions(qualifiedName, params, stack) +
      super.getCodeCompletions(qualifiedName, params, stack)
  }

  companion object {
    private fun isNamespacedKind(qualifiedKind: PolySymbolQualifiedKind) =
      qualifiedKind == UI_FRAMEWORK_COMPONENT_NAMESPACES || qualifiedKind == UI_FRAMEWORK_COMPONENTS
  }
}
