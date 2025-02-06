// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.JS_PROPERTIES
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.patterns.ComplexPatternOptions
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory
import com.intellij.webSymbols.patterns.WebSymbolsPatternSymbolsResolver
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import com.intellij.webSymbols.query.WebSymbolsQueryExecutor
import com.intellij.webSymbols.utils.match
import org.angular2.web.Angular2SymbolOrigin
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.PROP_BINDING_PATTERN

object AttributeWithInterpolationsScope : WebSymbolsScope {

  override fun createPointer(): Pointer<out WebSymbolsScope> =
    Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

  override fun getMatchingSymbols(
    qualifiedName: WebSymbolQualifiedName,
    params: WebSymbolsNameMatchQueryParams,
    scope: Stack<WebSymbolsScope>,
  ): List<WebSymbol> =
    if (qualifiedName.matches(WebSymbol.HTML_ATTRIBUTES)) {
      AttributeWithInterpolationsSymbol.match(qualifiedName.name, params, scope)
    }
    else emptyList()

  private object AttributeWithInterpolationsSymbol : WebSymbol {

    override val origin: WebSymbolOrigin
      get() = Angular2SymbolOrigin.empty

    override val namespace: SymbolNamespace
      get() =
        WebSymbol.NAMESPACE_HTML

    override val kind: SymbolKind
      get() =
        WebSymbol.KIND_HTML_ATTRIBUTES

    override val name: String
      get() = "Attribute with interpolations"

    override val properties: Map<String, Any>
      get() = mapOf(PROP_BINDING_PATTERN to true)

    override fun createPointer(): Pointer<out WebSymbol> =
      Pointer.hardPointer(this)

    override val pattern: WebSymbolsPattern = WebSymbolsPatternFactory.createComplexPattern(
      ComplexPatternOptions(
        null, null, true,
        WebSymbol.Priority.HIGHEST, null, false, false,
        PropertiesResolver),
      false,
      WebSymbolsPatternFactory.createSymbolReferencePlaceholder(null))

  }

  private object PropertiesResolver : WebSymbolsPatternSymbolsResolver {
    override fun getSymbolKinds(context: WebSymbol?): Set<WebSymbolQualifiedKind> = setOf(
      JS_PROPERTIES, NG_DIRECTIVE_INPUTS
    )

    override val delegate: WebSymbol? get() = null

    override fun codeCompletion(
      name: String,
      position: Int,
      scopeStack: Stack<WebSymbolsScope>,
      queryExecutor: WebSymbolsQueryExecutor,
    ): List<WebSymbolCodeCompletionItem> =
      emptyList()

    override fun listSymbols(
      scopeStack: Stack<WebSymbolsScope>,
      queryExecutor: WebSymbolsQueryExecutor,
      expandPatterns: Boolean,
    ): List<WebSymbol> =
      emptyList()

    override fun matchName(
      name: String,
      scopeStack: Stack<WebSymbolsScope>,
      queryExecutor: WebSymbolsQueryExecutor,
    ): List<WebSymbol> =
      queryExecutor.runNameMatchQuery(JS_PROPERTIES.withName(name), additionalScope = scopeStack) +
      queryExecutor.runNameMatchQuery(NG_DIRECTIVE_INPUTS.withName(name), additionalScope = scopeStack)

  }
}