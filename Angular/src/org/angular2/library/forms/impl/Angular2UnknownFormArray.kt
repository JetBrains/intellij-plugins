package org.angular2.library.forms.impl

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.patterns.PolySymbolsPattern
import com.intellij.webSymbols.patterns.PolySymbolsPatternFactory
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import org.angular2.library.forms.NG_FORM_ARRAY_PROPS
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS
import org.angular2.web.Angular2SymbolOrigin

object Angular2UnknownFormArray : PolySymbol {

  override val name: @NlsSafe String
    get() = "Unknown form array"

  override val pattern: PolySymbolsPattern? = PolySymbolsPatternFactory.createRegExMatch(".*")

  override fun getSymbols(qualifiedKind: PolySymbolQualifiedKind, params: WebSymbolsListSymbolsQueryParams, scope: Stack<PolySymbolsScope>): List<PolySymbolsScope> =
    if (qualifiedKind == NG_FORM_CONTROL_PROPS)
      listOf(Angular2FormArrayControl)
    else
      emptyList()

  override val namespace: @NlsSafe SymbolNamespace
    get() = PolySymbol.Companion.NAMESPACE_JS

  override val kind: @NlsSafe SymbolKind
    get() = NG_FORM_ARRAY_PROPS.kind

  override val origin: PolySymbolOrigin
    get() = Angular2SymbolOrigin.empty

  override val priority: PolySymbol.Priority?
    get() = PolySymbol.Priority.LOWEST

  override val properties: Map<String, Any> =
    mapOf(PolySymbol.Companion.PROP_HIDE_FROM_COMPLETION to true,
          PolySymbol.Companion.PROP_DOC_HIDE_PATTERN to true)

  override fun createPointer(): Pointer<out PolySymbol> =
    Pointer.hardPointer(this)
}