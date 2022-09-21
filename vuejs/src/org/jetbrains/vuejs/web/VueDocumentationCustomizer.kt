// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.javascript.web.lang.js.renderJsTypeForDocs
import com.intellij.openapi.util.text.Strings
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolDocumentation
import com.intellij.webSymbols.WebSymbolDocumentationCustomizer
import com.intellij.webSymbols.WebSymbolsContainer
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.context.isVueContext

class VueDocumentationCustomizer : WebSymbolDocumentationCustomizer {
  override fun customize(symbol: WebSymbol, documentation: WebSymbolDocumentation): WebSymbolDocumentation {
    if (symbol.namespace == WebSymbolsContainer.NAMESPACE_HTML
        && symbol.kind == WebSymbol.KIND_HTML_SLOTS
        && (symbol.origin.framework == VueFramework.ID
            || symbol.psiContext.let { it != null && isVueContext(it) })) {
      symbol.renderJsTypeForDocs(null)
        ?.replace(",", ",<br>")
        ?.let {
          @Suppress("HardCodedStringLiteral")
          return documentation.withDescriptionSection(
            VueBundle.message("vue.documentation.section.slot.scope"),
            "<code>$it</code>"
          )
        }
    }
    else {
      if (symbol.namespace == WebSymbolsContainer.NAMESPACE_HTML
          && symbol.kind == VueWebSymbolsAdditionalContextProvider.KIND_VUE_COMPONENT_PROPS) {
        symbol.renderJsTypeForDocs(Strings.escapeXmlEntities(symbol.name))?.let {
          return documentation.withDefinition(it)
        }
      }
    }
    return documentation
  }
}