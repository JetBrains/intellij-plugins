// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.html.HTML_SLOTS
import com.intellij.polySymbols.js.JS_EVENTS
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.psi.PsiElement

interface VueComponent : VueContainer, VueMixin {

  val elementToImport: PsiElement?

  val mode: VueMode
    get() = VueMode.CLASSIC

  val typeParameters: List<TypeScriptTypeParameter>

  fun getNavigationTargets(project: Project): Collection<NavigationTarget>

  override fun getSymbols(
    kind: PolySymbolKind,
    params: PolySymbolListSymbolsQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    when (kind) {
      HTML_SLOTS -> slots
      JS_EVENTS -> emits
      else -> super<VueContainer>.getSymbols(kind, params, stack)
    }

  override fun getModificationCount(): Long = -1

  override fun createPointer(): Pointer<out VueComponent>
}