// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.webSymbols.symbols.JSPropertySymbol
import com.intellij.javascript.webSymbols.symbols.getJSPropertySymbols
import com.intellij.javascript.webSymbols.symbols.getMatchingJSPropertySymbols
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.types.JSPsiBasedTypeOfType
import com.intellij.model.Pointer
import com.intellij.psi.createSmartPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import org.jetbrains.vuejs.model.VueLocallyDefinedRegularComponent
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueRegularComponent
import org.jetbrains.vuejs.web.VUE_COMPONENTS
import org.jetbrains.vuejs.web.VUE_COMPONENT_NAMESPACES
import org.jetbrains.vuejs.web.VueFramework
import java.util.*

class VueComponentNamespaceSymbol(
  override val name: String,
  override val source: JSPsiNamedElementBase
) : PsiSourcedWebSymbol {
  override fun createPointer(): Pointer<out PsiSourcedWebSymbol> {
    val name = name
    val sourcePtr = source.createSmartPointer()
    return Pointer {
      sourcePtr.dereference()?.let { VueComponentNamespaceSymbol(name, it) }
    }
  }

  override val type: Any
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

  override val origin: WebSymbolOrigin
    get() = object : WebSymbolOrigin {
      override val framework: FrameworkId
        get() = VueFramework.ID
    }
  override val namespace: SymbolNamespace
    get() = VUE_COMPONENT_NAMESPACES.namespace

  override val kind: SymbolKind
    get() = VUE_COMPONENT_NAMESPACES.kind

  override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    isNamespacedKind(qualifiedKind)

  override fun getMatchingSymbols(qualifiedName: WebSymbolQualifiedName,
                                  params: WebSymbolsNameMatchQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbol> =
    if (isNamespacedKind(qualifiedName.qualifiedKind) && qualifiedName.name.getOrNull(0)?.isUpperCase() != false)
      getMatchingJSPropertySymbols(qualifiedName.name, params.queryExecutor.namesProvider).adaptToNamespaceComponents(qualifiedName.kind)
    else
      emptyList()

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind,
                          params: WebSymbolsListSymbolsQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    if (isNamespacedKind(qualifiedKind))
      getJSPropertySymbols().adaptToNamespaceComponents(qualifiedKind.kind)
    else
      emptyList()

  private fun List<JSPropertySymbol>.adaptToNamespaceComponents(kind: SymbolKind) =
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
    Objects.hash(name, source)

  private class VueNamespacedComponent(delegate: VueComponentSymbol) : PsiSourcedWebSymbolDelegate<VueComponentSymbol>(delegate) {

    private val namespaceSymbol = VueComponentNamespaceSymbol(delegate.name, delegate.rawSource as JSPsiNamedElementBase)

    override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean =
      isNamespacedKind(qualifiedKind) || super.isExclusiveFor(qualifiedKind)

    override fun createPointer(): Pointer<out PsiSourcedWebSymbol> {
      val delegatePtr = delegate.createPointer()
      return Pointer {
        delegatePtr.dereference()?.let { VueNamespacedComponent(it) }
      }
    }

    override val queryScope: List<WebSymbolsScope>
      get() = listOf(this)

    override fun getMatchingSymbols(qualifiedName: WebSymbolQualifiedName,
                                    params: WebSymbolsNameMatchQueryParams,
                                    scope: Stack<WebSymbolsScope>): List<WebSymbol> =
      namespaceSymbol.getMatchingSymbols(qualifiedName, params, scope) +
      super.getMatchingSymbols(qualifiedName, params, scope)

    override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind,
                            params: WebSymbolsListSymbolsQueryParams,
                            scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
      namespaceSymbol.getSymbols(qualifiedKind, params, scope) +
      super.getSymbols(qualifiedKind, params, scope)

    override fun getCodeCompletions(qualifiedName: WebSymbolQualifiedName,
                                    params: WebSymbolsCodeCompletionQueryParams,
                                    scope: Stack<WebSymbolsScope>): List<WebSymbolCodeCompletionItem> =
      namespaceSymbol.getCodeCompletions(qualifiedName, params, scope) +
      super.getCodeCompletions(qualifiedName, params, scope)

    override fun equals(other: Any?): Boolean =
      other is VueNamespacedComponent && other.delegate == delegate

    override fun hashCode(): Int =
      delegate.hashCode()

  }

  companion object {
    private fun isNamespacedKind(qualifiedKind: WebSymbolQualifiedKind) =
      qualifiedKind == VUE_COMPONENT_NAMESPACES || qualifiedKind == VUE_COMPONENTS
  }

}