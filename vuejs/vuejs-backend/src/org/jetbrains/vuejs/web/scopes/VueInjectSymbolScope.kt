// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.polySymbols.*
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.JS_STRING_LITERALS
import com.intellij.polySymbols.query.PolySymbolCodeCompletionQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.polySymbols.utils.ReferencingPolySymbol
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.model.provides
import org.jetbrains.vuejs.model.source.VueSourceComponent
import org.jetbrains.vuejs.web.VUE_PROVIDES
import org.jetbrains.vuejs.web.VueFramework
import org.jetbrains.vuejs.web.symbols.VueProvideSymbol
import org.jetbrains.vuejs.web.symbols.VueScopeElementOrigin

class VueInjectSymbolScope(private val enclosingComponent: VueSourceComponent)
  : PolySymbolScopeWithCache<VueSourceComponent, Unit>(VueFramework.ID, enclosingComponent.source.project, enclosingComponent, Unit) {

  override fun provides(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    qualifiedKind == VUE_PROVIDES
    || qualifiedKind == JS_STRING_LITERALS
    || qualifiedKind == JS_PROPERTIES

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    val origin = VueScopeElementOrigin(enclosingComponent)
    val provides = enclosingComponent.global.provides

    provides.forEach { (provide, container) ->
      consumer(VueProvideSymbol(provide, container, origin))
    }

    consumer(vueInjectStringSymbol)
    consumer(vueInjectPropertySymbol)

    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  override fun getCodeCompletions(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolCodeCompletionQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbolCodeCompletionItem> {
    return super.getCodeCompletions(qualifiedName, params, stack).filter {
      it.symbol.asSafely<VueProvideSymbol>()?.injectionKey == null
    }
  }

  override fun createPointer(): Pointer<VueInjectSymbolScope> {
    val componentPointer = enclosingComponent.createPointer()
    return Pointer {
      componentPointer.dereference()?.let { VueInjectSymbolScope(it) }
    }
  }

  override fun getModificationCount(): Long =
    PsiModificationTracker.getInstance(enclosingComponent.source.project).modificationCount

  private object VueInjectSymbolOrigin : PolySymbolOrigin {
    override val framework: FrameworkId
      get() = VueFramework.ID
  }

  private val vueInjectStringSymbol = ReferencingPolySymbol.create(
    JS_STRING_LITERALS,
    "Vue Inject String",
    VueInjectSymbolOrigin,
    VUE_PROVIDES
  )

  private val vueInjectPropertySymbol = ReferencingPolySymbol.create(
    JS_PROPERTIES,
    "Vue Inject Property",
    VueInjectSymbolOrigin,
    VUE_PROVIDES
  )

}