// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.refactoring.PolySymbolRenameTarget
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueFilter
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.PROP_VUE_PROXIMITY
import org.jetbrains.vuejs.web.asPolySymbolPriority

internal open class VueFilterWithProximity private constructor(
  val delegate: VueFilter,
  val vueProximity: VueModelVisitor.Proximity,
) : VueFilter {

  override val name: @NlsSafe String
    get() = delegate.name

  override val priority: PolySymbol.Priority
    get() = vueProximity.asPolySymbolPriority()

  override val source: PsiElement
    get() = delegate.source

  override val parents: List<VueEntitiesContainer>
    get() = delegate.parents

  override val kind: PolySymbolKind
    get() = delegate.kind

  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_VUE_PROXIMITY -> vueProximity as T
      else -> delegate[property]
    }

  override val searchTarget: PolySymbolSearchTarget
    get() = delegate.searchTarget

  override val renameTarget: PolySymbolRenameTarget?
    get() = delegate.renameTarget

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    delegate.getNavigationTargets(project)

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    if (symbol is VueFilterWithProximity)
      delegate.isEquivalentTo(symbol.delegate)
    else
      super.isEquivalentTo(symbol)
      || delegate.isEquivalentTo(symbol)

  override fun createPointer(): Pointer<out VueFilterWithProximity> {
    val delegatePtr = delegate.createPointer()
    val vueProximity = vueProximity
    return Pointer {
      delegatePtr.dereference()?.let { VueFilterWithProximity(it, vueProximity) }
    }
  }

  companion object {
    fun create(delegate: VueFilter, proximity: VueModelVisitor.Proximity?): VueFilter =
      when {
        proximity == null -> delegate
        delegate is PsiSourcedPolySymbol -> VuePsiSourcedFilterWithProximity(delegate, proximity)
        else -> VueFilterWithProximity(delegate, proximity)
      }
  }

  private class VuePsiSourcedFilterWithProximity(
    delegate: VueFilter,
    proximity: VueModelVisitor.Proximity,
  ) : VueFilterWithProximity(delegate, proximity), PsiSourcedPolySymbol {

    override val source: PsiElement
      get() = delegate.source

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      delegate.getNavigationTargets(project)

    override fun isEquivalentTo(symbol: Symbol): Boolean =
      super<PsiSourcedPolySymbol>.isEquivalentTo(symbol)
      || super<VueFilterWithProximity>.isEquivalentTo(symbol)

    override fun createPointer(): Pointer<VuePsiSourcedFilterWithProximity> {
      val delegatePtr = delegate.createPointer()
      val vueProximity = vueProximity
      return Pointer {
        delegatePtr.dereference()?.let { VuePsiSourcedFilterWithProximity(it, vueProximity) }
      }
    }

  }


}
