// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.documentation.PolySymbolWithDocumentation
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.codeInsight.documentation.VueItemDocumentation

abstract class VueDocumentedItemSymbol<T : VueDocumentedItem>(
  override val name: String,
  protected val item: T,
) : VuePolySymbolBase(), PsiSourcedPolySymbol, PolySymbolWithDocumentation {

  override val source: PsiElement?
    get() = item.source

  val rawSource: PsiElement?
    get() = item.rawSource

  override val description: String?
    get() = item.description

  override val presentation: TargetPresentation
    get() = TargetPresentation.builder(VueBundle.message("vue.symbol.presentation", VueItemDocumentation.typeOf(item), name))
      .icon(icon)
      .presentation()

  abstract override fun createPointer(): Pointer<out VueDocumentedItemSymbol<T>>

  override fun equals(other: Any?): Boolean =
    other === this ||
    (other is VueDocumentedItemSymbol<*>
     && other.javaClass == this.javaClass
     && name == other.name
     && item == other.item)

  override fun hashCode(): Int =
    31 * name.hashCode() + item.hashCode()

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    if (symbol is VueDocumentedItemSymbol<*>)
      symbol === this || (symbol.javaClass == this.javaClass
                          && symbol.name == name)
    //&& VueDelegatedContainer.unwrap(item) == VueDelegatedContainer.unwrap(symbol.item))
    else
      super<PsiSourcedPolySymbol>.isEquivalentTo(symbol)
}