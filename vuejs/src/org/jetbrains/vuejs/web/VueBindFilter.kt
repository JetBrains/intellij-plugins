// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.javascript.web.symbols.*

class VueBindFilter : WebSymbolsFilter {
  override fun filterCodeCompletions(codeCompletions: List<WebSymbolCodeCompletionItem>,
                                     registry: WebSymbolsRegistry,
                                     context: List<WebSymbolsContainer>,
                                     properties: Map<String, Any>): List<WebSymbolCodeCompletionItem> {
    val props = registry.runNameMatchQuery(listOf(WebSymbolsContainer.NAMESPACE_HTML, WebSymbol.KIND_HTML_VUE_COMPONENT_PROPS),
                               context = context).mapTo(HashSet()) {it.name}
    return codeCompletions.filter { !it.name.startsWith("on") || props.contains(it.name)}
  }

  override fun filterNameMatches(matches: List<WebSymbol>,
                                 registry: WebSymbolsRegistry,
                                 context: List<WebSymbolsContainer>,
                                 properties: Map<String, Any>): List<WebSymbol> {
    val props = registry.runNameMatchQuery(listOf(WebSymbolsContainer.NAMESPACE_HTML, WebSymbol.KIND_HTML_VUE_COMPONENT_PROPS),
                                           context = context).mapTo(HashSet()) {it.name}
    return matches.filter { !it.name.startsWith("on") || props.contains(it.name) }
  }
}