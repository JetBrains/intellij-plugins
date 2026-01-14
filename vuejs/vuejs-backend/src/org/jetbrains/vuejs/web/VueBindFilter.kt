// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.query.PolySymbolQueryExecutor
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.webTypes.filters.PolySymbolFilter

class VueBindFilter : PolySymbolFilter {
  override fun filterCodeCompletions(
    codeCompletions: List<PolySymbolCodeCompletionItem>,
    queryExecutor: PolySymbolQueryExecutor,
    stack: PolySymbolQueryStack,
    properties: Map<String, Any>,
  ): List<PolySymbolCodeCompletionItem> =
    codeCompletions.filterHtmlEventAttributes(queryExecutor, stack, { name }, { this.symbol?.kind })

  override fun filterNameMatches(
    matches: List<PolySymbol>,
    queryExecutor: PolySymbolQueryExecutor,
    stack: PolySymbolQueryStack,
    properties: Map<String, Any>,
  ): List<PolySymbol> =
    matches.filterHtmlEventAttributes(queryExecutor, stack, { name }, { kind })

  private fun <T> List<T>.filterHtmlEventAttributes(
    queryExecutor: PolySymbolQueryExecutor,
    stack: PolySymbolQueryStack,
    getName: T.() -> String,
    symbolKind: T.() -> PolySymbolKind?,
  ): List<T> {
    val props = queryExecutor.listSymbolsQuery(VUE_COMPONENT_PROPS, true)
      .additionalScope(stack)
      .run()
      .mapTo(HashSet()) { it.name }
    return filter {
      if (it.symbolKind() == VUE_COMPONENT_PROPS) return@filter true
      val name = it.getName()
      !name.startsWith("on") || name.startsWith("on-") || name == "on" || props.contains(name)
    }
  }

}