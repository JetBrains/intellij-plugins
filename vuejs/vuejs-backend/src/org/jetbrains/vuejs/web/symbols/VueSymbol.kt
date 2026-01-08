// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSType
import com.intellij.polySymbols.PolySymbol.Companion.PROP_DOC_HIDE_ICON
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.framework.FrameworkId
import com.intellij.polySymbols.html.HtmlFrameworkSymbol
import com.intellij.polySymbols.html.PROP_HTML_ATTRIBUTE_VALUE
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.js.types.PROP_JS_TYPE
import com.intellij.polySymbols.js.types.TypeScriptSymbolTypeSupport
import com.intellij.polySymbols.utils.PolySymbolTypeSupport.Companion.PROP_TYPE_SUPPORT
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.web.VueFramework
import javax.swing.Icon

interface VueSymbol : HtmlFrameworkSymbol {

  val type: JSType? get() = null
  val attributeValue: PolySymbolHtmlAttributeValue? get() = null

  override val origin: PolySymbolOrigin
    get() = PolySymbolOrigin.empty()

  override val framework: FrameworkId
    get() = VueFramework.ID

  override val icon: Icon?
    get() = VuejsIcons.Vue

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_DOC_HIDE_ICON -> property.tryCast(true)
      PROP_JS_TYPE -> property.tryCast(type)
      PROP_HTML_ATTRIBUTE_VALUE -> property.tryCast(attributeValue)
      PROP_TYPE_SUPPORT -> property.tryCast(TypeScriptSymbolTypeSupport.default)
      else -> super.get(property)
    }
}