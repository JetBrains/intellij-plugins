package org.angular2.library.forms.scopes

import com.intellij.model.Pointer
import com.intellij.openapi.util.NlsSafe
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.patterns.WebSymbolsPattern
import com.intellij.webSymbols.patterns.WebSymbolsPatternFactory
import com.intellij.webSymbols.query.WebSymbolsListSymbolsQueryParams
import org.angular2.Angular2Framework
import org.angular2.library.forms.NG_FORM_CONTROL_PROPS
import org.angular2.library.forms.NG_FORM_GROUP_PROPS

object Angular2UnknownFormGroup : WebSymbol {

  override val name: @NlsSafe String
    get() = "Unknown form group"

  override val pattern: WebSymbolsPattern? = WebSymbolsPatternFactory.createRegExMatch(".*")

  override fun getSymbols(qualifiedKind: WebSymbolQualifiedKind, params: WebSymbolsListSymbolsQueryParams, scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    when (qualifiedKind) {
      NG_FORM_CONTROL_PROPS -> listOf(Angular2UnknownFormControl)
      NG_FORM_GROUP_PROPS -> listOf(this)
      else -> emptyList()
    }

  override fun isExclusiveFor(qualifiedKind: WebSymbolQualifiedKind): Boolean =
    qualifiedKind == NG_FORM_GROUP_PROPS || qualifiedKind == NG_FORM_CONTROL_PROPS

  override val priority: WebSymbol.Priority?
    get() = WebSymbol.Priority.LOWEST

  override val properties: Map<String, Any> =
    mapOf(WebSymbol.Companion.PROP_HIDE_FROM_COMPLETION to true)

  override val namespace: @NlsSafe SymbolNamespace
    get() = WebSymbol.Companion.NAMESPACE_JS

  override val kind: @NlsSafe SymbolKind
    get() = NG_FORM_GROUP_PROPS.kind

  override val origin: WebSymbolOrigin =
    WebSymbolOrigin.Companion.create(Angular2Framework.Companion.ID)

  override fun createPointer(): Pointer<out WebSymbol> =
    Pointer.hardPointer(this)
}