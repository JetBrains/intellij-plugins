// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.html.polySymbols.elements.PolySymbolElementDescriptor
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolQualifiedName
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueScriptSetupLocalDirective
import org.jetbrains.vuejs.web.symbols.VueComponentSymbol
import org.jetbrains.vuejs.web.symbols.VueDirectiveSymbol
import org.jetbrains.vuejs.web.symbols.VueScriptSetupLocalDirectiveSymbol

fun PolySymbolElementDescriptor.getModel(): VueModelDirectiveProperties =
  runListSymbolsQuery(VUE_MODEL, true).firstOrNull()
    ?.let {
      VueModelDirectiveProperties(prop = it.properties[PROP_VUE_MODEL_PROP] as? String,
                                  event = it.properties[PROP_VUE_MODEL_EVENT] as? String)
    }
  ?: VueModelDirectiveProperties()

fun VueScopeElement.asPolySymbol(name: String, forcedProximity: VueModelVisitor.Proximity): PolySymbol? =
  when (this) {
    is VueComponent -> VueComponentSymbol(toAsset(name, true), this, forcedProximity)
    is VueScriptSetupLocalDirective -> VueScriptSetupLocalDirectiveSymbol(this, forcedProximity)
    is VueDirective -> VueDirectiveSymbol(name, this, forcedProximity)
    else -> null
  }

fun VueModelVisitor.Proximity.asPolySymbolPriority(): PolySymbol.Priority =
  when (this) {
    VueModelVisitor.Proximity.LOCAL -> PolySymbol.Priority.HIGHEST
    VueModelVisitor.Proximity.APP -> PolySymbol.Priority.HIGH
    VueModelVisitor.Proximity.PLUGIN, VueModelVisitor.Proximity.GLOBAL -> PolySymbol.Priority.NORMAL
    VueModelVisitor.Proximity.OUT_OF_SCOPE -> PolySymbol.Priority.LOW
  }

internal fun isVueComponentQuery(qualifiedName: PolySymbolQualifiedName): Boolean {
  return qualifiedName.name.getOrNull(0)?.isUpperCase() == true || qualifiedName.name.contains('-')
}