// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.query.WebSymbolsQueryExecutor
import com.intellij.webSymbols.webTypes.filters.WebSymbolsFilter
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.KIND_VUE_COMPONENT_PROPS

class VueBindFilter : WebSymbolsFilter {
  override fun filterCodeCompletions(codeCompletions: List<WebSymbolCodeCompletionItem>,
                                     queryExecutor: WebSymbolsQueryExecutor,
                                     scope: List<WebSymbolsScope>,
                                     properties: Map<String, Any>): List<WebSymbolCodeCompletionItem> =
    codeCompletions.filterHtmlEventAttributes(queryExecutor, scope) { name }

  override fun filterNameMatches(matches: List<WebSymbol>,
                                 queryExecutor: WebSymbolsQueryExecutor,
                                 scope: List<WebSymbolsScope>,
                                 properties: Map<String, Any>): List<WebSymbol> =
    matches.filterHtmlEventAttributes(queryExecutor, scope) { name }

  private fun <T> List<T>.filterHtmlEventAttributes(queryExecutor: WebSymbolsQueryExecutor,
                                                    scope: List<WebSymbolsScope>,
                                                    getName: T.() -> String): List<T> {
    val props = queryExecutor.runNameMatchQuery(WebSymbol.NAMESPACE_HTML, KIND_VUE_COMPONENT_PROPS, "",
                                                scope = scope).mapTo(HashSet()) { it.name }
    return filter {
      val name = it.getName()
      !name.startsWith("on") || name.startsWith("on-") || name == "on" || props.contains(name)
    }
  }

}