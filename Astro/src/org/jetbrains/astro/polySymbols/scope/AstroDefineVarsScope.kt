// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.scope

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.css.CSS_PROPERTIES
import com.intellij.polySymbols.polySymbol
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.js.symbols.asJSSymbol
import com.intellij.polySymbols.js.symbols.getJSPropertySymbols
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.query.polySymbolScopeCached
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlTag
import org.jetbrains.astro.codeInsight.ASTRO_DEFINE_VARS_DIRECTIVE


private fun astroDefineVarsScope(tag: XmlTag, symbolKind: PolySymbolKind, symbolProvider: () -> PolySymbol) =
  polySymbolScopeCached(tag) {
    provides(JS_PROPERTIES, symbolKind)
    initialize {
      cacheDependencies(PsiModificationTracker.MODIFICATION_COUNT)

      element.attributes
        .find { it.name == ASTRO_DEFINE_VARS_DIRECTIVE }
        ?.valueElement
        ?.childrenOfType<JSEmbeddedContent>()
        ?.firstOrNull()
        ?.childrenOfType<JSObjectLiteralExpression>()
        ?.firstOrNull()
        ?.let {
          it.asJSSymbol().getJSPropertySymbols().forEach(::add)
          add(symbolProvider())
        }
    }
  }

internal fun astroScriptDefineVarsScope(scriptTag: XmlTag): PolySymbolScope = astroDefineVarsScope(scriptTag, JS_SYMBOLS) {
  polySymbol(JS_SYMBOLS, "Astro Defined Script Variable") {
    pattern {
      group {
        symbols { from(JS_PROPERTIES) }
        symbolReference()
      }
    }
  }
}

internal fun astroStyleDefineVarsScope(styleTag: XmlTag): PolySymbolScope = astroDefineVarsScope(styleTag, CSS_PROPERTIES) {
  polySymbol(CSS_PROPERTIES, "Astro Defined CSS Variable") {
    pattern {
      group {
        symbols { from(JS_PROPERTIES) }
        sequence {
          literal("--")
          symbolReference()
        }
      }
    }
  }
}