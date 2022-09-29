// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.containers

import com.intellij.model.Pointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider

object VueTopLevelElementsContainer : WebSymbolsContainer {

  override fun getSymbols(namespace: SymbolNamespace?,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
    if (namespace == WebSymbol.NAMESPACE_HTML && kind == WebSymbol.KIND_HTML_ELEMENTS)
      params.registry.runNameMatchQuery(
        listOfNotNull(WebSymbol.NAMESPACE_HTML, VueWebSymbolsAdditionalContextProvider.KIND_VUE_TOP_LEVEL_ELEMENTS, name),
        context = context,
        virtualSymbols = params.virtualSymbols,
        strictScope = params.strictScope,
        abstractSymbols = params.abstractSymbols,
      )
        .map {
          WebSymbolMatch.create(it.name, it.nameSegments, WebSymbol.NAMESPACE_HTML, WebSymbol.KIND_HTML_ELEMENTS, it.origin)
        }
    else emptyList()

  override fun getCodeCompletions(namespace: SymbolNamespace?,
                                  kind: SymbolKind,
                                  name: String?,
                                  params: WebSymbolsCodeCompletionQueryParams,
                                  context: Stack<WebSymbolsContainer>): List<WebSymbolCodeCompletionItem> =
    if (namespace == WebSymbol.NAMESPACE_HTML && kind == WebSymbol.KIND_HTML_ELEMENTS)
      params.registry.runCodeCompletionQuery(
        listOfNotNull(WebSymbol.NAMESPACE_HTML, VueWebSymbolsAdditionalContextProvider.KIND_VUE_TOP_LEVEL_ELEMENTS, name),
        context = context,
        position = params.position,
        virtualSymbols = params.virtualSymbols,
      )
    else emptyList()

  override fun createPointer(): Pointer<out WebSymbolsContainer> = Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

}