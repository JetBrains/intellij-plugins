// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol.DocHidePatternProperty
import com.intellij.polySymbols.PolySymbol.HideFromCompletionProperty
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.patterns.PolySymbolPattern
import com.intellij.polySymbols.patterns.PolySymbolPatternFactory
import com.intellij.polySymbols.query.PolySymbolWithPattern
import org.jetbrains.vuejs.model.VueSymbol

class VueAnySymbol(
  override val kind: PolySymbolKind,
  override val name: String,
  override val type: JSType? = null,
) : PolySymbolWithPattern, VueSymbol {

  override val pattern: PolySymbolPattern
    get() = PolySymbolPatternFactory.createRegExMatch(".*", false)

  @PolySymbol.Property(HideFromCompletionProperty::class)
  val hideFromCompletion: Boolean
    get() = true

  @PolySymbol.Property(DocHidePatternProperty::class)
  val docHidePattern: Boolean
    get() = true

  override fun createPointer(): Pointer<VueAnySymbol> =
    Pointer.hardPointer(this)
}