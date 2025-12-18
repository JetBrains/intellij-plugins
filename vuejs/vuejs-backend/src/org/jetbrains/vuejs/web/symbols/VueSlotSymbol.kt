// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.html.HTML_SLOTS
import com.intellij.polySymbols.js.symbols.getJSPropertySymbols
import com.intellij.polySymbols.js.symbols.getMatchingJSPropertySymbols
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolNameMatchQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolWithPattern
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueContainer
import org.jetbrains.vuejs.model.VueSlot

open class VueSlotSymbol private constructor(
  slot: VueSlot,
  owner: VueComponent,
  origin: PolySymbolOrigin,
) : VueNamedPolySymbol<VueSlot>(
  item = slot,
  origin = origin,
  owner = owner,
) {

  companion object {
    fun create(slot: VueSlot, owner: VueComponent, origin: PolySymbolOrigin): VueSlotSymbol =
      slot.pattern?.let { VueSlotSymbolWithPattern(slot, owner, origin, PolySymbolPatternFactory.createRegExMatch(it, true)) }
      ?: VueSlotSymbol(slot, owner, origin)
  }

  override val kind: PolySymbolKind
    get() = HTML_SLOTS

  override val type: JSType?
    get() = item.scope

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> {
    return getJSPropertySymbols(kind)
  }

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> {
    return getMatchingJSPropertySymbols(qualifiedName, params.queryExecutor.namesProvider)
  }

  override fun createPointer(): Pointer<VueSlotSymbol> =
    object : NamedSymbolPointer<VueSlot, VueSlotSymbol>(this) {

      override fun locateSymbol(owner: VueComponent): VueSlot? =
        (owner as? VueContainer)?.slots?.find { it.name == name }

      override fun createWrapper(owner: VueComponent, symbol: VueSlot): VueSlotSymbol =
        create(symbol, owner, origin)

    }

  private class VueSlotSymbolWithPattern(
    slot: VueSlot,
    owner: VueComponent,
    origin: PolySymbolOrigin,
    override val pattern: PolySymbolPattern,
  ) : VueSlotSymbol(slot, owner, origin), PolySymbolWithPattern

}