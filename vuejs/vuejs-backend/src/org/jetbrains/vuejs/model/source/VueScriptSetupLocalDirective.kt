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
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.typed.VueTypedDirectives.getDirectiveModifiers
import org.jetbrains.vuejs.web.VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES

class VueScriptSetupLocalDirective(
  override val name: String,
  private val rawSource: JSPsiNamedElementBase,
  private val mode: VueMode,
  override val vueProximity: VueModelVisitor.Proximity? = null,
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

  override fun withVueProximity(proximity: VueModelVisitor.Proximity): VueDirective =
    VueScriptSetupLocalDirective(name, rawSource, mode, proximity)

  override fun createPointer(): Pointer<VueScriptSetupLocalDirective> {
    val name = name
    val source = this.rawSource.createSmartPointer()
    val mode = this.mode
    val vueProximity = this.vueProximity
    return Pointer {
      val newSource = source.dereference() ?: return@Pointer null
      VueScriptSetupLocalDirective(name, newSource, mode, vueProximity)
    }
  }

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    super<PsiSourcedPolySymbol>.isEquivalentTo(symbol)
    || PsiSymbolService.getInstance().extractElementFromSymbol(symbol)
      ?.let { it.manager.areElementsEquivalent(it, rawSource) } == true
    || symbol is VueScriptSetupLocalDirective
    && symbol.source == source
    && symbol.name == name

  override fun equals(other: Any?): Boolean =
    other === this
    || other is VueScriptSetupLocalDirective
    && other.name == name
    && other.rawSource == rawSource
    && other.vueProximity == vueProximity

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + rawSource.hashCode()
    result = 31 * result + (vueProximity?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "VueScriptSetupLocalDirective($name)"
  }
}
