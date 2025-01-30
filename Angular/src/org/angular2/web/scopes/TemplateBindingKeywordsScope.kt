package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.model.Pointer.hardPointer
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.JS_PROPERTIES
import com.intellij.webSymbols.WebSymbol.Companion.JS_SYMBOLS
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import com.intellij.webSymbols.utils.ReferencingWebSymbol
import org.angular2.web.NG_TEMPLATE_BINDINGS
import org.angular2.web.NG_TEMPLATE_BINDING_KEYWORDS

object TemplateBindingKeywordsScope : WebSymbolsScope {

  private val KEYWORDS_REF_FOR_JS_SYMBOLS = ReferencingWebSymbol.create(
    JS_SYMBOLS, "keywords", WebSymbolOrigin.empty(), NG_TEMPLATE_BINDING_KEYWORDS)

  private val KEYWORDS_REF_FOR_JS_PROPERTIES = ReferencingWebSymbol.create(
    JS_PROPERTIES, "keywords", WebSymbolOrigin.empty(), NG_TEMPLATE_BINDING_KEYWORDS)

  private val KEYWORDS_REF_FOR_NG_TEMPLATE_BINDINGS = ReferencingWebSymbol.create(
    NG_TEMPLATE_BINDINGS, "keywords", WebSymbolOrigin.empty(), NG_TEMPLATE_BINDING_KEYWORDS)

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind, params: WebSymbolsListSymbolsQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    when (qualifiedKind) {
      JS_SYMBOLS -> listOf(KEYWORDS_REF_FOR_JS_SYMBOLS)
      NG_TEMPLATE_BINDINGS -> listOf(KEYWORDS_REF_FOR_NG_TEMPLATE_BINDINGS)
      JS_PROPERTIES -> listOf(KEYWORDS_REF_FOR_JS_PROPERTIES)
      else -> emptyList()
    }

  override fun getMatchingSymbols(qualifiedName: WebSymbolQualifiedName, params: WebSymbolsNameMatchQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbol> =
    emptyList()

  override fun createPointer(): Pointer<out WebSymbolsScope> =
    hardPointer(this)

  override fun getModificationCount(): Long =
    0
}