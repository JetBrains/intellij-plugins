// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.refactoring.PolySymbolRenameTarget
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueDirectiveModifier
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.PROP_VUE_PROXIMITY
import org.jetbrains.vuejs.web.asPolySymbolPriority

internal open class VueDirectiveModifierWithProximity private constructor(
  val delegate: VueDirectiveModifier,
  val vueProximity: VueModelVisitor.Proximity,
) : VueDirectiveModifier {

  override val name: @NlsSafe String
    get() = delegate.name

  override val priority: PolySymbol.Priority
    get() = vueProximity.asPolySymbolPriority()

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
    if (symbol is VueDirectiveModifierWithProximity)
      delegate.isEquivalentTo(symbol.delegate)
    else
      super.isEquivalentTo(symbol)
      || delegate.isEquivalentTo(symbol)

  override fun createPointer(): Pointer<out VueDirectiveModifier> {
    val delegatePtr = delegate.createPointer()
    val vueProximity = vueProximity
    return Pointer {
      delegatePtr.dereference()?.let { VueDirectiveModifierWithProximity(it, vueProximity) }
    }
  }

  companion object {
    fun create(delegate: VueDirectiveModifier, proximity: VueModelVisitor.Proximity?): VueDirectiveModifier =
      when {
        proximity == null -> delegate
        delegate is PsiSourcedPolySymbol -> VuePsiSourcedDirectiveModifierWithProximity(delegate, proximity)
        else -> VueDirectiveModifierWithProximity(delegate, proximity)
      }
  }

  private class VuePsiSourcedDirectiveModifierWithProximity(
    delegate: VueDirectiveModifier,
    proximity: VueModelVisitor.Proximity,
  ) : VueDirectiveModifierWithProximity(delegate, proximity), PsiSourcedPolySymbol {

    override val source: PsiElement?
      get() = (delegate as PsiSourcedPolySymbol).source

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      delegate.getNavigationTargets(project)

    override fun isEquivalentTo(symbol: Symbol): Boolean =
      super<PsiSourcedPolySymbol>.isEquivalentTo(symbol)
      || super<VueDirectiveModifierWithProximity>.isEquivalentTo(symbol)

    override fun createPointer(): Pointer<VuePsiSourcedDirectiveModifierWithProximity> {
      val delegatePtr = delegate.createPointer()
      val vueProximity = vueProximity
      return Pointer {
        delegatePtr.dereference()?.let { VuePsiSourcedDirectiveModifierWithProximity(it, vueProximity) }
      }
    }

  }


}
