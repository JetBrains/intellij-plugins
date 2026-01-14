// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.symbols.getJSPropertySymbols
import com.intellij.polySymbols.js.symbols.getMatchingJSPropertySymbols
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolNameMatchQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.model.VueProperty

interface VuePropertySymbolMixin : VueDocumentedItemSymbolMixin, VueProperty, PolySymbolScope {

  abstract override fun createPointer(): Pointer<out VuePropertySymbolMixin>

  override fun getModificationCount(): Long = -1

  override fun isExclusiveFor(kind: PolySymbolKind): Boolean =
    kind == JS_PROPERTIES

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    getMatchingJSPropertySymbols(qualifiedName, params.queryExecutor.namesProvider)

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    getJSPropertySymbols(kind)

}