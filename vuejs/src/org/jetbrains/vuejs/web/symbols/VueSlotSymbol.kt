// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.polySymbols.symbols.getJSPropertySymbols
import com.intellij.javascript.polySymbols.symbols.getMatchingJSPropertySymbols
import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.util.containers.Stack
import com.intellij.polySymbols.*
import com.intellij.polySymbols.html.HTML_SLOTS
import com.intellij.polySymbols.patterns.PolySymbolsPattern
import com.intellij.polySymbols.patterns.PolySymbolsPatternFactory
import com.intellij.polySymbols.query.PolySymbolsListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolsNameMatchQueryParams
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueContainer
import org.jetbrains.vuejs.model.VueSlot

class VueSlotSymbol(
  slot: VueSlot,
  owner: VueComponent,
  origin: PolySymbolOrigin,
) : VueNamedPolySymbol<VueSlot>(
  item = slot,
  origin = origin,
  owner = owner,
) {

  override val pattern: PolySymbolsPattern?
    get() = item.pattern?.let { PolySymbolsPatternFactory.createRegExMatch(it, true) }

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = HTML_SLOTS

  override val type: JSType?
    get() = item.scope

  override fun getSymbols(
    qualifiedKind: PolySymbolQualifiedKind,
    params: PolySymbolsListSymbolsQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbolsScope> {
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
        VueSlotSymbol(symbol, owner, origin)

    }

}