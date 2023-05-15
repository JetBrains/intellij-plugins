// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.model.Pointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import org.jetbrains.vuejs.web.VueFramework
import org.jetbrains.vuejs.web.symbols.VueComponentNamespaceSymbol

class VueScriptSetupNamespacedComponentsScope(module: JSExecutionScope)
  : WebSymbolsScopeWithCache<JSExecutionScope, Unit>(VueFramework.ID, module.project, module, Unit) {
  override fun createPointer(): Pointer<out WebSymbolsScopeWithCache<JSExecutionScope, Unit>> {
    val modulePtr = dataHolder.createSmartPointer()
    return Pointer {
      modulePtr.dereference()?.let { VueScriptSetupNamespacedComponentsScope(it) }
    }
  }

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
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

  override fun getCodeCompletions(namespace: SymbolNamespace,
                                  kind: SymbolKind,
                                  name: String?,
                                  params: WebSymbolsCodeCompletionQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbolCodeCompletionItem> {
    return emptyList()
  }
}