// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueScriptSetupLocalDirective
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.KIND_VUE_MODEL
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.PROP_VUE_MODEL_EVENT
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator.Companion.PROP_VUE_MODEL_PROP
import org.jetbrains.vuejs.web.symbols.VueComponentSymbol
import org.jetbrains.vuejs.web.symbols.VueDirectiveSymbol
import org.jetbrains.vuejs.web.symbols.VueScriptSetupLocalDirectiveSymbol

fun WebSymbolElementDescriptor.getModel(): VueModelDirectiveProperties =
  runListSymbolsQuery(NAMESPACE_HTML, KIND_VUE_MODEL, true).firstOrNull()
    ?.let {
      VueModelDirectiveProperties(prop = it.properties[PROP_VUE_MODEL_PROP] as? String,
                                  event = it.properties[PROP_VUE_MODEL_EVENT] as? String)
    }
  ?: VueModelDirectiveProperties()

fun VueScopeElement.asWebSymbol(name: String, forcedProximity: VueModelVisitor.Proximity): WebSymbol? =
  when (this) {
    is VueComponent -> VueComponentSymbol(toAsset(name, true), this, forcedProximity)
    is VueScriptSetupLocalDirective -> VueScriptSetupLocalDirectiveSymbol(this, forcedProximity)
    is VueDirective -> VueDirectiveSymbol(name, this, forcedProximity)
    else -> null
  }

fun VueModelVisitor.Proximity.asWebSymbolPriority(): WebSymbol.Priority =
  when (this) {
    VueModelVisitor.Proximity.LOCAL -> WebSymbol.Priority.HIGHEST
    VueModelVisitor.Proximity.APP -> WebSymbol.Priority.HIGH
    VueModelVisitor.Proximity.PLUGIN, VueModelVisitor.Proximity.GLOBAL -> WebSymbol.Priority.NORMAL
    VueModelVisitor.Proximity.OUT_OF_SCOPE -> WebSymbol.Priority.LOW
  }