// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.navigation.JSDeclarationEvaluator
import com.intellij.lang.javascript.psi.JSPsiReferenceElement
import com.intellij.navigation.SymbolNavigationService
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement

interface ComponentPolySymbol : PsiSourcedPolySymbol, AstroSymbol {
  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
    val target = computeNavigationElement(project) ?: return emptyList()
    return listOf(SymbolNavigationService.getInstance().psiElementNavigationTarget(target))
  }

  fun computeNavigationElement(project: Project): PsiElement? = when (val s = source) {
    is ES6ImportedBinding -> JSDeclarationEvaluator.GO_TO_DECLARATION.getDeclarations(s)?.singleOrNull()
    is JSPsiReferenceElement -> JSDeclarationEvaluator.GO_TO_DECLARATION.getDeclarations(s)?.singleOrNull()
    else -> s
  }
}
