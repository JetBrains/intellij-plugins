// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolService
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueDirectiveModifier
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueMode
import org.jetbrains.vuejs.model.typed.VueTypedDirectives.getDirectiveModifiers
import org.jetbrains.vuejs.web.VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES

data class VueScriptSetupLocalDirective(
  override val name: String,
  private val rawSource: JSPsiNamedElementBase,
  private val mode: VueMode,
) : VueDirective, PsiSourcedPolySymbol {

  override val parents: List<VueEntitiesContainer> = emptyList()

  override val kind: PolySymbolKind
    get() = VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES

  override val source: PsiElement by lazy(LazyThreadSafetyMode.PUBLICATION) {
    rawSource.resolveIfImportSpecifier()
  }

  override val directiveModifiers: List<VueDirectiveModifier>
    get() = CachedValuesManager.getCachedValue(source) {
      CachedValueProvider.Result.create(
        getDirectiveModifiers(source, mode),
        PsiModificationTracker.MODIFICATION_COUNT,
      )
    }

  override fun createPointer(): Pointer<VueScriptSetupLocalDirective> {
    val name = name
    val source = this.rawSource.createSmartPointer()
    val mode = this.mode
    return Pointer {
      val newSource = source.dereference() ?: return@Pointer null
      VueScriptSetupLocalDirective(name, newSource, mode)
    }
  }

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    super<PsiSourcedPolySymbol>.isEquivalentTo(symbol)
    || PsiSymbolService.getInstance().extractElementFromSymbol(symbol)
      ?.let { it.manager.areElementsEquivalent(it, rawSource) } == true

}
