// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.polySymbols.js.decorateWithSymbolType
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.polySymbols.FrameworkId
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.html.HTML_ELEMENTS
import com.intellij.polySymbols.js.JS_EVENTS
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItemCustomizer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.contextOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import org.jetbrains.vuejs.index.isScriptSetupTag
import org.jetbrains.vuejs.lang.html.isVueFileName
import org.jetbrains.vuejs.web.symbols.VueComponentSymbol

class VueCodeCompletionItemCustomizer :
  PolySymbolCodeCompletionItemCustomizer {

  override fun customize(
    item: PolySymbolCodeCompletionItem,
    framework: FrameworkId?,
    qualifiedKind: PolySymbolQualifiedKind,
    location: PsiElement,
  ): PolySymbolCodeCompletionItem? =
    if (framework == VueFramework.ID)
      when (qualifiedKind) {
        HTML_ATTRIBUTES ->
          item.symbol
            ?.takeIf { it.qualifiedKind == VUE_COMPONENT_PROPS || it.qualifiedKind == JS_EVENTS }
            ?.let { item.decorateWithSymbolType(location, it) }
          ?: item
        HTML_ELEMENTS ->
          item.takeIf { !shouldFilterOutLowerCaseScriptSetupIdentifier(it) }
        else -> item
      }
    else item

  private fun shouldFilterOutLowerCaseScriptSetupIdentifier(item: PolySymbolCodeCompletionItem): Boolean {
    val source = item.symbol.asSafely<VueComponentSymbol>()?.source?.asSafely<JSPsiNamedElementBase>()
    if (source?.contextOfType<XmlTag>(false)?.isScriptSetupTag() != true)
      return false
    val originalName = if (source is ES6ImportSpecifier) source.declaredName else source.name
    return originalName?.getOrNull(0)?.isLowerCase() == true
           && (source !is ES6ImportedBinding
               || source.declaration
                 ?.fromClause?.referenceText?.let { JSStringUtil.unquoteStringLiteralValue(it) }
                 ?.let { isVueFileName(it) } != true)
  }
}