// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.javascript.web.symbols.*
import com.intellij.lang.javascript.documentation.JSHtmlHighlightingUtil
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.context.isVueContext

class VueDocumentationCustomizer : WebSymbolDocumentationCustomizer {
  override fun customize(symbol: WebSymbol, documentation: WebSymbolDocumentation): WebSymbolDocumentation {
    if (symbol.namespace == WebSymbolsContainer.Namespace.HTML
        && symbol.kind == WebSymbol.KIND_HTML_SLOTS
        && symbol.psiContext.let { it != null && isVueContext(it) }) {
      symbol.jsType?.let {
        @Suppress("HardCodedStringLiteral")
        return documentation.withDescriptionSection(
          VueBundle.message("vue.documentation.section.slot.scope"),
          "<code>${JSHtmlHighlightingUtil.getTypeWithLinksHtmlHighlighting(it, (symbol as? PsiSourcedWebSymbol)?.source, false)}</code>"
        )
      }
    }
    return documentation
  }
}