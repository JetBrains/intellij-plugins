// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.polySymbols.html.PROP_HTML_ATTRIBUTE_VALUE
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.js.types.PROP_JS_TYPE
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.codeInsight.documentation.VueItemDocumentation
import org.jetbrains.vuejs.web.vueEmptyOrigin

interface VueDocumentedItemSymbolMixin : PolySymbol, VueDocumentedItem {

  val type: JSType? get() = null

  val attributeValue: PolySymbolHtmlAttributeValue? get() = null

  override val origin: PolySymbolOrigin
    get() = vueEmptyOrigin

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget? =
    PolySymbolDocumentationTarget.create(this, location) { symbol, location ->
      description = symbol.description
    }

  override val presentation: TargetPresentation
    get() = TargetPresentation.builder(VueBundle.message("vue.symbol.presentation", VueItemDocumentation.typeOf(this), name))
      .icon(icon)
      .presentation()

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_JS_TYPE -> property.tryCast(type)
      PROP_HTML_ATTRIBUTE_VALUE -> property.tryCast(attributeValue)
      else -> super.get(property)
    }

  abstract override fun createPointer(): Pointer<out VueDocumentedItemSymbolMixin>

}