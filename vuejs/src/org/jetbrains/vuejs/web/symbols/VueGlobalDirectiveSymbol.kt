// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolService
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.typed.VueTypedDirective
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_ARGUMENT
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_MODIFIERS
import org.jetbrains.vuejs.web.VUE_GLOBAL_DIRECTIVES
import org.jetbrains.vuejs.web.asPolySymbolPriority

class VueGlobalDirectiveSymbol(
  directive: VueTypedDirective,
  private val vueProximity: VueModelVisitor.Proximity,
) : VueScopeElementSymbol<VueTypedDirective>(directive.defaultName, directive) {

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = VUE_GLOBAL_DIRECTIVES

  override val priority: PolySymbol.Priority
    get() = vueProximity.asPolySymbolPriority()

  override fun isEquivalentTo(symbol: Symbol): Boolean {
    val target = PsiSymbolService.getInstance().extractElementFromSymbol(symbol)
    if (target != null && target.manager.areElementsEquivalent(target, rawSource))
      return true
    return super.isEquivalentTo(symbol)
  }

  override fun getSymbols(
    qualifiedKind: PolySymbolQualifiedKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    when (qualifiedKind) {
      VUE_DIRECTIVE_ARGUMENT -> {
        listOf(VueAnySymbol(origin, qualifiedKind, "Vue directive argument"))
      }

      VUE_DIRECTIVE_MODIFIERS -> {
        item.modifiers.map { modifier ->
          VueGlobalDirectiveModifierSymbol(modifier, vueProximity)
        }.ifEmpty {
          listOf(VueAnySymbol(origin, qualifiedKind, "Vue directive modifier"))
        }
      }

      else -> emptyList()
    }

  override fun createPointer(): Pointer<VueGlobalDirectiveSymbol> {
    val directive = item.createPointer()
    val vueProximity = this.vueProximity
    return Pointer {
      directive.dereference()?.let { VueGlobalDirectiveSymbol(it, vueProximity) }
    }
  }

}