// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolService
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolsScope
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.source.VueCompositionApiLocalDirective
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator
import org.jetbrains.vuejs.web.asWebSymbolPriority

class VueCompositionApiLocalDirectiveSymbol(directive: VueCompositionApiLocalDirective, private val vueProximity: VueModelVisitor.Proximity) :
  VueScopeElementSymbol<VueCompositionApiLocalDirective>(directive.defaultName, directive) {

  override val kind: SymbolKind
    get() = VueWebSymbolsQueryConfigurator.KIND_VUE_COMPOSITION_API_LOCAL_DIRECTIVES

  override val priority: WebSymbol.Priority
    get() = vueProximity.asWebSymbolPriority()

  override fun isEquivalentTo(symbol: Symbol): Boolean {
    val target = PsiSymbolService.getInstance().extractElementFromSymbol(symbol)
    if (target != null && target.manager.areElementsEquivalent(target, rawSource))
      return true
    return super.isEquivalentTo(symbol)
  }

  override fun getSymbols(namespace: SymbolNamespace,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    if (namespace == WebSymbol.NAMESPACE_HTML
        && (kind == VueWebSymbolsQueryConfigurator.KIND_VUE_DIRECTIVE_ARGUMENT || (name != null && kind == VueWebSymbolsQueryConfigurator.KIND_VUE_DIRECTIVE_MODIFIERS))) {
      listOf(VueAnySymbol(this.origin, WebSymbol.NAMESPACE_HTML, kind, name ?: "Vue directive argument"))
    }
    else emptyList()

  override fun createPointer(): Pointer<VueCompositionApiLocalDirectiveSymbol> {
    val directive = item.createPointer()
    val vueProximity = this.vueProximity
    return Pointer {
      directive.dereference()?.let { VueCompositionApiLocalDirectiveSymbol(it, vueProximity) }
    }
  }

}