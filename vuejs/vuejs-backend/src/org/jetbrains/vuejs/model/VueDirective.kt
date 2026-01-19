// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolService
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_ARGUMENT
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_MODIFIERS
import org.jetbrains.vuejs.web.asPolySymbolPriority
import org.jetbrains.vuejs.web.symbols.VueAnySymbol
import org.jetbrains.vuejs.web.symbols.VueScopeElementSymbol

interface VueDirective : VueScopeElementSymbol {

  val directiveModifiers: List<VueDirectiveModifier> get() = emptyList()

  val vueArgument: VueDirectiveArgument? get() = null

  val vueProximity: VueModelVisitor.Proximity?

  fun withVueProximity(proximity: VueModelVisitor.Proximity?): VueDirective

  override val priority: PolySymbol.Priority?
    get() = vueProximity?.asPolySymbolPriority()

  override fun createPointer(): Pointer<out VueDirective>

  override fun isEquivalentTo(symbol: Symbol): Boolean {
    val target = PsiSymbolService.getInstance().extractElementFromSymbol(symbol)
    if (target != null && target.manager.areElementsEquivalent(target, rawSource))
      return true
    return super.isEquivalentTo(symbol)
  }

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
          .map { it.withProximity(vueProximity) }
          .ifEmpty { listOf(VueAnySymbol(kind, "Vue directive modifier")) }
      }

      else -> emptyList()
    }
}
