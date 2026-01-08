// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolOrigin
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueNamedSymbol

abstract class VueNamedPolySymbol<T : VueNamedSymbol>(
  item: T,
  protected val owner: VueComponent,
) : VueDocumentedItemSymbol<T>(item.name, item) {

  abstract override fun createPointer(): Pointer<out VueNamedPolySymbol<T>>

  abstract class NamedSymbolPointer<T : VueNamedSymbol, S : VueNamedPolySymbol<T>>(wrapper: S) : Pointer<S> {
    val name: String = wrapper.item.name
    private val owner = wrapper.owner.createPointer()

    override fun dereference(): S? =
      owner.dereference()?.let { component ->
        locateSymbol(component)
          ?.let { createWrapper(component, it) }
      }

    abstract fun locateSymbol(owner: VueComponent): T?

    abstract fun createWrapper(owner: VueComponent, symbol: T): S

  }

}