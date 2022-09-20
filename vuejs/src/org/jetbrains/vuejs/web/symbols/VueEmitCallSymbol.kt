// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.web.symbols.SymbolKind
import com.intellij.javascript.web.symbols.WebSymbol
import com.intellij.javascript.web.symbols.WebSymbolsContainer
import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueContainer
import org.jetbrains.vuejs.model.VueEmitCall

class VueEmitCallSymbol(emitCall: VueEmitCall,
                        owner: VueComponent,
                        origin: WebSymbolsContainer.Origin)
  : VueNamedWebSymbol<VueEmitCall>(emitCall, origin = origin, owner = owner) {

  override val namespace: WebSymbolsContainer.Namespace
    get() = WebSymbolsContainer.Namespace.JS

  override val kind: SymbolKind
    get() = WebSymbol.KIND_JS_EVENTS

  override val type: JSType?
    get() = item.eventJSType

  override val priority: WebSymbol.Priority
    get() = WebSymbol.Priority.HIGHEST

  override fun createPointer(): Pointer<VueNamedWebSymbol<VueEmitCall>> =
    object : NamedSymbolPointer<VueEmitCall>(this) {

      override fun locateSymbol(owner: VueComponent): VueEmitCall? =
        (owner as? VueContainer)?.emits?.find { it.name == name }

      override fun createWrapper(owner: VueComponent, symbol: VueEmitCall): VueNamedWebSymbol<VueEmitCall> =
        VueEmitCallSymbol(symbol, owner, origin)

    }
}