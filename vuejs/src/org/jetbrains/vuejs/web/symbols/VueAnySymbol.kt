// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.web.symbols.SymbolKind
import com.intellij.javascript.web.symbols.WebSymbol
import com.intellij.javascript.web.symbols.WebSymbolsContainer
import com.intellij.javascript.web.symbols.patterns.RegExpPattern
import com.intellij.javascript.web.symbols.patterns.WebSymbolsPattern
import com.intellij.model.Pointer

class VueAnySymbol(override val origin: WebSymbolsContainer.Origin,
                   override val namespace: WebSymbolsContainer.Namespace,
                   override val kind: SymbolKind,
                   override val matchedName: String) : WebSymbol {

  override val pattern: WebSymbolsPattern
    get() = RegExpPattern(".*", false)

  override val properties: Map<String, Any> =
    mapOf(Pair(WebSymbol.PROP_HIDE_FROM_COMPLETION, true))

  override fun createPointer(): Pointer<VueAnySymbol> =
    Pointer.hardPointer(this)
}