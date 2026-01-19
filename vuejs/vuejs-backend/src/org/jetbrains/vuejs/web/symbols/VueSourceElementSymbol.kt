// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.typeOf
import org.jetbrains.vuejs.model.VueSourceElement

abstract class VueSourceElementSymbol<T : VueSourceElement>(
  override val name: String,
  internal val item: T,
) : PolySymbolScope, PsiSourcedPolySymbol, VueSymbol {

  override fun getModificationCount(): Long =
    source?.project?.let { PsiModificationTracker.getInstance(it).modificationCount } ?: 0

  override val source: PsiElement?
    get() = item.source

  val rawSource: PsiElement?
    get() = item.rawSource

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget? =
    PolySymbolDocumentationTarget.create(this, location, VueSymbolDocumentationProvider)

  override val presentation: TargetPresentation
    get() = TargetPresentation.builder(VueBundle.message("vue.symbol.presentation", typeOf(item), name))
      .icon(icon)
      .presentation()

  abstract override fun createPointer(): Pointer<out VueSourceElementSymbol<T>>

  override fun equals(other: Any?): Boolean =
    other === this ||
    (other is VueSourceElementSymbol<*>
     && other.javaClass == this.javaClass
     && name == other.name
     && item == other.item)

  override fun hashCode(): Int =
    31 * name.hashCode() + item.hashCode()

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    if (symbol is VueSourceElementSymbol<*>)
      symbol === this || (symbol.javaClass == this.javaClass
                          && symbol.name == name)
    //&& VueDelegatedContainer.unwrap(item) == VueDelegatedContainer.unwrap(symbol.item))
    else
      super<PsiSourcedPolySymbol>.isEquivalentTo(symbol)
}