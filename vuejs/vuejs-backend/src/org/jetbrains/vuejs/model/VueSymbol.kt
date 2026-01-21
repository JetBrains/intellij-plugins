// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSType
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.polySymbols.framework.FrameworkId
import com.intellij.polySymbols.html.HtmlFrameworkSymbol
import com.intellij.polySymbols.html.PROP_HTML_ATTRIBUTE_VALUE
import com.intellij.polySymbols.html.PolySymbolHtmlAttributeValue
import com.intellij.polySymbols.js.JsSymbolSymbolKind
import com.intellij.polySymbols.js.PROP_JS_SYMBOL_KIND
import com.intellij.polySymbols.js.types.PROP_JS_TYPE
import com.intellij.polySymbols.js.types.TypeScriptSymbolTypeSupport
import com.intellij.polySymbols.utils.PolySymbolTypeSupport
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.codeInsight.getLibraryNameForDocumentationOf
import org.jetbrains.vuejs.web.VueFramework
import javax.swing.Icon

interface VueSymbol : HtmlFrameworkSymbol, VueElement {

  val type: JSType? get() = null

  override val framework: FrameworkId
    get() = VueFramework.ID

  override val icon: Icon?
    get() = VuejsIcons.Vue

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PolySymbol.PROP_DOC_HIDE_ICON -> property.tryCast(true)
      PROP_JS_TYPE -> property.tryCast(type)
      PROP_JS_SYMBOL_KIND -> property.tryCast(JsSymbolSymbolKind.Property)
      PolySymbolTypeSupport.PROP_TYPE_SUPPORT -> property.tryCast(TypeScriptSymbolTypeSupport.default)
      else -> super.get(property)
    }

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget? =
    PolySymbolDocumentationTarget.create(this, location) { symbol, _ ->
      library = getLibraryNameForDocumentationOf(symbol.psiContext)
    }
}