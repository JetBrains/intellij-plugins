// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
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
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENTS
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENT_NAMESPACES

class AstroNamespacedComponent(
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
      when (val source = source) {
        is ES6ImportedBinding -> source.multiResolve(false)
        is ES6ImportSpecifier -> source.multiResolve(false)
        else -> null
      }
        ?.asSequence()?.firstNotNullOfOrNull { it.takeIf { it.isValidResult }?.element }
      ?: source, false)

  override val kind: PolySymbolKind
    get() = UI_FRAMEWORK_COMPONENT_NAMESPACES

  override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
    isNamespacedKind(kind)

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (isNamespacedKind(qualifiedName.kind) && qualifiedName.name.getOrNull(0)?.isUpperCase() != false) {
      getMatchingJSPropertySymbols(qualifiedName.name, params.queryExecutor.namesProvider)
        .adaptToNamespaceComponents(qualifiedName.kind)
        .ifEmpty { listOf(UiFrameworkNamespacedComponent(UnknownComponent(params.queryExecutor.location ?: source, qualifiedName.name))) }
    }
    else
      emptyList()

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (isNamespacedKind(kind)) {
      getJSPropertySymbols().adaptToNamespaceComponents(kind)
    }
    else
      emptyList()

  private fun List<JSPropertySymbol>.adaptToNamespaceComponents(kind: PolySymbolKind): List<PolySymbol> =
    mapNotNull { symbol ->
      val source = symbol.source as? PsiElement ?: return@mapNotNull null
      when {
        kind == UI_FRAMEWORK_COMPONENTS -> {
          UiFrameworkNamespacedComponent(UiFrameworkComponent(symbol.name, source))
        }
        else -> null
      }
    }

  override fun equals(other: Any?): Boolean =
    other is AstroNamespacedComponent
    && other.name == name
    && other.source == source

  override fun hashCode(): Int =
    31 * name.hashCode() + source.hashCode()

  private class UiFrameworkNamespacedComponent(override val delegate: PsiSourcedPolySymbol) :
    PsiSourcedPolySymbolDelegate<PsiSourcedPolySymbol> {

    private val namespaceSymbol = AstroNamespacedComponent(delegate.name, delegate.source as PsiElement)

    override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
      isNamespacedKind(kind) || kind == UI_FRAMEWORK_COMPONENTS || super.isExclusiveFor(kind)

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
      kind: PolySymbolKind,
      params: PolySymbolListSymbolsQueryParams,
      stack: PolySymbolQueryStack,
    ): List<PolySymbol> =
      namespaceSymbol.getSymbols(kind, params, stack) +
      super.getSymbols(kind, params, stack)

    override fun getCodeCompletions(
      qualifiedName: PolySymbolQualifiedName,
      params: PolySymbolCodeCompletionQueryParams,
      stack: PolySymbolQueryStack,
    ): List<PolySymbolCodeCompletionItem> =
      namespaceSymbol.getCodeCompletions(qualifiedName, params, stack) +
      super.getCodeCompletions(qualifiedName, params, stack)

    override fun equals(other: Any?): Boolean =
      other is UiFrameworkNamespacedComponent && other.delegate == delegate

    override fun hashCode(): Int =
      delegate.hashCode()
  }

  companion object {
    private fun isNamespacedKind(qualifiedKind: PolySymbolKind) =
      qualifiedKind == UI_FRAMEWORK_COMPONENT_NAMESPACES || qualifiedKind == UI_FRAMEWORK_COMPONENTS
  }
}
