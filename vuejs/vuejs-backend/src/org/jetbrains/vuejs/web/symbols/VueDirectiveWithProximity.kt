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
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueDirectiveModifier
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.VueProximityProperty
import org.jetbrains.vuejs.web.asPolySymbolPriority

internal open class VueDirectiveWithProximity private constructor(
  val delegate: VueDirective,
  @PolySymbol.Property(VueProximityProperty::class)
  val vueProximity: VueModelVisitor.Proximity,
) : VueDirective {

  companion object {
    fun create(delegate: VueDirective, proximity: VueModelVisitor.Proximity): VueDirective =
      if (delegate is PsiSourcedPolySymbol)
        VuePsiSourcedDirectiveWithProximity(delegate, proximity)
      else
        VueDirectiveWithProximity(delegate, proximity)
  }

  override val directiveModifiers: List<VueDirectiveModifier>
    get() = delegate.directiveModifiers

  override val priority: PolySymbol.Priority
    get() = vueProximity.asPolySymbolPriority()

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    super.get(property) ?: delegate[property]

  override val searchTarget: PolySymbolSearchTarget
    get() = delegate.searchTarget

  override val renameTarget: PolySymbolRenameTarget?
    get() = delegate.renameTarget

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    delegate.getNavigationTargets(project)

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    if (symbol is VueDirectiveWithProximity)
      delegate.isEquivalentTo(symbol.delegate)
    else
      super.isEquivalentTo(symbol)
      || delegate.isEquivalentTo(symbol)

  override val kind: PolySymbolKind
    get() = delegate.kind

  override val name: @NlsSafe String
    get() = delegate.name

  override val source: PsiElement?
    get() = delegate.source

  override val parents: List<VueEntitiesContainer>
    get() = delegate.parents

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is VueDirectiveWithProximity
    && other.delegate == delegate
    && other.vueProximity == vueProximity

  override fun hashCode(): Int {
    var result = delegate.hashCode()
    result = 31 * result + vueProximity.hashCode()
    return result
  }

  override fun createPointer(): Pointer<out VueDirective> {
    val delegatePtr = delegate.createPointer()
    val vueProximity = vueProximity
    return Pointer {
      VueDirectiveWithProximity(delegatePtr.dereference() ?: return@Pointer null, vueProximity)
    }
  }

  private class VuePsiSourcedDirectiveWithProximity(
    delegate: VueDirective,
    vueProximity: VueModelVisitor.Proximity,
  ) : VueDirectiveWithProximity(delegate, vueProximity), PsiSourcedPolySymbol {

    override val source: PsiElement?
      get() = (delegate as PsiSourcedPolySymbol).source

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      delegate.getNavigationTargets(project)

    override fun isEquivalentTo(symbol: Symbol): Boolean =
      super<PsiSourcedPolySymbol>.isEquivalentTo(symbol)
      || super<VueDirectiveWithProximity>.isEquivalentTo(symbol)

    override fun createPointer(): Pointer<VuePsiSourcedDirectiveWithProximity> {
      val delegatePtr = delegate.createPointer()
      val vueProximity = vueProximity
      return Pointer {
        VuePsiSourcedDirectiveWithProximity(delegatePtr.dereference() ?: return@Pointer null, vueProximity)
      }
    }
  }
}
