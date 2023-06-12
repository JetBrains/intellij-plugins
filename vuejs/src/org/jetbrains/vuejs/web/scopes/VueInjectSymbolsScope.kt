// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.KIND_JS_PROPERTIES
import com.intellij.webSymbols.WebSymbol.Companion.KIND_JS_STRING_LITERALS
import com.intellij.webSymbols.WebSymbolsScopeWithCache
import org.jetbrains.vuejs.model.source.VueSourceComponent
import org.jetbrains.vuejs.web.VueFramework
import org.jetbrains.vuejs.web.symbols.VueInjectSymbol
import org.jetbrains.vuejs.web.symbols.VueScopeElementOrigin

class VueInjectSymbolsScope(private val enclosingComponent: VueSourceComponent)
  : WebSymbolsScopeWithCache<VueSourceComponent, Unit>(VueFramework.ID, enclosingComponent.source.project, enclosingComponent, Unit) {

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    if (enclosingComponent.inject.isEmpty()) {
      cacheDependencies.add(PsiModificationTracker.NEVER_CHANGED)
      return
    }

    val origin = VueScopeElementOrigin(enclosingComponent)
    val provides = enclosingComponent.collectProvides()

    enclosingComponent.inject
      .forEach {
        val (provide, provideOwner) = provides[it.from ?: it.name] ?: return@forEach
        consumer(VueInjectSymbol(provide, provideOwner, origin, KIND_JS_PROPERTIES))
        consumer(VueInjectSymbol(provide, provideOwner, origin, KIND_JS_STRING_LITERALS))
      }

    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  override fun createPointer(): Pointer<VueInjectSymbolsScope> {
    val componentPointer = enclosingComponent.createPointer()
    return Pointer {
      componentPointer.dereference()?.let { VueInjectSymbolsScope(it) }
    }
  }

  override fun getModificationCount(): Long =
    PsiModificationTracker.getInstance(enclosingComponent.source.project).modificationCount

}