// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.dsl.polySymbol
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.js.symbols.getJSPropertySymbols
import com.intellij.polySymbols.js.types.JSTypeProperty
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.web.getVueSymbolsCacheDependencies

internal class VueTypedContainerInstanceScope(container: VueTypedContainer) :
  PolySymbolScopeWithCache<PsiElement, Unit>(container.source.project, container.source, Unit) {

  private val containerPointer = container.createPointer()

  override fun provides(kind: PolySymbolKind): Boolean =
    kind == JS_PROPERTIES

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)

    val container = containerPointer.dereference() ?: return

    cacheDependencies.addAll(getVueSymbolsCacheDependencies(container.source.project))

    polySymbol(JS_SYMBOLS, "wrapper") {
      val container by dependency(container, VueTypedContainer::createPointer)
      property(JSTypeProperty) {
        container.thisType
      }
    }
      .getJSPropertySymbols()
      .forEach(consumer)
  }

  override fun createPointer(): Pointer<out PolySymbolScopeWithCache<PsiElement, Unit>> {
    val containerPointer = containerPointer
    return Pointer {
      val container = containerPointer.dereference() ?: return@Pointer null
      VueTypedContainerInstanceScope(container)
    }
  }


}
