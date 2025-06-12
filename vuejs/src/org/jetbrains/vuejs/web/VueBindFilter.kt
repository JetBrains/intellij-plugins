// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.query.PolySymbolsQueryExecutor
import com.intellij.polySymbols.query.PolySymbolsScope
import com.intellij.polySymbols.webTypes.filters.PolySymbolsFilter

class VueBindFilter : PolySymbolsFilter {
  override fun filterCodeCompletions(
    codeCompletions: List<PolySymbolCodeCompletionItem>,
    queryExecutor: PolySymbolsQueryExecutor,
    scope: List<PolySymbolsScope>,
    properties: Map<String, Any>,
  ): List<PolySymbolCodeCompletionItem> =
    codeCompletions.filterHtmlEventAttributes(queryExecutor, scope) { name }

  override fun filterNameMatches(
    matches: List<PolySymbol>,
    queryExecutor: PolySymbolsQueryExecutor,
    scope: List<PolySymbolsScope>,
    properties: Map<String, Any>,
  ): List<PolySymbol> =
    matches.filterHtmlEventAttributes(queryExecutor, scope) { name }

  private fun <T> List<T>.filterHtmlEventAttributes(
    queryExecutor: PolySymbolsQueryExecutor,
    scope: List<PolySymbolsScope>,
    getName: T.() -> String,
  ): List<T> {
    val props = queryExecutor.listSymbolsQuery(VUE_COMPONENT_PROPS, true)
      .additionalScope(scope)
      .run()
      .mapTo(HashSet()) { it.name }
    return filter {
      val name = it.getName()
      !name.startsWith("on") || name.startsWith("on-") || name == "on" || props.contains(name)
    }
  }

}