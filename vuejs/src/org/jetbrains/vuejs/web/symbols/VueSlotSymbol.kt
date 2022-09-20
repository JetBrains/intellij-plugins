// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolsContainer
import com.intellij.webSymbols.patterns.RegExpPattern
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueContainer
import org.jetbrains.vuejs.model.VueSlot

class VueSlotSymbol(slot: VueSlot,
                    owner: VueComponent,
                    origin: WebSymbolsContainer.Origin)
  : VueNamedWebSymbol<VueSlot>(slot, origin = origin, owner = owner) {

  override val pattern: WebSymbolsPattern?
    get() = item.pattern?.let { RegExpPattern(it, true) }

  override val kind: SymbolKind
    get() = WebSymbol.KIND_HTML_SLOTS

  override val type: JSType?
    get() = item.scope

  override fun createPointer(): Pointer<VueNamedWebSymbol<VueSlot>> =
    object : NamedSymbolPointer<VueSlot>(this) {

      override fun locateSymbol(owner: VueComponent): VueSlot? =
        (owner as? VueContainer)?.slots?.find { it.name == name }

      override fun createWrapper(owner: VueComponent, symbol: VueSlot): VueNamedWebSymbol<VueSlot> =
        VueSlotSymbol(symbol, owner, origin)

    }

}