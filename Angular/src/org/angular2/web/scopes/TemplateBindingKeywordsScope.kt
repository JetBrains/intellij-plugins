package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.model.Pointer.hardPointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.JS_SYMBOLS
import com.intellij.polySymbols.query.PolySymbolsListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolsNameMatchQueryParams
import com.intellij.polySymbols.query.PolySymbolsScope
import com.intellij.polySymbols.utils.ReferencingPolySymbol
import com.intellij.util.containers.Stack
import org.angular2.web.NG_TEMPLATE_BINDINGS
import org.angular2.web.NG_TEMPLATE_BINDING_KEYWORDS

object TemplateBindingKeywordsScope : PolySymbolsScope {

  private val KEYWORDS_REF_FOR_JS_SYMBOLS = ReferencingPolySymbol.create(
    JS_SYMBOLS, "Angular template binding keyword", PolySymbolOrigin.empty(), NG_TEMPLATE_BINDING_KEYWORDS)

  private val KEYWORDS_REF_FOR_JS_PROPERTIES = ReferencingPolySymbol.create(
    JS_PROPERTIES, "Angular template binding keyword", PolySymbolOrigin.empty(), NG_TEMPLATE_BINDING_KEYWORDS)

  private val KEYWORDS_REF_FOR_NG_TEMPLATE_BINDINGS = ReferencingPolySymbol.create(
    NG_TEMPLATE_BINDINGS, "Angular template binding keyword", PolySymbolOrigin.empty(), NG_TEMPLATE_BINDING_KEYWORDS)

  override fun getSymbols(qualifiedKind: PolySymbolQualifiedKind, params: PolySymbolsListSymbolsQueryParams, scope: Stack<PolySymbolsScope>): List<PolySymbol> =
    when (qualifiedKind) {
      JS_SYMBOLS -> listOf(KEYWORDS_REF_FOR_JS_SYMBOLS)
      NG_TEMPLATE_BINDINGS -> listOf(KEYWORDS_REF_FOR_NG_TEMPLATE_BINDINGS)
      JS_PROPERTIES -> listOf(KEYWORDS_REF_FOR_JS_PROPERTIES)
      else -> emptyList()
    }

  override fun getMatchingSymbols(qualifiedName: PolySymbolQualifiedName, params: PolySymbolsNameMatchQueryParams, scope: Stack<PolySymbolsScope>): List<PolySymbol> =
    emptyList()

  override fun createPointer(): Pointer<out PolySymbolsScope> =
    hardPointer(this)

  override fun getModificationCount(): Long =
    0
}