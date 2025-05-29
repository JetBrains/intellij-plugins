// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.PolySymbol
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.PolySymbolQualifiedKind
import com.intellij.webSymbols.PolySymbolsScope
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.utils.ReferencingPolySymbol
import org.jetbrains.vuejs.web.VUE_TOP_LEVEL_ELEMENTS

object VueTopLevelElementsScope : PolySymbolsScope {

  private val referencingSymbol = ReferencingPolySymbol.create(
    PolySymbol.HTML_ELEMENTS,
    "Vue Top Level Element",
    WebSymbolOrigin.empty(),
    VUE_TOP_LEVEL_ELEMENTS
  )

  override fun getSymbols(
    qualifiedKind: PolySymbolQualifiedKind,
    params: WebSymbolsListSymbolsQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbolsScope> =
    if (qualifiedKind == PolySymbol.HTML_ELEMENTS)
      listOf(referencingSymbol)
    else
      emptyList()

  override fun createPointer(): Pointer<out PolySymbolsScope> = Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

}