// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.PolySymbol
import com.intellij.webSymbols.PolySymbolQualifiedKind
import com.intellij.webSymbols.PolySymbolQualifiedName
import com.intellij.webSymbols.PolySymbolsScope
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.VUE_DIRECTIVES
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_ARGUMENT
import org.jetbrains.vuejs.web.VUE_DIRECTIVE_MODIFIERS
import org.jetbrains.vuejs.web.asWebSymbolPriority

open class VueDirectiveSymbol(
  name: String,
  directive: VueDirective,
  private val vueProximity: VueModelVisitor.Proximity,
) : VueScopeElementSymbol<VueDirective>(fromAsset(name), directive) {

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = VUE_DIRECTIVES

  override val priority: PolySymbol.Priority
    get() = vueProximity.asWebSymbolPriority()

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: WebSymbolsNameMatchQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    if (qualifiedName.matches(VUE_DIRECTIVE_ARGUMENT, VUE_DIRECTIVE_MODIFIERS)) {
      listOf(VueAnySymbol(this.origin, qualifiedName.qualifiedKind, qualifiedName.name))
    }
    else emptyList()

  override fun getSymbols(
    qualifiedKind: PolySymbolQualifiedKind,
    params: WebSymbolsListSymbolsQueryParams,
    scope: Stack<PolySymbolsScope>,
  ): List<PolySymbol> =
    if (qualifiedKind == VUE_DIRECTIVE_ARGUMENT) {
      listOf(VueAnySymbol(this.origin, qualifiedKind, "Vue directive argument"))
    }
    else emptyList()

  override fun createPointer(): Pointer<VueDirectiveSymbol> {
    val component = item.createPointer()
    val name = this.name
    val vueProximity = this.vueProximity
    return Pointer {
      component.dereference()?.let { VueDirectiveSymbol(name, it, vueProximity) }
    }
  }
}