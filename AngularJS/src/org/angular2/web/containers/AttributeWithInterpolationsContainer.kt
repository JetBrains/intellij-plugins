// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.containers

import com.intellij.javascript.web.symbols.*
import com.intellij.javascript.web.symbols.patterns.ComplexPattern
import com.intellij.javascript.web.symbols.patterns.ItemPattern
import com.intellij.javascript.web.symbols.patterns.WebSymbolsPattern
import com.intellij.model.Pointer
import com.intellij.util.containers.Stack
import org.angular2.Angular2Framework
import org.angular2.web.Angular2WebSymbolsAdditionalContextProvider

object AttributeWithInterpolationsContainer : WebSymbolsContainer {

  override fun createPointer(): Pointer<out WebSymbolsContainer> =
    Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

  override fun getSymbols(namespace: WebSymbolsContainer.Namespace?,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
    if (namespace == WebSymbolsContainer.Namespace.HTML
        && kind == WebSymbol.KIND_HTML_ATTRIBUTES
        && name != null) {
      AttributeWithInterpolationsSymbol.match(name, context, params)
    }
    else emptyList()

  private object AttributeWithInterpolationsSymbol : WebSymbol {

    override val origin: WebSymbolsContainer.Origin =
      WebSymbolsContainer.OriginData(Angular2Framework.ID)

    override val namespace: WebSymbolsContainer.Namespace
      get() =
        WebSymbolsContainer.Namespace.HTML

    override val kind: SymbolKind
      get() =
        WebSymbol.KIND_HTML_ATTRIBUTES

    override fun createPointer(): Pointer<out WebSymbol> =
      Pointer.hardPointer(this)

    override val pattern: WebSymbolsPattern = ComplexPattern(
      { _, _ ->
        ComplexPattern.ComplexPatternOptions(
          null, false, true,
          WebSymbol.Priority.HIGHEST, null, false, false,
          PropertiesProvider)
      }, defaultDisplayName = null, ItemPattern(null))

  }

  private object PropertiesProvider : WebSymbolsPattern.ItemsProvider {
    override val delegate: WebSymbol? get() = null

    override fun codeCompletion(name: String,
                                contextStack: Stack<WebSymbolsContainer>,
                                registry: WebSymbolsRegistry): List<WebSymbolCodeCompletionItem> =
      emptyList()

    override fun matchName(name: String,
                           contextStack: Stack<WebSymbolsContainer>,
                           registry: WebSymbolsRegistry): List<WebSymbol> =
      registry.runNameMatchQuery(
        listOf(WebSymbolsContainer.NAMESPACE_JS, WebSymbol.KIND_JS_PROPERTIES, name),
        context = contextStack) +
      registry.runNameMatchQuery(
        listOf(WebSymbolsContainer.NAMESPACE_JS, Angular2WebSymbolsAdditionalContextProvider.KIND_NG_DIRECTIVE_INPUTS, name),
        context = contextStack)

  }
}