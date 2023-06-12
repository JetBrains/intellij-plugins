// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.util.asSafely
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolOrigin
import org.jetbrains.vuejs.model.VueContainer
import org.jetbrains.vuejs.model.VueProvide

class VueInjectSymbol(provide: VueProvide,
                      private val owner: VueContainer,
                      override val origin: WebSymbolOrigin,
                      override val kind: SymbolKind)
  : VueDocumentedItemSymbol<VueProvide>(provide.name, provide) {

  override val namespace: SymbolNamespace
    get() = WebSymbol.NAMESPACE_JS

  override val type: JSType?
    get() = item.jsType

  override val required: Boolean
    get() = false

  override fun equals(other: Any?): Boolean =
    super.equals(other)
    && (other as VueInjectSymbol).kind == kind

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + kind.hashCode()
    return result
  }

  override fun createPointer(): Pointer<VueInjectSymbol> = object : Pointer<VueInjectSymbol> {
    private val name = this@VueInjectSymbol.name
    private val origin = this@VueInjectSymbol.origin
    private val kind = this@VueInjectSymbol.kind
    private val pointer = this@VueInjectSymbol.owner.createPointer()

    override fun dereference(): VueInjectSymbol? =
      pointer.dereference()?.asSafely<VueContainer>()?.let { container ->
        container.provide.find { it.name == name }?.let { provide ->
          VueInjectSymbol(provide, container, origin, kind)
        }
      }
  }
}