// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.polySymbols.symbols.JSPropertySymbol
import com.intellij.javascript.polySymbols.symbols.getJSPropertySymbols
import com.intellij.javascript.polySymbols.symbols.getMatchingJSPropertySymbols
import com.intellij.javascript.polySymbols.types.PROP_JS_TYPE
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.types.JSPsiBasedTypeOfType
import com.intellij.model.Pointer
import com.intellij.polySymbols.*
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.query.PolySymbolsCodeCompletionQueryParams
import com.intellij.polySymbols.query.PolySymbolsListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolsNameMatchQueryParams
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PsiSourcedPolySymbolDelegate
import com.intellij.psi.createSmartPointer
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.model.VueLocallyDefinedRegularComponent
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueRegularComponent
import org.jetbrains.vuejs.web.VUE_COMPONENTS
import org.jetbrains.vuejs.web.VUE_COMPONENT_NAMESPACES
import org.jetbrains.vuejs.web.VueFramework

class VueComponentNamespaceSymbol(
  override val name: String,
  override val source: JSPsiNamedElementBase,
) : PsiSourcedPolySymbol {
  override fun createPointer(): Pointer<out PsiSourcedPolySymbol> {
    val name = name
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      sourcePtr.dereference()?.let { VueComponentNamespaceSymbol(name, it) }
    }
  }

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
        ?.asSequence()
        ?.mapNotNull { it.takeIf { it.isValidResult }?.element }
        ?.firstOrNull()
      ?: source, false)

  override val origin: PolySymbolOrigin
    get() = object : PolySymbolOrigin {
      override val framework: FrameworkId
        get() = VueFramework.ID
    }

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = VUE_COMPONENT_NAMESPACES

  override fun isExclusiveFor(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    isNamespacedKind(qualifiedKind)

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolsNameMatchQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    if (isNamespacedKind(qualifiedName.qualifiedKind) && qualifiedName.name.getOrNull(0)?.isUpperCase() != false)
      getMatchingJSPropertySymbols(qualifiedName.name, params.queryExecutor.namesProvider).adaptToNamespaceComponents(qualifiedName.kind)
    else
      emptyList()

  override fun getSymbols(
    qualifiedKind: PolySymbolQualifiedKind,
    params: PolySymbolsListSymbolsQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbolsScope> =
    if (isNamespacedKind(qualifiedKind))
      getJSPropertySymbols().adaptToNamespaceComponents(qualifiedKind.kind)
    else
      emptyList()

  private fun List<JSPropertySymbol>.adaptToNamespaceComponents(kind: PolySymbolKind) =
    mapNotNull { symbol ->
      val source = symbol.source as? JSPsiNamedElementBase ?: return@mapNotNull null
      val component = VueModelManager.getComponent(source) as? VueRegularComponent
      if (component != null && kind == VUE_COMPONENTS.kind) {
        VueNamespacedComponent(
          VueComponentSymbol(symbol.name, VueLocallyDefinedRegularComponent(component, source), VueModelVisitor.Proximity.LOCAL))
      }
      else if (component == null && kind == VUE_COMPONENT_NAMESPACES.kind) {
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

    override fun isExclusiveFor(qualifiedKind: PolySymbolQualifiedKind): Boolean =
      isNamespacedKind(qualifiedKind) || super.isExclusiveFor(qualifiedKind)

    override fun createPointer(): Pointer<out PsiSourcedPolySymbol> {
      val delegatePtr = delegate.createPointer()
      return Pointer {
        delegatePtr.dereference()?.let { VueNamespacedComponent(it) }
      }
    }

    override val queryScope: List<PolySymbolsScope>
      get() = listOf(this)

    override fun getMatchingSymbols(
      qualifiedName: PolySymbolQualifiedName,
      params: PolySymbolsNameMatchQueryParams,
      scope: Stack<PolySymbolsScope>,
    ): List<PolySymbol> =
      namespaceSymbol.getMatchingSymbols(qualifiedName, params, scope) +
      super.getMatchingSymbols(qualifiedName, params, scope)

    override fun getSymbols(
      qualifiedKind: PolySymbolQualifiedKind,
      params: PolySymbolsListSymbolsQueryParams,
      scope: Stack<PolySymbolsScope>,
    ): List<PolySymbolsScope> =
      namespaceSymbol.getSymbols(qualifiedKind, params, scope) +
      super.getSymbols(qualifiedKind, params, scope)

    override fun getCodeCompletions(
      qualifiedName: PolySymbolQualifiedName,
      params: PolySymbolsCodeCompletionQueryParams,
      scope: Stack<PolySymbolsScope>,
    ): List<PolySymbolCodeCompletionItem> =
      namespaceSymbol.getCodeCompletions(qualifiedName, params, scope) +
      super.getCodeCompletions(qualifiedName, params, scope)

    override fun equals(other: Any?): Boolean =
      other is VueNamespacedComponent && other.delegate == delegate

    override fun hashCode(): Int =
      delegate.hashCode()

  }

  companion object {
    private fun isNamespacedKind(qualifiedKind: PolySymbolQualifiedKind) =
      qualifiedKind == VUE_COMPONENT_NAMESPACES || qualifiedKind == VUE_COMPONENTS
  }

}