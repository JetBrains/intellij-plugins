// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbol.Companion.PROP_DOC_HIDE_PATTERN
import com.intellij.polySymbols.PolySymbol.Companion.PROP_HIDE_FROM_COMPLETION
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory
import com.intellij.polySymbols.query.PolySymbolWithPattern
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENT_PROPS

object AstroComponentWildcardAttribute : PolySymbolWithPattern, AstroSymbol {
  override val origin: PolySymbolOrigin
    get() = PolySymbolOrigin.empty()

  override val kind: PolySymbolKind
    get() = UI_FRAMEWORK_COMPONENT_PROPS

  override val name: String
    get() = "Component Attribute"

  override val priority: PolySymbol.Priority
    get() = PolySymbol.Priority.LOWEST

  override val pattern: PolySymbolPattern
    get() = PolySymbolPatternFactory.createRegExMatch(".*")

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_DOC_HIDE_PATTERN -> property.tryCast(true)
      PROP_HIDE_FROM_COMPLETION -> property.tryCast(true)
      else -> super<AstroSymbol>.get(property)
    }

  override fun createPointer(): Pointer<out PolySymbol> =
    Pointer.hardPointer(this)

}