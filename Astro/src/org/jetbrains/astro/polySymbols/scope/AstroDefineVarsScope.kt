// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.scope

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.css.CSS_PROPERTIES
import com.intellij.polySymbols.dsl.polySymbol
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.js.symbols.asJSSymbol
import com.intellij.polySymbols.js.symbols.getJSPropertySymbols
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlTag
import org.jetbrains.astro.codeInsight.ASTRO_DEFINE_VARS_DIRECTIVE

abstract class AstroDefineVarsScope(tag: XmlTag) : PolySymbolScopeWithCache<XmlTag, Unit>(tag.project, tag, Unit) {

  protected abstract val providedSymbol: PolySymbol

  override fun provides(kind: PolySymbolKind): Boolean =
    kind == providedSymbol.kind
    || kind == JS_PROPERTIES

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)

    dataHolder.attributes
      .find { it.name == ASTRO_DEFINE_VARS_DIRECTIVE }
      ?.valueElement
      ?.childrenOfType<JSEmbeddedContent>()
      ?.firstOrNull()
      ?.childrenOfType<JSObjectLiteralExpression>()
      ?.firstOrNull()
      ?.let {
        it.asJSSymbol().getJSPropertySymbols().forEach(consumer)
        providedSymbol.let(consumer)
      }
  }
}

class AstroScriptDefineVarsScope(scriptTag: XmlTag) : AstroDefineVarsScope(scriptTag) {
  override fun createPointer(): Pointer<out PolySymbolScopeWithCache<XmlTag, Unit>> {
    val ptr = dataHolder.createSmartPointer()
    return Pointer { ptr.dereference()?.let(::AstroScriptDefineVarsScope) }
  }

  override val providedSymbol: PolySymbol = polySymbol(JS_SYMBOLS, "Astro Defined Script Variable") {
    pattern {
      group {
        symbols { from(JS_PROPERTIES) }
        symbolReference()
      }
    }
  }
}

class AstroStyleDefineVarsScope(styleTag: XmlTag) : AstroDefineVarsScope(styleTag) {
  override fun createPointer(): Pointer<out PolySymbolScopeWithCache<XmlTag, Unit>> {
    val ptr = dataHolder.createSmartPointer()
    return Pointer { ptr.dereference()?.let(::AstroStyleDefineVarsScope) }
  }

  override val providedSymbol: PolySymbol = polySymbol(CSS_PROPERTIES, "Astro Defined CSS Variable") {
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
