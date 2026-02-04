// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.declarations.PolySymbolDeclaration
import com.intellij.polySymbols.refactoring.PolySymbolRenameTarget
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PolySymbolDeclaredInPsi
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueDelegatedComponent
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueNamedComponent
import org.jetbrains.vuejs.web.PROP_VUE_PROXIMITY
import org.jetbrains.vuejs.web.asPolySymbolPriority

internal open class VueComponentWithProximity private constructor(
  override val delegate: VueNamedComponent,
  val vueProximity: VueModelVisitor.Proximity,
) : VueDelegatedComponent<VueNamedComponent>() {

  companion object {
    fun create(delegate: VueNamedComponent, proximity: VueModelVisitor.Proximity): VueNamedComponent =
      when (delegate) {
        is PsiSourcedPolySymbol -> VuePsiSourcedComponentWithProximity(delegate, proximity)
        is PolySymbolDeclaredInPsi -> VueComponentDeclaredInPsiWithProximity(delegate, proximity)
        else -> VueComponentWithProximity(delegate, proximity)
      }
  }

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
    if (symbol is VueComponentWithProximity)
      delegate.isEquivalentTo(symbol.delegate)
    else
      super.isEquivalentTo(symbol)
      || delegate.isEquivalentTo(symbol)

  override fun createPointer(): Pointer<out VueDelegatedComponent<VueNamedComponent>> {
    val delegatePtr = delegate.createPointer()
    val vueProximity = vueProximity
    return Pointer {
      VueComponentWithProximity(delegatePtr.dereference() ?: return@Pointer null, vueProximity)
    }
  }

  private class VueComponentDeclaredInPsiWithProximity(
    delegate: VueNamedComponent,
    vueProximity: VueModelVisitor.Proximity,
  ) : VueComponentWithProximity(delegate, vueProximity), PolySymbolDeclaredInPsi {

    override val sourceElement: PsiElement?
      get() = (delegate as PolySymbolDeclaredInPsi).sourceElement

    override val textRangeInSourceElement: TextRange?
      get() = (delegate as PolySymbolDeclaredInPsi).textRangeInSourceElement

    override val declaration: PolySymbolDeclaration?
      get() = (delegate as PolySymbolDeclaredInPsi).declaration

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      delegate.getNavigationTargets(project)

    override fun isEquivalentTo(symbol: Symbol): Boolean =
      super<PolySymbolDeclaredInPsi>.isEquivalentTo(symbol)
      || super<VueComponentWithProximity>.isEquivalentTo(symbol)

    override fun createPointer(): Pointer<VueComponentDeclaredInPsiWithProximity> {
      val delegatePtr = delegate.createPointer()
      val vueProximity = vueProximity
      return Pointer {
        VueComponentDeclaredInPsiWithProximity(delegatePtr.dereference() ?: return@Pointer null, vueProximity)
      }
    }
  }

  private class VuePsiSourcedComponentWithProximity(
    delegate: VueNamedComponent,
    vueProximity: VueModelVisitor.Proximity,
  ) : VueComponentWithProximity(delegate, vueProximity), PsiSourcedPolySymbol {

    override val source: PsiElement?
      get() = delegate.source

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
      delegate.getNavigationTargets(project)

    override fun isEquivalentTo(symbol: Symbol): Boolean =
      super<PsiSourcedPolySymbol>.isEquivalentTo(symbol)
      || super<VueComponentWithProximity>.isEquivalentTo(symbol)

    override fun createPointer(): Pointer<VuePsiSourcedComponentWithProximity> {
      val delegatePtr = delegate.createPointer()
      val vueProximity = vueProximity
      return Pointer {
        VuePsiSourcedComponentWithProximity(delegatePtr.dereference() ?: return@Pointer null, vueProximity)
      }
    }

  }

}
