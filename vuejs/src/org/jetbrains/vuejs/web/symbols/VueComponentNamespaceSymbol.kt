// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.web.js.symbols.getJSPropertySymbols
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.types.JSPsiBasedTypeOfType
import com.intellij.model.Pointer
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import org.jetbrains.vuejs.model.VueLocallyDefinedRegularComponent
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueRegularComponent
import org.jetbrains.vuejs.web.VueFramework
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.KIND_VUE_COMPONENTS
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.KIND_VUE_COMPONENT_NAMESPACES
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
    get() = NAMESPACE_JS

  override val kind: SymbolKind
    get() = KIND_VUE_COMPONENT_NAMESPACES

  override fun isExclusiveFor(namespace: SymbolNamespace, kind: SymbolKind): Boolean =
    isNamespacedKind(namespace, kind)

  override fun getSymbols(namespace: SymbolNamespace,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    if (isNamespacedKind(namespace, kind) && name?.getOrNull(0)?.isUpperCase() != false) {
      getJSPropertySymbols(name).mapNotNull { symbol ->
        val source = symbol.source as? JSPsiNamedElementBase ?: return@mapNotNull null
        val component = VueModelManager.getComponent(source) as? VueRegularComponent
        if (component != null && kind == KIND_VUE_COMPONENTS) {
          VueNamespacedComponent(
            VueComponentSymbol(symbol.name, VueLocallyDefinedRegularComponent(component, source), VueModelVisitor.Proximity.LOCAL))
        }
        else if (component == null && kind == KIND_VUE_COMPONENT_NAMESPACES) {
          VueComponentNamespaceSymbol(symbol.name, source)
        }
        else null
      }
    }
    else emptyList()

  override fun equals(other: Any?): Boolean =
    other is VueComponentNamespaceSymbol
    && other.name == name
    && other.source == source

  override fun hashCode(): Int =
    Objects.hash(name, source)

  private class VueNamespacedComponent(delegate: VueComponentSymbol) : PsiSourcedWebSymbolDelegate<VueComponentSymbol>(delegate) {

    private val namespaceSymbol = VueComponentNamespaceSymbol(delegate.name, delegate.rawSource as JSPsiNamedElementBase)

    override fun isExclusiveFor(namespace: SymbolNamespace, kind: SymbolKind): Boolean =
      isNamespacedKind(namespace, kind) || super.isExclusiveFor(namespace, kind)

    override fun createPointer(): Pointer<out PsiSourcedWebSymbol> {
      val delegatePtr = delegate.createPointer()
      return Pointer {
        delegatePtr.dereference()?.let { VueNamespacedComponent(it) }
      }
    }

    override val queryScope: List<WebSymbolsScope>
      get() = listOf(this)

    override fun getSymbols(namespace: SymbolNamespace,
                            kind: SymbolKind,
                            name: String?,
                            params: WebSymbolsNameMatchQueryParams,
                            scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
      namespaceSymbol.getSymbols(namespace, kind, name, params, scope) +
      super.getSymbols(namespace, kind, name, params, scope)

    override fun getCodeCompletions(namespace: SymbolNamespace,
                                    kind: SymbolKind,
                                    name: String?,
                                    params: WebSymbolsCodeCompletionQueryParams,
                                    scope: Stack<WebSymbolsScope>): List<WebSymbolCodeCompletionItem> =
      namespaceSymbol.getCodeCompletions(namespace, kind, name, params, scope) +
      super.getCodeCompletions(namespace, kind, name, params, scope)

    override fun equals(other: Any?): Boolean =
      other is VueNamespacedComponent && other.delegate == delegate

    override fun hashCode(): Int =
      delegate.hashCode()

  }

  companion object {
    private fun isNamespacedKind(namespace: String, kind: String) =
      (namespace == NAMESPACE_JS && kind == KIND_VUE_COMPONENT_NAMESPACES)
      || (namespace == NAMESPACE_HTML && kind == KIND_VUE_COMPONENTS)
  }

}