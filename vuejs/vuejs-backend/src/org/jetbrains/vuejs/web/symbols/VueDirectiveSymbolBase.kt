// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolService
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_ARGUMENT
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_MODIFIERS
import org.jetbrains.vuejs.web.asPolySymbolPriority

abstract class VueDirectiveSymbolBase<T : VueDirective>(
  name: String,
  directive: T,
) : VueScopeElementSymbol<T>(
  name = name,
  item = directive,
) {

  protected abstract val vueProximity: VueModelVisitor.Proximity

  final override val priority: PolySymbol.Priority
    get() = vueProximity.asPolySymbolPriority()

  final override fun isEquivalentTo(symbol: Symbol): Boolean {
    val target = PsiSymbolService.getInstance().extractElementFromSymbol(symbol)
    if (target != null && target.manager.areElementsEquivalent(target, rawSource))
      return true
    return super.isEquivalentTo(symbol)
  }

  final override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    when (kind) {
      VUE_DIRECTIVE_ARGUMENT if (!params.expandPatterns) -> {
        listOf(VueAnySymbol(kind, "Vue directive argument"))
      }

      VUE_DIRECTIVE_MODIFIERS -> {
        item.modifiers
          .map { it.withProximity(vueProximity) }
          .ifEmpty { listOf(VueAnySymbol(kind, "Vue directive modifier")) }
      }

      else -> emptyList()
    }
}
