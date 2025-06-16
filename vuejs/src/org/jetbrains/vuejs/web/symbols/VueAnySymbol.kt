// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.polySymbols.types.PROP_JS_TYPE
import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.patterns.PolySymbolsPattern
import com.intellij.polySymbols.patterns.PolySymbolsPatternFactory
import com.intellij.polySymbols.query.PolySymbolWithPattern

class VueAnySymbol(
  override val origin: PolySymbolOrigin,
  override val qualifiedKind: PolySymbolQualifiedKind,
  override val name: String,
  val type: JSType? = null,
) : PolySymbolWithPattern {

  override val pattern: PolySymbolsPattern
    get() = PolySymbolsPatternFactory.createRegExMatch(".*", false)

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PolySymbol.PROP_HIDE_FROM_COMPLETION -> true as? T
      PolySymbol.PROP_DOC_HIDE_PATTERN -> true as? T
      PROP_JS_TYPE -> type as? T
      else -> null
    }

  override fun createPointer(): Pointer<VueAnySymbol> =
    Pointer.hardPointer(this)
}