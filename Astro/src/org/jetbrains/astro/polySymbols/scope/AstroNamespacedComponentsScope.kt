// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.scope

import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.query.PolySymbolCodeCompletionQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENT_NAMESPACES
import org.jetbrains.astro.polySymbols.symbols.AstroNamespacedComponent

class AstroNamespacedComponentsScope(private val scope: PsiElement)
  : PolySymbolScopeWithCache<PsiElement, Unit>(AstroFramework.ID, scope.project, scope, Unit) {
  override fun createPointer(): Pointer<out PolySymbolScopeWithCache<PsiElement, Unit>> {
    val modulePtr = dataHolder.createSmartPointer()
    return Pointer {
      modulePtr.dereference()?.let { AstroNamespacedComponentsScope(it) }
    }
  }

  override fun provides(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    qualifiedKind == UI_FRAMEWORK_COMPONENT_NAMESPACES

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    JSStubBasedPsiTreeUtil.processDeclarationsInScope(
      scope,
      { element, _ ->
        val name = (element as? JSPsiNamedElementBase)
          ?.let { if (it is ES6ImportSpecifier) it.declaredName else it.name }
        if (name?.getOrNull(0)?.isUpperCase() == true) {
          consumer(AstroNamespacedComponent(name, element))
        }
        true
      },
      false
    )

    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  override fun getCodeCompletions(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolCodeCompletionQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbolCodeCompletionItem> {
    return emptyList()
  }
}
