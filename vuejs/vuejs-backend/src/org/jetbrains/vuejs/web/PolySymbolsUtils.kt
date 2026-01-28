// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.html.elements.HtmlElementSymbolDescriptor
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.web.symbols.VueComponentWithProximity
import org.jetbrains.vuejs.web.symbols.VueDirectiveWithProximity

fun HtmlElementSymbolDescriptor.getModel(): VueModelDirectiveProperties =
  runListSymbolsQuery(VUE_MODEL, true).firstOrNull()
    ?.let {
      VueModelDirectiveProperties(prop = it[PROP_VUE_MODEL_PROP],
                                  event = it[PROP_VUE_MODEL_EVENT])
    }
  ?: VueModelDirectiveProperties()

fun VueModelVisitor.Proximity.asPolySymbolPriority(): PolySymbol.Priority =
  when (this) {
    VueModelVisitor.Proximity.LOCAL -> PolySymbol.Priority.HIGHEST
    VueModelVisitor.Proximity.APP -> PolySymbol.Priority.HIGH
    VueModelVisitor.Proximity.LIBRARY, VueModelVisitor.Proximity.GLOBAL -> PolySymbol.Priority.NORMAL
    VueModelVisitor.Proximity.OUT_OF_SCOPE -> PolySymbol.Priority.LOW
  }

fun PolySymbol.unwrapVueSymbolWithProximity(): PolySymbol =
  (this as? VueComponentWithProximity)?.delegate
  ?: (this as? VueDirectiveWithProximity)?.delegate
  ?: this

internal fun isVueComponentQuery(name: String): Boolean {
  return name.getOrNull(0)?.isUpperCase() == true || name.contains('-') || name == "slot"
}