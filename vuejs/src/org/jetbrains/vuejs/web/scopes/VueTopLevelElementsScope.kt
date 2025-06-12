// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.html.HTML_ELEMENTS
import com.intellij.polySymbols.query.PolySymbolsListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolsScope
import com.intellij.polySymbols.utils.ReferencingPolySymbol
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.web.VUE_TOP_LEVEL_ELEMENTS

object VueTopLevelElementsScope : PolySymbolsScope {

  private val referencingSymbol = ReferencingPolySymbol.create(
    HTML_ELEMENTS,
    "Vue Top Level Element",
    PolySymbolOrigin.empty(),
    VUE_TOP_LEVEL_ELEMENTS
  )

  override fun getSymbols(
    qualifiedKind: PolySymbolQualifiedKind,
    params: PolySymbolsListSymbolsQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    if (qualifiedKind == HTML_ELEMENTS)
      listOf(referencingSymbol)
    else
      emptyList()

  override fun createPointer(): Pointer<out PolySymbolsScope> = Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

}