// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.XmlAttributeValueQuotationHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.types.JSPrimitiveLiteralType
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSUnionType
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.html.attributes.HtmlAttributeSymbolDescriptor
import com.intellij.polySymbols.html.attributes.HtmlAttributeSymbolInfo
import com.intellij.polySymbols.js.jsType
import com.intellij.polySymbols.js.symbols.JSPropertySymbol
import com.intellij.polySymbols.js.types.TypeScriptSymbolTypeSupport
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ThreeState
import org.jetbrains.astro.codeInsight.attributes.AstroAttributeDescriptor
import javax.swing.Icon

val astroFramework: WebFramework
  get() = WebFramework.get(AstroFramework.ID)

class AstroFramework : WebFramework() {
  companion object {
    const val ID: String = "astro"
  }

  override fun isOwnTemplateLanguage(language: Language): Boolean = false

  override fun getFileType(kind: SourceFileKind, context: VirtualFile, project: Project): WebFrameworkHtmlFileType? = null

  override val displayName: String = "Astro"

  override val icon: Icon
    get() = AstroIcons.Astro

  override fun createHtmlAttributeDescriptor(info: HtmlAttributeSymbolInfo, tag: XmlTag?): HtmlAttributeSymbolDescriptor =
    AstroAttributeDescriptor(info, tag)

  override fun createAttributeInsertHandler(
    parameters: CompletionParameters,
    item: PolySymbolCodeCompletionItem,
    info: HtmlAttributeSymbolInfo,
  ): InsertHandler<LookupElement> {
    return if (shouldUseBracesForAttributeType(item.symbol)) {
      XmlAttributeValueQuotationHandler.BRACES
    }
    else {
      XmlAttributeValueQuotationHandler.QUOTES
    }
  }

  override fun shouldInsertAttributeValue(
    parameters: CompletionParameters,
    item: PolySymbolCodeCompletionItem,
    info: HtmlAttributeSymbolInfo,
  ): Boolean {
    if (isBooleanType(item.symbol)) return false
    if (shouldUseBracesForAttributeType(item.symbol)) return true
    return info.acceptsValue && !info.acceptsNoValue
  }

  private fun isBooleanType(symbol: PolySymbol?): Boolean {
    val jsType = extractJSType(symbol) ?: return false
    return TypeScriptSymbolTypeSupport.isBoolean(jsType) == ThreeState.YES
  }

  private fun shouldUseBracesForAttributeType(symbol: PolySymbol?): Boolean {
    val jsType = extractJSType(symbol) ?: return false
    val actualType = JSTypeUtils.removeNullableComponents(jsType.substitute())

    if (actualType is JSStringType || actualType is JSStringLiteralTypeImpl) return false
    if (actualType is JSUnionType && actualType.types.all { it is JSPrimitiveLiteralType<*> && it.literal is String }) {
      return false
    }

    return true
  }

  private fun extractJSType(symbol: PolySymbol?): JSType? {
    if (symbol == null) return null
    return (symbol as? JSPropertySymbol)?.type ?: symbol.jsType
  }
}
