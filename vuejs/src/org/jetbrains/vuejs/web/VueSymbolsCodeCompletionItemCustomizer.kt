// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.javascript.webSymbols.decorateWithSymbolType
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.psi.PsiElement
import com.intellij.psi.util.contextOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.webSymbols.FrameworkId
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItemCustomizer
import org.jetbrains.vuejs.index.isScriptSetupTag
import org.jetbrains.vuejs.lang.html.isVueFileName
import org.jetbrains.vuejs.web.symbols.VueComponentSymbol

class VueSymbolsCodeCompletionItemCustomizer : WebSymbolCodeCompletionItemCustomizer {
  override fun customize(item: WebSymbolCodeCompletionItem,
                         framework: FrameworkId?,
                         qualifiedKind: WebSymbolQualifiedKind,
                         location: PsiElement): WebSymbolCodeCompletionItem? =
    if (framework == VueFramework.ID)
      when (qualifiedKind) {
        WebSymbol.HTML_ATTRIBUTES ->
          item.symbol
            ?.takeIf { it.kind == VUE_COMPONENT_PROPS.kind || it.kind == WebSymbol.KIND_JS_EVENTS }
            ?.let { item.decorateWithSymbolType(location, it) }
          ?: item
        WebSymbol.HTML_ELEMENTS ->
          item.takeIf { !shouldFilterOutLowerCaseScriptSetupIdentifier(it) }
        else -> item
      }
    else item

  private fun shouldFilterOutLowerCaseScriptSetupIdentifier(item: WebSymbolCodeCompletionItem): Boolean {
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