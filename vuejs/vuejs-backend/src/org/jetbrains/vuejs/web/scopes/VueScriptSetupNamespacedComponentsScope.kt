// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.query.PolySymbolCodeCompletionQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.web.VUE_COMPONENT_NAMESPACES
import org.jetbrains.vuejs.web.symbols.VueComponentNamespaceSymbol

class VueScriptSetupNamespacedComponentsScope(module: JSExecutionScope) :
  PolySymbolScopeWithCache<JSExecutionScope, Unit>(module.project, module, Unit) {
  override fun createPointer(): Pointer<out PolySymbolScopeWithCache<JSExecutionScope, Unit>> {
    val modulePtr = dataHolder.createSmartPointer()
    return Pointer {
      modulePtr.dereference()?.let { VueScriptSetupNamespacedComponentsScope(it) }
    }
  }

  override fun provides(kind: PolySymbolKind): Boolean =
    kind == VUE_COMPONENT_NAMESPACES

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    JSStubBasedPsiTreeUtil.processDeclarationsInScope(
      dataHolder,
      { element, _ ->
        val name = (element as? JSPsiNamedElementBase)
          ?.let { if (it is ES6ImportSpecifier) it.declaredName else it.name }
        if (name?.getOrNull(0)?.isUpperCase() == true) {
          consumer(VueComponentNamespaceSymbol(name, element))
        }
        true
      },
      false
    )
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  override fun getCodeCompletions(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolCodeCompletionQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbolCodeCompletionItem> {
    return emptyList()
  }
}