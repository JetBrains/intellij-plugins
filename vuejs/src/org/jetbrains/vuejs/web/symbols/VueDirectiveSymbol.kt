// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolsContainer
import com.intellij.webSymbols.WebSymbolsNameMatchQueryParams
import com.intellij.model.Pointer
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider
import org.jetbrains.vuejs.web.asWebSymbolPriority

class VueDirectiveSymbol(matchedName: String, directive: VueDirective, private val vueProximity: VueModelVisitor.Proximity) :
  VueScopeElementSymbol<VueDirective>(fromAsset(matchedName), directive) {

  override val kind: SymbolKind
    get() = VueWebSymbolsAdditionalContextProvider.KIND_VUE_DIRECTIVES

  override val name: String
    get() = matchedName

  override val priority: WebSymbol.Priority
    get() = vueProximity.asWebSymbolPriority()

  override fun getSymbols(namespace: WebSymbolsContainer.Namespace?,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
    if ((namespace == null || namespace == WebSymbolsContainer.Namespace.HTML)
        && (kind == VueWebSymbolsAdditionalContextProvider.KIND_VUE_DIRECTIVE_ARGUMENT || (name != null && kind == VueWebSymbolsAdditionalContextProvider.KIND_VUE_DIRECTIVE_MODIFIERS))) {
      listOf(VueAnySymbol(this.origin, WebSymbolsContainer.Namespace.HTML, kind, name ?: "Vue directive argument"))
    }
    else emptyList()

  override fun createPointer(): Pointer<VueDirectiveSymbol> {
    val component = item.createPointer()
    val matchedName = this.matchedName
    val vueProximity = this.vueProximity
    return Pointer {
      component.dereference()?.let { VueDirectiveSymbol(matchedName, it, vueProximity) }
    }
  }
}