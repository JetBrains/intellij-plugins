// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.model.VueDirectiveModifier
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.asPolySymbolPriority

data class VueTypedDirectiveModifier(
  override val name: String,
  override val source: PsiElement?,
  private val vueProximity: VueModelVisitor.Proximity? = null,
) : VueDirectiveModifier, PsiSourcedPolySymbol {

  override fun withProximity(proximity: VueModelVisitor.Proximity?): VueDirectiveModifier =
    VueTypedDirectiveModifier(name, source, proximity)

  override val priority: PolySymbol.Priority?
    get() = vueProximity?.asPolySymbolPriority()

  override fun createPointer(): Pointer<out VueTypedDirectiveModifier> {
    val name = this.name
    val sourcePointer = source?.createSmartPointer()
    val vueProximity = this.vueProximity
    return Pointer {
      val source = sourcePointer?.let { it.dereference() ?: return@Pointer null }
      VueTypedDirectiveModifier(name, source, vueProximity)
    }
  }

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    super<PsiSourcedPolySymbol>.isEquivalentTo(symbol)
    || symbol is VueTypedDirectiveModifier
    && symbol.name == name
    && symbol.source == source

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is VueTypedDirectiveModifier
    && other.name == name
    && other.source == source
    && other.vueProximity == vueProximity

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + source.hashCode()
    result = 31 * result + (vueProximity?.hashCode() ?: 0)
    return result
  }
}
