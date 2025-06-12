// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.polySymbols.*
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.patterns.ComplexPatternOptions
import com.intellij.polySymbols.patterns.PolySymbolsPattern
import com.intellij.polySymbols.patterns.PolySymbolsPatternFactory
import com.intellij.polySymbols.patterns.PolySymbolsPatternSymbolsResolver
import com.intellij.polySymbols.query.PolySymbolsNameMatchQueryParams
import com.intellij.polySymbols.query.PolySymbolsQueryExecutor
import com.intellij.polySymbols.query.PolySymbolsScope
import com.intellij.polySymbols.utils.match
import com.intellij.util.containers.Stack
import org.angular2.web.Angular2SymbolOrigin
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.PROP_BINDING_PATTERN

object AttributeWithInterpolationsScope : PolySymbolsScope {

  override fun createPointer(): Pointer<out PolySymbolsScope> =
    Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolsNameMatchQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    if (qualifiedName.matches(HTML_ATTRIBUTES)) {
      AttributeWithInterpolationsSymbol.match(qualifiedName.name, params, scope)
    }
    else emptyList()

  private object AttributeWithInterpolationsSymbol : PolySymbol {

    override val origin: PolySymbolOrigin
      get() = Angular2SymbolOrigin.empty

    override val qualifiedKind: PolySymbolQualifiedKind
      get() = HTML_ATTRIBUTES

    override val name: String
      get() = "Attribute with interpolations"


    override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
      when (property) {
        PROP_BINDING_PATTERN -> property.tryCast(true)
        else -> super.get(property)
      }

    override fun createPointer(): Pointer<out PolySymbol> =
      Pointer.hardPointer(this)

    override val pattern: PolySymbolsPattern = PolySymbolsPatternFactory.createComplexPattern(
      ComplexPatternOptions(
        null, null, true,
        PolySymbol.Priority.HIGHEST, false, false,
        PropertiesResolver),
      false,
      PolySymbolsPatternFactory.createSymbolReferencePlaceholder(null))

  }

  private object PropertiesResolver : PolySymbolsPatternSymbolsResolver {
    override fun getSymbolKinds(context: PolySymbol?): Set<PolySymbolQualifiedKind> = setOf(
      JS_PROPERTIES, NG_DIRECTIVE_INPUTS
    )

    override val delegate: PolySymbol? get() = null

    override fun codeCompletion(
      name: String,
      position: Int,
      scopeStack: Stack<PolySymbolsScope>,
      queryExecutor: PolySymbolsQueryExecutor,
    ): List<PolySymbolCodeCompletionItem> =
      emptyList()

    override fun listSymbols(
      scopeStack: Stack<PolySymbolsScope>,
      queryExecutor: PolySymbolsQueryExecutor,
      expandPatterns: Boolean,
    ): List<PolySymbol> =
      emptyList()

    override fun matchName(
      name: String,
      scopeStack: Stack<PolySymbolsScope>,
      queryExecutor: PolySymbolsQueryExecutor,
    ): List<PolySymbol> =
      queryExecutor.nameMatchQuery(JS_PROPERTIES, name).additionalScope(scopeStack).run() +
      queryExecutor.nameMatchQuery(NG_DIRECTIVE_INPUTS, name).additionalScope(scopeStack).run()

  }
}