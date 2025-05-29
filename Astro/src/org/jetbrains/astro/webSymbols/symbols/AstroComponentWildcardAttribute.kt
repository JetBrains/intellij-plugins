// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols.symbols

import com.intellij.model.Pointer
import com.intellij.polySymbols.SymbolKind
import com.intellij.polySymbols.SymbolNamespace
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbol.Companion.NAMESPACE_HTML
import com.intellij.polySymbols.PolySymbol.Companion.PROP_DOC_HIDE_PATTERN
import com.intellij.polySymbols.PolySymbol.Companion.PROP_HIDE_FROM_COMPLETION
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.patterns.PolySymbolsPattern
import com.intellij.polySymbols.patterns.PolySymbolsPatternFactory
import org.jetbrains.astro.webSymbols.UI_FRAMEWORK_COMPONENT_PROPS

object AstroComponentWildcardAttribute: PolySymbol {
  override val origin: PolySymbolOrigin
    get() = AstroProjectSymbolOrigin

  override val namespace: SymbolNamespace
    get() = NAMESPACE_HTML

  override val kind: SymbolKind
    get() = UI_FRAMEWORK_COMPONENT_PROPS.kind

  override val name: String
    get() = "Component Attribute"

  override val priority: PolySymbol.Priority
    get() = PolySymbol.Priority.LOWEST

  override val pattern: PolySymbolsPattern
    get() = PolySymbolsPatternFactory.createRegExMatch(".*")

  override val properties: Map<String, Any>
    get() = mapOf(PROP_DOC_HIDE_PATTERN to true,
                  PROP_HIDE_FROM_COMPLETION to true)

  override fun createPointer(): Pointer<out PolySymbol> =
    Pointer.hardPointer(this)

}