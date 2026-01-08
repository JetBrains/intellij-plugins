// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSPsiBasedTypeOfType
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.js.symbols.JSPropertySymbol
import com.intellij.polySymbols.js.symbols.getJSPropertySymbols
import com.intellij.polySymbols.js.symbols.getMatchingJSPropertySymbols
import com.intellij.polySymbols.query.*
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PsiSourcedPolySymbolDelegate
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.model.VueLocallyDefinedRegularComponent
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueRegularComponent
import org.jetbrains.vuejs.web.VUE_COMPONENTS
import org.jetbrains.vuejs.web.VUE_COMPONENT_NAMESPACES

class VueComponentNamespaceSymbol(
  override val name: String,
  override val source: JSPsiNamedElementBase,
) : PsiSourcedPolySymbol, PolySymbolScope, VueSymbol {
  override fun createPointer(): Pointer<out VueComponentNamespaceSymbol> {
    val name = name
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      sourcePtr.dereference()?.let { VueComponentNamespaceSymbol(name, it) }
    }
  }

  override fun getModificationCount(): Long =
    PsiModificationTracker.getInstance(source.project).modificationCount

  override val type: JSType
    get() = JSPsiBasedTypeOfType(
      when (val source = source) {
        is ES6ImportedBinding -> source.multiResolve(false)
        is ES6ImportSpecifier -> source.multiResolve(false)
        else -> null
      }
        ?.asSequence()
        ?.firstNotNullOfOrNull { it.takeIf { it.isValidResult }?.element }
      ?: source, false)

  override val kind: PolySymbolKind
    get() = VUE_COMPONENT_NAMESPACES

  override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
    isNamespacedKind(kind)

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (isNamespacedKind(qualifiedName.kind) && qualifiedName.name.getOrNull(0)?.isUpperCase() != false)
      getMatchingJSPropertySymbols(qualifiedName.name, params.queryExecutor.namesProvider).adaptToNamespaceComponents(qualifiedName.kind)
    else
      emptyList()

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (isNamespacedKind(kind))
      getJSPropertySymbols().adaptToNamespaceComponents(kind)
    else
      emptyList()

  private fun List<JSPropertySymbol>.adaptToNamespaceComponents(kind: PolySymbolKind): List<PolySymbol> =
    mapNotNull { symbol ->
      val source = symbol.source as? JSPsiNamedElementBase ?: return@mapNotNull null
      val component = VueModelManager.getComponent(source) as? VueRegularComponent
      if (component != null && kind == VUE_COMPONENTS) {
        VueNamespacedComponent(
          VueComponentSymbol(symbol.name, VueLocallyDefinedRegularComponent(component, source), VueModelVisitor.Proximity.LOCAL))
      }
      else if (component == null && kind == VUE_COMPONENT_NAMESPACES) {
        VueComponentNamespaceSymbol(symbol.name, source)
      }
      else null
    }

  override fun equals(other: Any?): Boolean =
    other is VueComponentNamespaceSymbol
    && other.name == name
    && other.source == source

  override fun hashCode(): Int =
    31 * name.hashCode() + source.hashCode()

  private class VueNamespacedComponent(override val delegate: VueComponentSymbol) : PsiSourcedPolySymbolDelegate<VueComponentSymbol> {

    private val namespaceSymbol = VueComponentNamespaceSymbol(delegate.name, delegate.rawSource as JSPsiNamedElementBase)

    override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
      isNamespacedKind(kind) || super.isExclusiveFor(kind)

    override fun createPointer(): Pointer<out VueNamespacedComponent> {
      val delegatePtr = delegate.createPointer()
      return Pointer {
        delegatePtr.dereference()?.let { VueNamespacedComponent(it) }
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
      other is VueNamespacedComponent && other.delegate == delegate

    override fun hashCode(): Int =
      delegate.hashCode()

  }

  companion object {
    private fun isNamespacedKind(kind: PolySymbolKind) =
      kind == VUE_COMPONENT_NAMESPACES || kind == VUE_COMPONENTS
  }

}