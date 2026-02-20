// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolApiStatus
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.js.types.JSTypeProperty
import org.angular2.web.Angular2Symbol
import org.angular2.web.NG_DIRECTIVE_ATTRIBUTES

interface Angular2DirectiveAttribute : Angular2Symbol, Angular2Element {

  override val name: String

  @PolySymbol.Property(JSTypeProperty::class)
  val type: JSType?

  val required: Boolean? get() = null

  override val modifiers: Set<PolySymbolModifier>
    get() = when (required) {
      true -> setOf(PolySymbolModifier.REQUIRED)
      false -> setOf(PolySymbolModifier.OPTIONAL)
      null -> emptySet()
    }

  override val kind: PolySymbolKind
    get() = NG_DIRECTIVE_ATTRIBUTES

  override val apiStatus: PolySymbolApiStatus

  override fun createPointer(): Pointer<out Angular2DirectiveAttribute>
}
