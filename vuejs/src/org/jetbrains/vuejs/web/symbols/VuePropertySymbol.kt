// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.webSymbols.properties.tryGetJSPropertySymbols
import com.intellij.model.Pointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueProperty

abstract class VuePropertySymbol<T : VueProperty>(item: T, owner: VueComponent, origin: WebSymbolOrigin)
  : VueNamedWebSymbol<T>(item, owner, origin) {
  abstract override fun createPointer(): Pointer<out VuePropertySymbol<T>>

  override fun isExclusiveFor(namespace: SymbolNamespace, kind: SymbolKind): Boolean =
    namespace == WebSymbol.NAMESPACE_JS && kind == WebSymbol.KIND_JS_PROPERTIES

  override fun getSymbols(namespace: SymbolNamespace,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    tryGetJSPropertySymbols(namespace, kind, name) ?: emptyList()

}