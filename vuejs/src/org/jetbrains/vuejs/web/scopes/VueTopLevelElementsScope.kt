// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

import com.intellij.model.Pointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.utils.ReferencingWebSymbol
import org.jetbrains.vuejs.web.VUE_TOP_LEVEL_ELEMENTS

object VueTopLevelElementsScope : WebSymbolsScope {

  private val referencingSymbol = ReferencingWebSymbol(
    WebSymbol.HTML_ELEMENTS,
    "Vue Top Level Element",
    WebSymbolOrigin.empty(),
    VUE_TOP_LEVEL_ELEMENTS
  )

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind,
                          params: WebSymbolsListSymbolsQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    if (qualifiedKind == WebSymbol.HTML_ELEMENTS)
      listOf(referencingSymbol)
    else
      emptyList()

  override fun createPointer(): Pointer<out WebSymbolsScope> = Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

}