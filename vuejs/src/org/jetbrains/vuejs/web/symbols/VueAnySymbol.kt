// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory

class VueAnySymbol(override val origin: WebSymbolOrigin,
                   override val namespace: SymbolNamespace,
                   override val kind: SymbolKind,
                   override val matchedName: String) : WebSymbol {

  override val pattern: WebSymbolsPattern
    get() = WebSymbolsPatternFactory.createRegExMatch(".*", false)

  override val properties: Map<String, Any> =
    mapOf(Pair(WebSymbol.PROP_HIDE_FROM_COMPLETION, true))

  override fun createPointer(): Pointer<VueAnySymbol> =
    Pointer.hardPointer(this)
}