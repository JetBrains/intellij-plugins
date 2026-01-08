// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.html.HTML_ELEMENTS
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.utils.ReferencingPolySymbol
import org.jetbrains.vuejs.web.VUE_TOP_LEVEL_ELEMENTS

object VueTopLevelElementsScope : PolySymbolScope {

  private val referencingSymbol = ReferencingPolySymbol.create(
    HTML_ELEMENTS,
    "Vue Top Level Element",
    VUE_TOP_LEVEL_ELEMENTS
  )

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (kind == HTML_ELEMENTS)
      listOf(referencingSymbol)
    else
      emptyList()

  override fun createPointer(): Pointer<out PolySymbolScope> = Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

}