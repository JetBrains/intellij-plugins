// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.web.symbols.PsiSourcedWebSymbol
import com.intellij.model.Symbol
import com.intellij.model.presentation.SymbolPresentation
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.codeInsight.documentation.VueItemDocumentation
import java.util.*

abstract class VueDocumentedItemSymbol<T : VueDocumentedItem>(
  override val matchedName: String, protected val item: T) : VueWebSymbolBase(), PsiSourcedWebSymbol {

  override val source: PsiElement?
    get() = item.source

  val rawSource: PsiElement?
    get() = item.rawSource

  override val description: String?
    get() = item.description

  override fun getSymbolPresentation(): SymbolPresentation {
    val description = VueBundle.message("vue.symbol.presentation", VueItemDocumentation.typeOf(item), name)
    return SymbolPresentation.create(icon, name, description, description)
  }

  override fun equals(other: Any?): Boolean =
    other === this ||
    (other is VueDocumentedItemSymbol<*>
     && other.javaClass == this.javaClass
     && matchedName == other.matchedName
     && item == other.item)

  override fun hashCode(): Int = Objects.hash(matchedName, item)

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    if (symbol is VueDocumentedItemSymbol<*>)
      symbol === this || (symbol.javaClass == this.javaClass
                          && symbol.matchedName == matchedName)
    //&& VueDelegatedContainer.unwrap(item) == VueDelegatedContainer.unwrap(symbol.item))
    else
      super.isEquivalentTo(symbol)
}