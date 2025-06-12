// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolService
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.query.PolySymbolsListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolsNameMatchQueryParams
import com.intellij.polySymbols.query.PolySymbolsScope
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.source.VueScriptSetupLocalDirective
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_ARGUMENT
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_MODIFIERS
import org.jetbrains.vuejs.web.VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES
import org.jetbrains.vuejs.web.asPolySymbolPriority

class VueScriptSetupLocalDirectiveSymbol(
  directive: VueScriptSetupLocalDirective,
  private val vueProximity: VueModelVisitor.Proximity,
) : VueScopeElementSymbol<VueScriptSetupLocalDirective>(directive.defaultName, directive) {

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES

  override val priority: PolySymbol.Priority
    get() = vueProximity.asPolySymbolPriority()

  override fun isEquivalentTo(symbol: Symbol): Boolean {
    val target = PsiSymbolService.getInstance().extractElementFromSymbol(symbol)
    if (target != null && target.manager.areElementsEquivalent(target, rawSource))
      return true
    return super.isEquivalentTo(symbol)
  }

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolsNameMatchQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    if (qualifiedName.matches(VUE_DIRECTIVE_ARGUMENT, VUE_DIRECTIVE_MODIFIERS)) {
      listOf(VueAnySymbol(this.origin, qualifiedName.qualifiedKind, qualifiedName.name))
    }
    else emptyList()

  override fun getSymbols(
    qualifiedKind: PolySymbolQualifiedKind,
    params: PolySymbolsListSymbolsQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    if (qualifiedKind == VUE_DIRECTIVE_ARGUMENT
        && !params.expandPatterns) {
      listOf(VueAnySymbol(this.origin, qualifiedKind, "Vue directive argument"))
    }
    else emptyList()

  override fun createPointer(): Pointer<VueScriptSetupLocalDirectiveSymbol> {
    val directive = item.createPointer()
    val vueProximity = this.vueProximity
    return Pointer {
      directive.dereference()?.let { VueScriptSetupLocalDirectiveSymbol(it, vueProximity) }
    }
  }

}