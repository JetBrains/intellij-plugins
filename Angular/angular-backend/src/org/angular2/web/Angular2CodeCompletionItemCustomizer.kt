// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItemCustomizer
import com.intellij.polySymbols.context.PolyContext
import com.intellij.polySymbols.framework.framework
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.html.HTML_ATTRIBUTE_VALUES
import com.intellij.polySymbols.js.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import org.angular2.Angular2Framework
import org.angular2.lang.types.BindingsTypeResolver
import org.angular2.web.scopes.OneTimeBindingsScope


private val typedKinds = setOf(JS_EVENTS,
                               JS_PROPERTIES,
                               NG_DIRECTIVE_ATTRIBUTES,
                               NG_DIRECTIVE_INPUTS,
                               NG_DIRECTIVE_OUTPUTS,
                               NG_DIRECTIVE_IN_OUTS,
                               NG_CUSTOM_PROPERTY)

private val selectorKinds = setOf(NG_DIRECTIVE_ELEMENT_SELECTORS,
                                  NG_DIRECTIVE_ATTRIBUTE_SELECTORS)

class Angular2CodeCompletionItemCustomizer : PolySymbolCodeCompletionItemCustomizer {

  override fun customize(
    item: PolySymbolCodeCompletionItem,
    context: PolyContext,
    kind: PolySymbolKind,
    location: PsiElement,
  ): PolySymbolCodeCompletionItem =
    if (context.framework != Angular2Framework.ID)
      item
    else
      when (kind) {
        HTML_ATTRIBUTES, HTML_ATTRIBUTE_VALUES ->
          item.symbol
            ?.let { symbol ->
              val symbolKind = symbol.kind
              when {
                typedKinds.contains(symbolKind) -> item.decorateWithSymbolType(location, symbol)
                selectorKinds.contains(symbolKind) -> item.withPriority(PolySymbol.Priority.HIGH)

                // One time bindings and selectors require special handling
                // to not override standard attributes and elements
                symbolKind == NG_DIRECTIVE_ONE_TIME_BINDINGS ->
                  item
                    .decorateWithSymbolType(location, symbol)
                    .withPriority(
                      symbol[OneTimeBindingsScope.PROP_DELEGATE_PRIORITY]
                      ?: PolySymbol.Priority.HIGH
                    )

                symbolKind == NG_DIRECTIVE_EXPORTS_AS ->
                  item.decorateWithJsType(location,
                                          location.parentOfType<XmlTag>()
                                            ?.let { BindingsTypeResolver.get(it).resolveDirectiveExportAsType(item.name) })
                else -> item
              }
            }
          ?: item
        else -> if (kind.namespace == NAMESPACE_JS
                    && typedKinds.contains(item.symbol?.kind))
          item.decorateWithSymbolType(location, item.symbol)
        else
          item

      }
}