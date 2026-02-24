// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.html.HtmlAttributeValueProperty
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue

interface VueTemplateSymbol : VueSymbol {

  @PolySymbol.Property(HtmlAttributeValueProperty::class)
  val attributeValue: PolySymbolHtmlAttributeValue? get() = null

}
