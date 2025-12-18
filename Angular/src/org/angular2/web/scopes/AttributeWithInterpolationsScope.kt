// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.polySymbols.*
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.patterns.ComplexPatternOptions
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory
import com.intellij.polySymbols.patterns.PolySymbolPatternSymbolsResolver
import com.intellij.polySymbols.query.*
import com.intellij.polySymbols.utils.match
import org.angular2.web.Angular2SymbolOrigin
import org.angular2.web.NG_DIRECTIVE_INPUTS
import org.angular2.web.PROP_BINDING_PATTERN

object AttributeWithInterpolationsScope : PolySymbolScope {

  override fun createPointer(): Pointer<out PolySymbolScope> =
    Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (qualifiedName.matches(HTML_ATTRIBUTES)) {
      AttributeWithInterpolationsSymbol.match(qualifiedName.name, params, stack)
    }
    else emptyList()

  private object AttributeWithInterpolationsSymbol : PolySymbolWithPattern {

    override val origin: PolySymbolOrigin
      get() = Angular2SymbolOrigin.empty

    override val kind: PolySymbolKind
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

    override val pattern: PolySymbolPattern = PolySymbolPatternFactory.createComplexPattern(
      ComplexPatternOptions(
        null, null, true,
        PolySymbol.Priority.HIGHEST, false, false,
        PropertiesResolver),
      false,
      PolySymbolPatternFactory.createSymbolReferencePlaceholder(null))

  }

  private object PropertiesResolver : PolySymbolPatternSymbolsResolver {
    override fun getSymbolKinds(context: PolySymbol?): Set<PolySymbolKind> = setOf(
      JS_PROPERTIES, NG_DIRECTIVE_INPUTS
    )

    override val delegate: PolySymbol? get() = null

    override fun codeCompletion(
      name: String,
      position: Int,
      stack: PolySymbolQueryStack,
      queryExecutor: PolySymbolQueryExecutor,
    ): List<PolySymbolCodeCompletionItem> =
      emptyList()

    override fun listSymbols(
      stack: PolySymbolQueryStack,
      queryExecutor: PolySymbolQueryExecutor,
      expandPatterns: Boolean,
    ): List<PolySymbol> =
      emptyList()

    override fun matchName(
      name: String,
      stack: PolySymbolQueryStack,
      queryExecutor: PolySymbolQueryExecutor,
    ): List<PolySymbol> =
      queryExecutor.nameMatchQuery(JS_PROPERTIES, name).additionalScope(stack).run() +
      queryExecutor.nameMatchQuery(NG_DIRECTIVE_INPUTS, name).additionalScope(stack).run()

  }
}