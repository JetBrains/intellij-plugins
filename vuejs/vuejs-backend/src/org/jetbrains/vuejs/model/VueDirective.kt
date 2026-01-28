// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.model.Pointer
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.web.PROP_VUE_PROXIMITY
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_ARGUMENT
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_MODIFIERS
import org.jetbrains.vuejs.web.symbols.VueAnySymbol
import org.jetbrains.vuejs.web.symbols.VueDirectiveModifierWithProximity

interface VueDirective : VueSymbol, VueScopeElement, PolySymbolScope {

  val directiveModifiers: List<VueDirectiveModifier> get() = emptyList()

  override val searchTarget: PolySymbolSearchTarget
    get() = PolySymbolSearchTarget.create(this)

  override val presentation: TargetPresentation
    get() = TargetPresentation.builder(VueBundle.message("vue.documentation.type.directive", name))
      .icon(icon)
      .presentation()

  override fun createPointer(): Pointer<out VueDirective>

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    when (kind) {
      VUE_DIRECTIVE_ARGUMENT if (!params.expandPatterns) -> {
        listOf(VueAnySymbol(kind, "Vue directive argument"))
      }

      VUE_DIRECTIVE_MODIFIERS -> {
        directiveModifiers
          .map { VueDirectiveModifierWithProximity.create(it, this[PROP_VUE_PROXIMITY]) }
          .ifEmpty { listOf(VueAnySymbol(kind, "Vue directive modifier")) }
      }

      else -> emptyList()
    }

  override fun getModificationCount(): Long = -1
}
