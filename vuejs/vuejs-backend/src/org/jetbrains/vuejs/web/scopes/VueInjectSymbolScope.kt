// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.JS_STRING_LITERALS
import com.intellij.polySymbols.query.PolySymbolCodeCompletionQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.polySymbols.utils.ReferencingPolySymbol
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.jetbrains.vuejs.model.VueProvide
import org.jetbrains.vuejs.model.provides
import org.jetbrains.vuejs.model.source.VueSourceComponent
import org.jetbrains.vuejs.web.VUE_PROVIDES

class VueInjectSymbolScope(private val enclosingComponent: VueSourceComponent) :
  PolySymbolScopeWithCache<VueSourceComponent, Unit>(enclosingComponent.source.project, enclosingComponent, Unit) {

  override fun provides(kind: PolySymbolKind): Boolean =
    kind == VUE_PROVIDES
    || kind == JS_STRING_LITERALS
    || kind == JS_PROPERTIES

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    enclosingComponent.global.provides.forEach {
      consumer(it)
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
      it.symbol.asSafely<VueProvide>()?.injectionKey == null
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

  private val vueInjectStringSymbol = ReferencingPolySymbol.create(
    JS_STRING_LITERALS,
    "Vue Inject String",
    VUE_PROVIDES
  )

  private val vueInjectPropertySymbol = ReferencingPolySymbol.create(
    JS_PROPERTIES,
    "Vue Inject Property",
    VUE_PROVIDES
  )

}