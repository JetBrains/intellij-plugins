// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.scope

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.query.PolySymbolCodeCompletionQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.query.polySymbolScopeCached
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENT_NAMESPACES
import org.jetbrains.astro.polySymbols.symbols.AstroNamespacedComponent

fun astroNamespacedComponentsScope(scope: PsiElement): PolySymbolScope =
  polySymbolScopeCached(scope) {
    provides(UI_FRAMEWORK_COMPONENT_NAMESPACES)
    initialize {
      JSStubBasedPsiTreeUtil.processDeclarationsInScope(
        element,
        { element, _ ->
          val name = (element as? JSPsiNamedElementBase)
            ?.let { if (it is ES6ImportSpecifier) it.declaredName else it.name }
          if (name?.getOrNull(0)?.isUpperCase() == true) {
            add(AstroNamespacedComponent(name, element))
          }
          true
        },
        false
      )
      cacheDependencies(PsiModificationTracker.MODIFICATION_COUNT)
    }
    filterCodeCompletions { _, _ ->
      emptyList()
    }
  }