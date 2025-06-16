// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.polySymbols.symbols.getJSPropertySymbols
import com.intellij.javascript.polySymbols.symbols.getMatchingJSPropertySymbols
import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.html.HTML_SLOTS
import com.intellij.polySymbols.patterns.PolySymbolsPattern
import com.intellij.polySymbols.patterns.PolySymbolsPatternFactory
import com.intellij.polySymbols.query.PolySymbolWithPattern
import com.intellij.polySymbols.query.PolySymbolsListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolsNameMatchQueryParams
import com.intellij.polySymbols.query.PolySymbolsScope
import com.intellij.util.containers.Stack
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
      slot.pattern?.let { VueSlotSymbolWithPattern(slot, owner, origin, PolySymbolsPatternFactory.createRegExMatch(it, true)) }
      ?: VueSlotSymbol(slot, owner, origin)
  }

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = HTML_SLOTS

  override val type: JSType?
    get() = item.scope

  override fun getSymbols(
    qualifiedKind: PolySymbolQualifiedKind,
    params: PolySymbolsListSymbolsQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> {
    return getJSPropertySymbols(qualifiedKind)
  }

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolsNameMatchQueryParams,
    scope: Stack<PolySymbolsScope>,
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
    override val pattern: PolySymbolsPattern,
  ) : VueSlotSymbol(slot, owner, origin), PolySymbolWithPattern

}