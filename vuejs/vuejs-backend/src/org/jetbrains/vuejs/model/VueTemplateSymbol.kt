// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.html.PROP_HTML_ATTRIBUTE_VALUE
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue

interface VueTemplateSymbol : VueSymbol {

  val attributeValue: PolySymbolHtmlAttributeValue? get() = null

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_HTML_ATTRIBUTE_VALUE -> property.tryCast(attributeValue)
      else -> super.get(property)
    }
}
