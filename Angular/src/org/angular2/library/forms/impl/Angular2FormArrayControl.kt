package org.angular2.library.forms.impl

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.SymbolNamespace
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS
import org.angular2.web.Angular2SymbolOrigin

object Angular2FormArrayControl : WebSymbol {

  override val name: @NlsSafe String
    get() = "Form array control"

  override val pattern: WebSymbolsPattern? = WebSymbolsPatternFactory.createRegExMatch("[0-9]+")

  override val namespace: @NlsSafe SymbolNamespace
    get() = WebSymbol.Companion.NAMESPACE_JS

  override val kind: @NlsSafe SymbolKind
    get() = NG_FORM_CONTROL_PROPS.kind

  override val origin: WebSymbolOrigin
    get() = Angular2SymbolOrigin.empty

  override val priority: WebSymbol.Priority?
    get() = WebSymbol.Priority.LOWEST

  override val properties: Map<String, Any> =
    mapOf(WebSymbol.Companion.PROP_HIDE_FROM_COMPLETION to true,
          WebSymbol.Companion.PROP_DOC_HIDE_PATTERN to true)

  override fun createPointer(): Pointer<out WebSymbol> =
    Pointer.hardPointer(this)
}