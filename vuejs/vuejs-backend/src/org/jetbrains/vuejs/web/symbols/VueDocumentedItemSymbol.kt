// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.polySymbols.js.types.PROP_JS_TYPE
import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.polySymbols.html.PROP_HTML_ATTRIBUTE_VALUE
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.codeInsight.documentation.VueItemDocumentation

abstract class VueDocumentedItemSymbol<T : VueDocumentedItem>(
  override val name: String,
  protected val item: T,
) : PolySymbolScope, PsiSourcedPolySymbol {

  open val type: JSType? get() = null

  open val attributeValue: PolySymbolHtmlAttributeValue? get() = null

  override fun getModificationCount(): Long =
    source?.project?.let { PsiModificationTracker.getInstance(it).modificationCount} ?: 0

  override val source: PsiElement?
    get() = item.source

  val rawSource: PsiElement?
    get() = item.rawSource

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget? =
    PolySymbolDocumentationTarget.create(this, location) {symbol, location ->
      description = symbol.item.description
    }

  override val presentation: TargetPresentation
    get() = TargetPresentation.builder(VueBundle.message("vue.symbol.presentation", VueItemDocumentation.typeOf(item), name))
      .icon(icon)
      .presentation()

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_JS_TYPE -> property.tryCast(type)
      PROP_HTML_ATTRIBUTE_VALUE -> property.tryCast(attributeValue)
      else -> super.get(property)
    }

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