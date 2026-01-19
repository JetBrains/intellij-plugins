// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.navigation.JSDeclarationEvaluator
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
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.web.VUE_MODEL
import org.jetbrains.vuejs.web.VueComponentSourceNavigationTarget
import org.jetbrains.vuejs.web.symbols.VueModelSymbol

interface VueRegularComponent : VueComponent, VueContainer {

  override val typeParameters: List<TypeScriptTypeParameter>

  val nameElement: PsiElement?

  override val source: PsiElement?
    get() = nameElement ?: super.source

  // Use actual item source field for navigation
  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
    val source = componentSource ?: return emptyList()
    val adjustedSources = JSDeclarationEvaluator.adjustDeclaration(source, null) ?: source
    return listOf(VueComponentSourceNavigationTarget(adjustedSources))
  }

  override fun getSymbols(kind: PolySymbolKind, params: PolySymbolListSymbolsQueryParams, stack: PolySymbolQueryStack): List<PolySymbol> =
    when (kind) {
      HTML_SLOTS -> slots
      JS_EVENTS -> emits
      VUE_MODEL -> {
        collectModelDirectiveProperties()
          .takeIf { it.prop != null || it.event != null }
          ?.let { listOf(VueModelSymbol(it)) }
        ?: emptyList()
      }
      else -> super.getSymbols(kind, params, stack)
    }

  override fun createPointer(): Pointer<out VueRegularComponent>
}
