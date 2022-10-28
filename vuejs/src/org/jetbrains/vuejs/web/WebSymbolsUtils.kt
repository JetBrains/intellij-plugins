// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.model.Symbol
import com.intellij.refactoring.rename.api.RenameTarget
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.refactoring.WebSymbolRenameTarget
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.KIND_VUE_MODEL
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.PROP_VUE_MODEL_EVENT
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.PROP_VUE_MODEL_PROP
import org.jetbrains.vuejs.web.symbols.VueComponentSymbol
import org.jetbrains.vuejs.web.symbols.VueDirectiveSymbol
import org.jetbrains.vuejs.web.symbols.VueScopeElementSymbol

fun WebSymbolElementDescriptor.getModel(): VueModelDirectiveProperties =
  runNameMatchQuery(listOf(KIND_VUE_MODEL)).firstOrNull()
    ?.let {
      VueModelDirectiveProperties(prop = it.properties[PROP_VUE_MODEL_PROP] as? String,
                                  event = it.properties[PROP_VUE_MODEL_EVENT] as? String)
    }
  ?: VueModelDirectiveProperties()

fun VueScopeElement.asWebSymbol(name: String, forcedProximity: VueModelVisitor.Proximity): WebSymbol? =
  when (this) {
    is VueComponent -> VueComponentSymbol(toAsset(name, true), this, forcedProximity)
    is VueDirective -> VueDirectiveSymbol(name, this, forcedProximity)
    else -> null
  }

fun createRenameTarget(symbol: Symbol): RenameTarget? =
  if (symbol is VueScopeElementSymbol<*> && symbol.source is JSLiteralExpression)
    WebSymbolRenameTarget(symbol)
  else null

fun VueModelVisitor.Proximity.asWebSymbolPriority(): WebSymbol.Priority =
  when (this) {
    VueModelVisitor.Proximity.LOCAL -> WebSymbol.Priority.HIGHEST
    VueModelVisitor.Proximity.APP -> WebSymbol.Priority.HIGH
    VueModelVisitor.Proximity.PLUGIN, VueModelVisitor.Proximity.GLOBAL -> WebSymbol.Priority.NORMAL
    VueModelVisitor.Proximity.OUT_OF_SCOPE -> WebSymbol.Priority.LOW
  }