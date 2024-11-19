// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.javascript.webSymbols.decorateWithJsType
import com.intellij.javascript.webSymbols.decorateWithSymbolType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.FrameworkId
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItemCustomizer
import com.intellij.webSymbols.utils.qualifiedKind
import org.angular2.Angular2Framework
import org.angular2.lang.types.BindingsTypeResolver
import org.angular2.web.scopes.OneTimeBindingsScope


private val typedKinds = setOf(WebSymbol.JS_EVENTS,
                               WebSymbol.JS_PROPERTIES,
                               NG_DIRECTIVE_ATTRIBUTES,
                               NG_DIRECTIVE_INPUTS,
                               NG_DIRECTIVE_OUTPUTS,
                               NG_DIRECTIVE_IN_OUTS)

private val selectorKinds = setOf(NG_DIRECTIVE_ELEMENT_SELECTORS,
                                  NG_DIRECTIVE_ATTRIBUTE_SELECTORS)

class Angular2CompletionItemCustomizer : WebSymbolCodeCompletionItemCustomizer {

  override fun customize(item: WebSymbolCodeCompletionItem,
                         framework: FrameworkId?,
                         qualifiedKind: WebSymbolQualifiedKind,
                         location: PsiElement): WebSymbolCodeCompletionItem =
    if (framework != Angular2Framework.ID)
      item
    else
      when (qualifiedKind) {
        WebSymbol.HTML_ATTRIBUTES, WebSymbol.HTML_ATTRIBUTE_VALUES ->
          item.symbol
            ?.let { symbol ->
              val symbolKind = symbol.qualifiedKind
              when {
                typedKinds.contains(symbolKind) -> item.decorateWithSymbolType(location, symbol)
                selectorKinds.contains(symbolKind) -> item.withPriority(WebSymbol.Priority.HIGH)

                // One time bindings and selectors require special handling
                // to not override standard attributes and elements
                symbolKind == NG_DIRECTIVE_ONE_TIME_BINDINGS ->
                  item
                    .decorateWithSymbolType(location, symbol)
                    .withPriority(
                      symbol.properties[OneTimeBindingsScope.PROP_DELEGATE_PRIORITY] as WebSymbol.Priority?
                      ?: WebSymbol.Priority.HIGH
                    )

                symbolKind == NG_DIRECTIVE_EXPORTS_AS ->
                  item.decorateWithJsType(location,
                    location.parentOfType<XmlTag>()?.let { BindingsTypeResolver.get(it).resolveDirectiveExportAsType(item.name) })
                else -> item
              }
            }
          ?: item
        else -> if (qualifiedKind.namespace == WebSymbol.NAMESPACE_JS
                    && typedKinds.contains(item.symbol?.qualifiedKind))
          item.decorateWithSymbolType(location, item.symbol)
        else
          item

      }
}