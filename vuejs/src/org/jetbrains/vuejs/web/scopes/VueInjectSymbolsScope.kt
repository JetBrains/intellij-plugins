// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.KIND_JS_PROPERTIES
import com.intellij.webSymbols.WebSymbol.Companion.KIND_JS_STRING_LITERALS
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_JS
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.query.WebSymbolsCodeCompletionQueryParams
import com.intellij.webSymbols.utils.ReferencingWebSymbol
import org.jetbrains.vuejs.model.provides
import org.jetbrains.vuejs.model.source.VueSourceComponent
import org.jetbrains.vuejs.web.VueFramework
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.KIND_VUE_PROVIDES
import org.jetbrains.vuejs.web.symbols.VueProvideSymbol
import org.jetbrains.vuejs.web.symbols.VueScopeElementOrigin

class VueInjectSymbolsScope(private val enclosingComponent: VueSourceComponent)
  : WebSymbolsScopeWithCache<VueSourceComponent, Unit>(VueFramework.ID, enclosingComponent.source.project, enclosingComponent, Unit) {

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    val origin = VueScopeElementOrigin(enclosingComponent)
    val provides = enclosingComponent.global.provides

    provides.forEach { (provide, container) ->
      consumer(VueProvideSymbol(provide, container, origin))
    }

    consumer(vueInjectStringSymbol)
    consumer(vueInjectPropertySymbol)

    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  override fun getCodeCompletions(namespace: SymbolNamespace,
                                  kind: SymbolKind,
                                  name: String?,
                                  params: WebSymbolsCodeCompletionQueryParams,
                                  scope: Stack<WebSymbolsScope>): List<WebSymbolCodeCompletionItem> {
    return super.getCodeCompletions(namespace, kind, name, params, scope).filter {
      it.symbol.asSafely<VueProvideSymbol>()?.injectionKey == null
    }
  }

  override fun createPointer(): Pointer<VueInjectSymbolsScope> {
    val componentPointer = enclosingComponent.createPointer()
    return Pointer {
      componentPointer.dereference()?.let { VueInjectSymbolsScope(it) }
    }
  }

  override fun getModificationCount(): Long =
    PsiModificationTracker.getInstance(enclosingComponent.source.project).modificationCount

  private object VueInjectSymbolOrigin : WebSymbolOrigin {
    override val framework: FrameworkId
      get() = VueFramework.ID
  }

  private val vueInjectStringSymbol = ReferencingWebSymbol(
    NAMESPACE_JS,
    KIND_JS_STRING_LITERALS,
    "Vue Inject String",
    VueInjectSymbolOrigin,
    WebSymbolQualifiedKind(NAMESPACE_JS, KIND_VUE_PROVIDES)
  )

  private val vueInjectPropertySymbol = ReferencingWebSymbol(
    NAMESPACE_JS,
    KIND_JS_PROPERTIES,
    "Vue Inject Property",
    VueInjectSymbolOrigin,
    WebSymbolQualifiedKind(NAMESPACE_JS, KIND_VUE_PROVIDES)
  )

}