// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import org.jetbrains.vuejs.model.VueDirectiveModifier
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_MODIFIERS
import org.jetbrains.vuejs.web.asPolySymbolPriority

class VueDirectiveModifierSymbol(
  modifier: VueDirectiveModifier,
  private val vueProximity: VueModelVisitor.Proximity,
) : VueDocumentedItemSymbol<VueDirectiveModifier>(modifier.name, modifier) {

  override val origin: PolySymbolOrigin
    get() = PolySymbolOrigin.empty()

  override val kind: PolySymbolKind
    get() = VUE_DIRECTIVE_MODIFIERS

  override val priority: PolySymbol.Priority
    get() = vueProximity.asPolySymbolPriority()

  override fun createPointer(): Pointer<out VueDirectiveModifierSymbol> {
    val modifier = item.createPointer()
    val vueProximity = this.vueProximity
    return Pointer {
      modifier.dereference()
        ?.let { VueDirectiveModifierSymbol(it, vueProximity) }
    }
  }
}
