// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.PolySymbol.Companion.JS_PROPERTIES
import com.intellij.webSymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.webSymbols.patterns.ComplexPatternOptions
import com.intellij.webSymbols.patterns.PolySymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory
import com.intellij.webSymbols.patterns.WebSymbolsPatternSymbolsResolver
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import com.intellij.webSymbols.query.WebSymbolsQueryExecutor
import com.intellij.webSymbols.utils.match
import org.angular2.web.Angular2SymbolOrigin
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.PROP_BINDING_PATTERN

object AttributeWithInterpolationsScope : PolySymbolsScope {

  override fun createPointer(): Pointer<out PolySymbolsScope> =
    Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: WebSymbolsNameMatchQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    if (qualifiedName.matches(PolySymbol.HTML_ATTRIBUTES)) {
      AttributeWithInterpolationsSymbol.match(qualifiedName.name, params, scope)
    }
    else emptyList()

  private object AttributeWithInterpolationsSymbol : PolySymbol {

    override val origin: PolySymbolOrigin
      get() = Angular2SymbolOrigin.empty

    override val namespace: SymbolNamespace
      get() =
        PolySymbol.NAMESPACE_HTML

    override val kind: SymbolKind
      get() =
        PolySymbol.KIND_HTML_ATTRIBUTES

    override val name: String
      get() = "Attribute with interpolations"

    override val properties: Map<String, Any>
      get() = mapOf(PROP_BINDING_PATTERN to true)

    override fun createPointer(): Pointer<out PolySymbol> =
      Pointer.hardPointer(this)

    override val pattern: PolySymbolsPattern = WebSymbolsPatternFactory.createComplexPattern(
      ComplexPatternOptions(
        null, null, true,
        PolySymbol.Priority.HIGHEST, null, false, false,
        PropertiesResolver),
      false,
      WebSymbolsPatternFactory.createSymbolReferencePlaceholder(null))

  }

  private object PropertiesResolver : WebSymbolsPatternSymbolsResolver {
    override fun getSymbolKinds(context: PolySymbol?): Set<PolySymbolQualifiedKind> = setOf(
      JS_PROPERTIES, NG_DIRECTIVE_INPUTS
    )

    override val delegate: PolySymbol? get() = null

    override fun codeCompletion(
      name: String,
      position: Int,
      scopeStack: Stack<PolySymbolsScope>,
      queryExecutor: WebSymbolsQueryExecutor,
    ): List<PolySymbolCodeCompletionItem> =
      emptyList()

    override fun listSymbols(
      scopeStack: Stack<PolySymbolsScope>,
      queryExecutor: WebSymbolsQueryExecutor,
      expandPatterns: Boolean,
    ): List<PolySymbol> =
      emptyList()

    override fun matchName(
      name: String,
      scopeStack: Stack<PolySymbolsScope>,
      queryExecutor: WebSymbolsQueryExecutor,
    ): List<PolySymbol> =
      queryExecutor.runNameMatchQuery(JS_PROPERTIES.withName(name), additionalScope = scopeStack) +
      queryExecutor.runNameMatchQuery(NG_DIRECTIVE_INPUTS.withName(name), additionalScope = scopeStack)

  }
}