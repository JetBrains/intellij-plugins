// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbol.Companion.PROP_DOC_HIDE_PATTERN
import com.intellij.polySymbols.PolySymbol.Companion.PROP_HIDE_FROM_COMPLETION
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.patterns.PolySymbolsPattern
import com.intellij.polySymbols.patterns.PolySymbolsPatternFactory
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENT_PROPS

object AstroComponentWildcardAttribute : PolySymbol {
  override val origin: PolySymbolOrigin
    get() = AstroProjectSymbolOrigin

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = UI_FRAMEWORK_COMPONENT_PROPS

  override val name: String
    get() = "Component Attribute"

  override val priority: PolySymbol.Priority
    get() = PolySymbol.Priority.LOWEST

  override val pattern: PolySymbolsPattern
    get() = PolySymbolsPatternFactory.createRegExMatch(".*")

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_DOC_HIDE_PATTERN -> property.tryCast(true)
      PROP_HIDE_FROM_COMPLETION -> property.tryCast(true)
      else -> null
    }

  override fun createPointer(): Pointer<out PolySymbol> =
    Pointer.hardPointer(this)

}