// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols.symbols

import com.intellij.model.Pointer
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.KIND_HTML_ATTRIBUTES
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.WebSymbol.Companion.PROP_DOC_HIDE_PATTERN
import com.intellij.webSymbols.WebSymbol.Companion.PROP_HIDE_FROM_COMPLETION
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory

object AstroComponentWildcardAttribute: WebSymbol {
  override val origin: WebSymbolOrigin
    get() = AstroProjectSymbolOrigin

  override val namespace: SymbolNamespace
    get() = NAMESPACE_HTML

  override val kind: SymbolKind
    get() = KIND_HTML_ATTRIBUTES

  override val name: String
    get() = "Component Attribute"

  override val priority: WebSymbol.Priority
    get() = WebSymbol.Priority.LOWEST

  override val pattern: WebSymbolsPattern
    get() = WebSymbolsPatternFactory.createRegExMatch(".*")

  override val properties: Map<String, Any>
    get() = mapOf(PROP_DOC_HIDE_PATTERN to true,
                  PROP_HIDE_FROM_COMPLETION to true)

  override fun createPointer(): Pointer<out WebSymbol> =
    Pointer.hardPointer(this)

}