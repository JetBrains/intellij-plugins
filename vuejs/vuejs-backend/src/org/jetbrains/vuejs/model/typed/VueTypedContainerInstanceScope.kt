// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.js.symbols.getJSPropertySymbols
import com.intellij.polySymbols.js.types.PROP_JS_TYPE
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiModificationTracker

internal class VueTypedContainerInstanceScope(container: VueTypedContainer) :
  PolySymbolScopeWithCache<PsiElement, Unit>(container.source.project, container.source, Unit) {

  private val containerPointer = container.createPointer()

  override fun provides(kind: PolySymbolKind): Boolean =
    kind == JS_PROPERTIES

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)

    val container = containerPointer.dereference() ?: return

    ContainerTypeWrapper(container)
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

  private class ContainerTypeWrapper(val container: VueTypedContainer) : PolySymbol {
    override val kind: PolySymbolKind
      get() = JS_SYMBOLS

    override val name: @NlsSafe String
      get() = "wrapper"

    override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
      when (property) {
        PROP_JS_TYPE -> property.tryCast(container.thisType)
        else -> null
      }

    override fun createPointer(): Pointer<out PolySymbol> {
      val containerPtr = container.createPointer()
      return Pointer {
        containerPtr.dereference()?.let { ContainerTypeWrapper(it) }
      }
    }
  }

}
